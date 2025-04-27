(ns gg.tetris
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [cljs.core.async :refer [go-loop go <! >! timeout chan put! alts!] :as async]
            [clojure.string :as str]))



;; -- Logging
(def ^:dynamic *logging*)
(def LOG true)
(defn log [& objs]
  (when (and LOG *logging*)
    (println objs)))



;; -- Game state
(def none 0)
(def filled 1)
(defn none? [cell] (== cell none))
(defn filled? [cell] (== cell filled))
(defrecord SetColor [x y color])
(defn at [table x y]
  (-> table (nth y) (nth x)))

(defn at? [table x y]
  (-> table (nth y []) (nth x nil) nil? not))

(defn show-field [field]
  (log "The scene is:")
  (->> field reverse (map (fn [r] (log r))) dorun)
  field)

(def no-element nil)

;; State represents UI field in (x,y) coords
;;
;; x: left to right
;; y: bottom to top
;;
;; Following field...
;; 6 7 8
;; 3 4 5
;; 0 1 2
;; ... is stored like this:
;; [[0 1 2] [3 4 5] [6 7 8]]
(defn field-diff [state-a state-b]
  (for [[y [row-a row-b]] (map-indexed vector (map vector state-a state-b))
        [x [a b]] (map-indexed vector (map vector row-a row-b))
        :when (not= a b)]
    (do (show-field state-b)
        (SetColor. x y b))))

;; https://tetris.fandom.com/wiki/Tetromino
(def tetrominos {
                 :o {:height 1 :width 1 :shape [[1]]}
                 :I {:height 1 :width 4 :shape [[1 1 1 1]]}
                 :O {:height 2 :width 2 :shape [[1 1]
                                                [1 1]]}
                 :T {:height 2 :width 3 :shape [[1 1 1]
                                                [0 1 0]]}
                 :S {:height 2 :width 3 :shape [[1 1 0]
                                                [0 1 1]]}
                 :Z {:height 2 :width 3 :shape [[0 1 1]
                                                [1 1 0]]}
                 :J {:height 3 :width 2 :shape [[1 1]
                                                [0 1]
                                                [0 1]]}
                 :L {:height 3 :width 2 :shape [[1 1]
                                                [1 0]
                                                [1 0]]}})


(def tetromino-names [
                      :o
                      :I
                      :O
                      :T
                      :S
                      :Z
                      :J
                      :L])

(defn element-start-x [field-width element-width]
  (- (int (/ field-width 2)) (int (/ element-width 2))))

(defn random-element []
  (->> tetromino-names count rand-int (nth tetromino-names) tetrominos))
(defn init-state [{height :height width :width} refs]
  (let [{elem-width :width :as element} (random-element)]
    {:stop    #()
     :height  height
     :width   width
     :refs    refs
     ;;left-bottom corner of the object
     :x       (element-start-x width elem-width)
     :y       height
     :element element
     :field   (vec (repeat height (vec (repeat width none))))}))



;; - Game UI
(def game-container (gdom/getElement "app"))

; https://www.w3schools.com/css/tryit.asp?filename=trycss_align_container
(defn props [m]
  (->> m
       (map (fn [[k v]] (str k ":" v)))
       (str/join "; ")))
(defn cell-id [y x]
  (str "cell:" y ":" x))
(defn render-game-table [{height :height width :width}]
  (let [square-px 30
        sizer (fn [items] (str (* items square-px) "px"))
        table-style (props {"height"          (sizer height)
                            "width"           (sizer width)
                            "margin"          "auto"
                            "border"          "1px solid black"
                            "border-collapse" "collapse"})
        create-row (fn [row] (into [:tr]
                                   (map (fn [col] [:td {:id (cell-id row col) :style "border: 1px solid"} ""])
                                        (range width))))]
    [:div (into [:table {:style table-style}]
                (map create-row (reverse (range height))))]))

(defn get-rendered-references [{height :height width :width}]
  (for [y (range height)]
    (for [x (range width)]
      (gdom/getElement (cell-id y x)))))


(defn set-color [refs x y color]
  (gdom/setProperties
    (at refs x y)
    #js {"style" (props {"border"           "1px solid black"
                         "background-color" ({0 "white" 1 "black"} color)})}))

(defn set-value [id value]
  (gdom/setTextContent (gdom/getElement id) value))

(defn game-started-message []
  (set-value "game-message" "The game is on"))

(defn game-over-message []
  (log "GAME OVER")
  (set-value "game-message" "Game over"))

(defn set-game-html [html-str]
  (set! (.-innerHTML game-container) html-str))

(defn create-parameters [height width]
  {:height height :width width})

(def default-parameters (create-parameters 20 12))
(defn render-game! [parameters]
  (set-game-html
    (hiccups/html
      [:div {:id "game-message"}]
      (render-game-table parameters)))
  (get-rendered-references parameters))



;; -- Game logic
;;
;; [0 0 0 0 0]
;; [0 1 0 0 0]
;; [0 1 1 1 0]
;; [0 1 0 1 0]
;; x = 1
;; width = 3
;; output: [[1 3] [2 2] [3 2]]
(defn find-top-heights [element-x element-width field]
  (let [x-range-start element-x
        x-range-end (+ element-x element-width)]
    (->> field
         (map-indexed vector)
         reverse
         (reduce
           (fn [accum [y-idx row]]
             (map (fn [[x-idx {is-done :finished last-value :value} :as done]]
                    (if is-done
                      done
                      (if (= 0 (nth row x-idx))
                        [x-idx {:finished false :value y-idx}]
                        [x-idx {:finished true :value last-value}])))
                  accum))
           (map vector
                (range x-range-start x-range-end)
                (repeat element-width {:finished false :value (count field)})))
         (map (fn [[_ {value :value}]] value)))))



(defn how-much-can-descend [{x               :x
                             y               :y
                             {width  :width
                              height :height
                              shape  :shape} :element
                             field           :field}]
  (let [wish-to-descend (if (= y (count field)) height 1)

        heights-outside (find-top-heights x width field)
        heights-inside (find-top-heights 0 width (reverse shape))

        free-outside (map #(- y %) heights-outside)
        free-inside (map #(- (count shape) %) heights-inside)

        free (map + free-outside free-inside)
        how-much-free-space (apply min free)]
    (if (pos? how-much-free-space)
      (min wish-to-descend how-much-free-space)
      0)))

(defn add-element-to-field [{field           :field
                             x               :x
                             y               :y
                             {height :height
                              width  :width
                              shape  :shape} :element}]
  (let [blocks (for [ye (range height)
                     xe (range width)
                     :when (= 1 (at shape xe ye))]
                 [(+ xe x) (+ ye y)])
        visible-blocks (filter (fn [[x y]] (at? field x y)) blocks)]
    (loop [[[xb yb :as b] & bs] visible-blocks
           field field]
      (if b
        (recur bs (assoc-in field [yb xb] 1))
        field))))

;; todo add test
(defn merge-if-needed [{width  :width
                        height :height
                        :as    state}]
  (let [{elem-width :width :as next-element} (random-element)]
    (if (pos? (how-much-can-descend state))
      state
      (do (merge state {:field   (add-element-to-field state)
                        :x       (element-start-x width elem-width)
                        :y       height
                        :element next-element})))))
;; todo add test
(defn game-over [{stop :stop :as state}]
  (stop)
  (game-over-message)
  state)
;; todo add test
(defn descend [distance {y     :y
                         field :field
                         :as   state}]
  (if (= y (count field))
    (update state :y #(- % distance))
    (update state :y dec)))

;; todo add test
(defn descend-handler [state]
  (let [distance (how-much-can-descend state)]
    (if (pos? distance)
      (->> state (descend distance) merge-if-needed)
      (game-over state))))

(def handlers
  {:descend descend-handler})
(defn action-handler [state msg]
  (let [new-state ((get handlers msg identity) state)]
    (log "New coords" (:x new-state) (:y new-state))
    new-state))


(defn render [last-displayed-state new-state]
  (show-field new-state)
  (field-diff last-displayed-state new-state))

(defn listen [down-listener up-listener]
  (gevents/listen js/document "keydown" down-listener)
  (gevents/listen js/document "keyup" up-listener))

(defn create-kbd-ch []
  (let [kbd-inbox (chan)]
    (listen #(put! kbd-inbox {:action :down :key (.-key %)})
            #(put! kbd-inbox {:action :up :key (.-key %)}))
    kbd-inbox))

;; fn : {:msg msg-in :state} -> {:msg msg-out :state state}
;; if msg-out is nil, the message is not sent to channel, only recur is performed
;; current state of the actor is replaced with the state that the function returns
(defn actor [id logging inbox outbox state fn]
  (println "Starting actor" id)
  (go-loop [state state]
           (let [msg-in (<! inbox)]
             (binding [*logging* logging]
               (log "[" id "]" "received a message" (subs (str msg-in) 0 100))
               (if (= msg-in :quit)
                 (log "Quitting" id)
                 (let [{msg-out :msg new-state :state} (fn {:state state :msg msg-in})]
                   (when msg-out (>! outbox msg-out))
                   (recur new-state)))))))

(defn create-timed-ch [ctrl interval-ms]
  (let [inbox (chan)]
    (go-loop []
             (let [[v ch] (alts! [ctrl (timeout interval-ms)])]
               (when (and (= ch ctrl) (= v :quit)) nil)
               (>! inbox {})
               (recur)))
    inbox))

(defn default-ch [] (chan (async/sliding-buffer 10)))

(derive ::descend ::action)
(derive ::complete ::action)
(derive ::move-left ::action)
(derive ::move-right ::action)
(derive ::rotate-left ::action)
(derive ::rotate-right ::action)

(defn interpret-kbd-input [input]
  (let [key-commands {[[] "w"]              ::rotate-right
                      [["shift"] "w"]       ::rotate-left
                      [[] "a"]              ::move-left
                      [[] "s"]              ::complete
                      [[] "d"]              ::move-right

                      [[] " "]              ::complete

                      [[] "arrowup"]        ::rotate-right
                      [["shift"] "arrowup"] ::rotate-left
                      [[] "arrowdown"]      ::complete
                      [[] "arrowleft"]      ::move-left
                      [[] "arrowright"]     ::move-right

                      [[] "enter"]          ::rotate-right
                      [["shift"] "enter"]   ::rotate-left}]
    (let [{modifiers :modifiers pressed :pressed} input
          modifiers (filter #{"shift"} modifiers)
          pair [modifiers pressed]]
      (key-commands pair))))

;; -- Map of channels
;; kbd listener
;;    register what's pressed, e.g. [Shift + Enter]
;;    kbd-inbox -> chord-inbox
;; keyboard-interpreter
;;    interpret what was entered via keyboard, e.g. [Shift + Enter] -> :rotate
;;    chord-inbox -> action-inbox
;; ticker
;;    regularly descend the current element
;;    timed-inbox -> action-inbox
;;
;;
;; action-handler:
;;    action-inbox -> renderer-calculator-inbox
;;
;; renderer calculator:
;;    renderer-calculator-inbox -> renderer-inbox
;; renderer:
;;    renderer-inbox -> null-inbox
;;
;;
(defn generate-scene [state]
  (log "Generated scene")
  (show-field (add-element-to-field state)))

(defonce game (atom {:stop #()}))
(defn stop! []
  ((:stop @game)))
(defn start! [parameters]
  (stop!)
  (let [timed-ch-ctrl (default-ch)
        timed-ch (create-timed-ch timed-ch-ctrl 100)
        kbd-ch (create-kbd-ch)
        null-inbox (default-ch)

        renderer-ch (default-ch)
        renderer-calculator-ch (default-ch)
        action-ch (default-ch)
        scene-ch (default-ch)
        chord-ch (default-ch)

        refs (render-game! parameters)
        state (init-state parameters refs)
        state (assoc state :stop (fn []
                                   (->> [timed-ch-ctrl timed-ch kbd-ch chord-ch action-ch scene-ch
                                         renderer-calculator-ch renderer-ch]
                                        (map (fn [ch] (put! ch :quit)))
                                        dorun)))]

    (game-started-message)

    (actor "renderer"
           false
           renderer-ch
           null-inbox
           {}
           (fn [{cmds :msg}] (dorun (for [{x :x y :y color :color} cmds]
                                      (do (log "Setting color" x y color)
                                          (set-color (:refs state) x y color))))))

    (actor "renderer calculator"
           false
           renderer-calculator-ch
           renderer-ch
           (:field state)
           (fn [{last-displayed-state :state new-state-to-display :msg}]
             {:msg   (render last-displayed-state new-state-to-display)
              :state new-state-to-display}))

    (actor "scene generator"
           false
           scene-ch
           renderer-calculator-ch
           {}
           (fn [{msg :msg}]
             (let [scene (generate-scene msg)]
               {:msg scene})))

    (actor "action handler"
           false
           action-ch
           scene-ch
           state
           (fn [{state :state msg :msg}]
             (let [new-game-state (action-handler state msg)]
               {:msg new-game-state :state new-game-state})))

    (actor "ticker"
           false
           timed-ch
           action-ch
           {}
           (fn [{msg :msg}] {:msg :descend}))

    (actor "kbd interpreter"
           false
           chord-ch
           action-ch
           {}
           (fn [{input :msg}] {:msg (interpret-kbd-input input)}))

    (actor "kbd listener"
           false
           kbd-ch
           chord-ch
           []
           (fn [{msg :msg modifiers :state}]
             (let [{action :action key :key} msg
                   key (str/lower-case key)
                   is-modifier? #{"shift" "alt" "ctrl" "meta"}]
               (condp = action
                 :down
                 (if (is-modifier? key)
                   {:msg nil :state (conj modifiers key)}
                   {:msg {:modifiers modifiers :pressed key} :state modifiers})
                 :up
                 (if (is-modifier? key)
                   {:msg nil :state (filterv #(not= % key) modifiers)}
                   {:msg nil :state modifiers})))))

    (reset! game (assoc state :stop (fn []
                                      (->> [timed-ch-ctrl timed-ch kbd-ch chord-ch action-ch scene-ch
                                            renderer-calculator-ch renderer-ch]
                                           (map (fn [ch] (put! ch :quit)))
                                           dorun))))
    nil))


(start! default-parameters)




;; - make all pieces work
;;   - simplify
;;
;;
;; - actions:
;    - move left/right and reuse descend
;    - item with new coords doesn't hit fields blocks and edges
;; - rotate left/right, complete
;;
;; - proper test coverage, refactor, simplify
;;
;; -  erasing up horizontal blocks
;; -  speed as parameters
;; -  controls info
;; -  color schemes to choose
;; -  new game button
;; -  sounds

;;
;; stealing precaution: hostname and verify what is visible in the obfuscated code
;; domain name
;; use the domain name
;; robots.txt
;; seo
;; check how much traffic
;;
;;
;;
;; -1. google for more methods
;
;0. if check failed
;	- slow down the browser
;	- show the actual website and send the user there - use their website to your ad platform
;	- close the window if user disagrees
;
;1. addition obsfuscation methods
;	- compare to hash/hashes of the domain
;	- js references (window / location) - are they spilled?
;	- merge strings from bytes and send to js/eval to avoid detection in the output
;
;2. check the current location with JS
;	- different types of access checks in different places
;
;3. based on user's time
;	- same methods but "js works only until may 31 this year" - and somehow update on redeploy
;	- web pages auto reload themselves
;4. do request to backend on front (with website's url)
;	- backend checks from what site the query came, and gives bad uids


;; Model
(def initial-state
  {:contacts []
   :selected nil
   :editing? false})

(defn make-address [address]
  (select-keys address [:street :city :state :postal :country]))

(defn maybe-set-address [contact]
  (if (:address contact)
    (update contact :address make-address)
    contact))

(defn make-contact [contact]
  (-> contact
      (select-keys [:first-name :last-name :email :address])
      (maybe-set-address)))

(defn add-contact [contact-list input]
  (conj contact-list (make-contact input)))

(defn replace-contact [contact-list idx input]
  (assoc contact-list idx (make-contact input)))

(defn remove-contact [contact-list idx]
  (vec (concat (subvec contact-list 0 idx)
               (subvec contact-list (inc idx)))))



;; UI behavior
(declare refresh!)

(def app-container (gdom/getElement "app"))

(defn set-app-html! [html-str]
  (set! (.-innerHTML app-container) html-str))

(defn on-add-contact [state]
  (refresh! (-> state
                (assoc :editing? true)
                (dissoc :selected))))

(defn get-field-value [id]
  (let [value (.-value (gdom/getElement id))]
    (when (seq value) value)))

(defn get-contact-form-data []
  {:first-name (get-field-value "input-first-name")
   :last-name  (get-field-value "input-last-name")
   :email      (get-field-value "input-email")
   :address    {:street  (get-field-value "input-street")
                :city    (get-field-value "input-city")
                :state   (get-field-value "input-state")
                :postal  (get-field-value "input-postal")
                :country (get-field-value "input-country")}})

(defn on-save-contact [state]
  (refresh!
    (let [contact (get-contact-form-data)
          idx (:selected state)
          state (dissoc state :selected :editing?)]
      (if idx
        (update state :contacts replace-contact idx contact)
        (update state :contacts add-contact contact)))))

(defn on-cancel-edit [state]
  (refresh! (dissoc state :selected :editing?)))

(defn on-open-contact [e state]
  (refresh!
    (let [idx (int (.. e -currentTarget -dataset -idx))]
      (assoc state :selected idx
                   :editing? true))))

(defn on-delete-contact [e state]
  (.stopPropagation e)
  (let [idx (int (.. e -currentTarget -dataset -idx))]
    (refresh! (-> state
                  (update :contacts remove-contact idx)
                  (cond-> (= idx (:selected state))
                          (dissoc :selected :editing?))))))

(defn attach-event-handlers! [state]
  (when-let [add-button (gdom/getElement "add-contact")]
    (gevents/listen add-button "click"
                    (fn [_] (on-add-contact state))))

  (when-let [save-button (gdom/getElement "save-contact")]
    (gevents/listen save-button "click"
                    (fn [_] (on-save-contact state))))

  (when-let [cancel-button (gdom/getElement "cancel-edit")]
    (gevents/listen cancel-button "click"
                    (fn [_] (on-cancel-edit state))))

  (doseq [elem (array-seq (gdom/getElementsByClass "contact-summary"))]
    (gevents/listen elem "click"
                    (fn [e] (on-open-contact e state))))

  (doseq [elem (array-seq (gdom/getElementsByClass "delete-icon"))]
    (gevents/listen elem "click"
                    (fn [e] (on-delete-contact e state)))))



;; UI rendering
(defn action-button [id text icon-class]
  [:button {:id    id
            :class "button is-primary is-light"}
   [:span {:class (str "mu " icon-class)}]
   (str " " text)])

(def save-button (action-button "save-contact" "Save" "mu-file"))
(def cancel-button (action-button "cancel-edit" "Cancel" "mu-cancel"))
(def add-button (action-button "add-contact" "Add" "mu-plus"))

(defn section-header [editing?]
  [:div {:class "section-header"}
   [:div {:class "level"}
    [:div {:class "level-left"}
     [:div {:class "level-item"}
      [:h1 {:class "subtitle"}
       [:span {:class "mu mu-user"}]
       "Edit Contact"]]]
    [:div {:class "level-right"}
     (if editing?
       [:div {:class "buttons"} cancel-button save-button]
       add-button)]]])

(defn format-name [contact]
  (->> contact
       ((juxt :first-name :last-name))
       (str/join " ")))

(defn delete-icon [idx]
  [:span {:class    "delete-icon"
          :data-idx idx}
   [:span {:class "mu mu-delete"}]])

(defn render-contact-list-item [idx contact selected?]
  [:div {:class    (str "card contact-summary" (when selected? " selected"))
         :data-idx idx}
   [:div {:class "card-content"}
    [:div {:class "level"}
     [:div {:class "level-left"}
      [:div {:class "level-item"}
       (delete-icon idx)
       (format-name contact)]]
     [:div {:class "level-right"}
      [:span {:class "mu mu-right"}]]]]])

(defn render-contact-list [state]
  (let [contacts (:contacts state)
        selected (:selected state)]
    [:div {:class "contact-list column is-4 hero"}
     (map-indexed
       (fn [idx contact] (render-contact-list-item idx contact (= idx selected)))
       contacts)]))

(def no-contact-details
  [:p {:class "notice"}
   "No contact selected"])

(defn form-field
  ([id value label] (form-field id value label "text"))
  ([id value label type]
   [:div {:class "field"}
    [:label {:class "label"} label]
    [:div {:class "control"}
     [:input {:id    id
              :value value
              :type  type
              :class "input"}]]]))

(defn render-contact-details [contact]
  (let [address (get contact :address {})]
    [:div {:id "contact-form" :class "contact-form"}
     (form-field "input-first-name" (:first-name contact) "First Name")
     (form-field "input-last-name" (:last-name contact) "Last Name")
     (form-field "input-email" (:email contact) "Email" "email")
     [:fieldset
      [:legend "Address"]
      (form-field "input-street" (:street address) "Street")
      (form-field "input-city" (:city address) "City")
      (form-field "input-state" (:state address) "State")
      (form-field "input-postal" (:postal address) "Postal Code")
      (form-field "input-country" (:country address) "Country")]]))

(def top-bar
  [:div {:class "navbar has-shadow"}
   [:div {:class "container"}
    [:div {:class "navbar-brand"}
     [:span {:class "navbar-item"}
      "ClojureScript Contacts Alpha"]]]])

(defn render-app! [state]
  (set-app-html!
    (hiccups/html
      [:div {:class "app-main"}]
      top-bar
      [:div {:class "columns"}]
      (render-contact-list state)
      [:div {:class "contact-details column is-8"}]
      (section-header (:editing? state))
      [:div {:class "hero"}]
      (if (:editing? state)
        (render-contact-details (get-in state [:contacts (:selected state)] {}))
        no-contact-details))))


;
;;; Running the whole app
;(defn refresh! [state]
;  (render-app! state)
;  (attach-event-handlers! state))
;
;(refresh! initial-state)