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
(defrecord FieldDiff [x y color])
(defn at [table x y]
  (-> table (nth y) (nth x)))

(defn at? [table x y]
  (-> table (nth y []) (nth x nil) nil? not))

(defn show-field [field]
  (log "The scene is:")
  (->> field reverse (map (fn [r] (log r))) dorun)
  field)

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

(defn indexed [coll]
  (map-indexed vector coll))

(defn zip [coll-a coll-b]
  (map vector coll-a coll-b))

(defn field-diff [field-a field-b]
  (for [[y [row-a row-b]] (indexed (zip field-a field-b))
        [x [a b]] (indexed (zip row-a row-b))
        :when (not= a b)]
    (do (show-field field-b)
        (FieldDiff. x y b))))

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
                      ;:o
                      :I
                      :O
                      :T
                      :S
                      :Z
                      :J
                      :L])

(defn element-start-x [field-width element-width]
  (- (int (/ field-width 2)) (int (/ element-width 2))))
(defn create-empty-matrix [height width]
  (vec (repeat height (vec (repeat width 0)))))

(defn random-element []
  (->> tetromino-names count rand-int (nth tetromino-names) tetrominos))
(defn init-state [{height :height width :width ticking :ticking} refs]
  (let [{elem-width :width :as element} (random-element)]
    {:stop    #()
     :height  height
     :width   width
     :refs    refs
     :x       (element-start-x width elem-width)
     :y       height
     :element element
     :ticking ticking
     :field   (create-empty-matrix height width)}))



;; - Game UI
(def game-container (gdom/getElement "app"))

; https://www.w3schools.com/css/tryit.asp?filename=trycss_align_container
(defn props [a-map]
  (->> a-map
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

(defn get-rendered-references! [{height :height width :width}]
  (for [y (range height)]
    (for [x (range width)]
      (gdom/getElement (cell-id y x)))))


(defn set-color! [refs x y color]
  (gdom/setProperties
    (at refs x y)
    #js {"style" (props {"border"           "1px solid black"
                         "background-color" ({0 "white" 1 "black"} color)})}))

(defn set-value! [id value]
  (gdom/setTextContent (gdom/getElement id) value))

(defn game-started-message! []
  (set-value! "game-message" "The game is on"))

(defn game-over-message! []
  (log "GAME OVER")
  (set-value! "game-message" "Game over"))

(defn set-game-html! [html-str]
  (set! (.-innerHTML game-container) html-str))

(defn create-parameters [height width ticking]
  {:height height :width width :ticking ticking})

(def default-parameters (create-parameters 20 12 500))
(defn render-game! [parameters]
  (set-game-html!
    (hiccups/html
      [:div {:id "game-message"}]
      (render-game-table parameters)))
  (get-rendered-references! parameters))


;; -- Game logic
(defn is-acceptable [{x                    :x
                      y                    :y
                      field-height         :height
                      field-width          :width
                      {elem-width  :width
                       elem-height :height
                       shape       :shape} :element
                      field                :field}]
  (let [good-cells (for [xi (range 0 elem-width)
                         :let [xg (+ xi x)]
                         yi (range 0 elem-height)
                         :let [yg (+ yi y)]
                         :when (< yg field-height)]
                     (->> [#(and (>= xg 0) (< xg field-width))
                           #(>= yg 0)
                           #(or (= 0 (at shape xi yi))
                                (= 0 (at field xg yg)))]
                          (map apply)
                          (filter false?)
                          empty?))]
    (->> good-cells (filter false?) empty?)))

(defn how-much-can-descend [wish-to state]
  (->> wish-to
       inc
       (range 0)
       reverse
       (map (fn [d] [d (update state :y #(- % d))]))
       (filter (comp is-acceptable second))
       first
       first))

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

(defn merge-if-needed [elem-generator {width  :width
                                       height :height
                                       :as    state}]
  (let [{elem-width :width :as next-element} (elem-generator)]
    (if (pos? (how-much-can-descend 1 state))
      state
      (do (merge state {:field   (add-element-to-field state)
                        :x       (element-start-x width elem-width)
                        :y       height
                        :element next-element})))))

(defn game-over [{stop :stop :as state}]
  (stop)
  (game-over-message!)
  state)

(defn descend [distance state]
  (update state :y #(- % distance)))

(defn get-clear-candidates [{height :height
                             field  :field}]
  (->> height
       (range 0)
       (filter (fn [y] (->> y (nth field) (every? (partial == 1)))))
       seq
       (#(when % ((juxt first last) %)))))

(defn do-clear-candidates [[low-y high-y] {height :height
                                           width  :width
                                           field  :field
                                           :as    state}]
  (let [cleared (inc (- high-y low-y))]
    (assoc state
      :field
      (vec (concat
             (when (pos? low-y) (subvec field 0 low-y))
             (when (< high-y height) (subvec field (inc high-y) height))
             (create-empty-matrix cleared width))))))


(defn game-tick-handler [{y                     :y
                          field-height          :height
                          {elem-height :height} :element
                          :as                   state}]
  (let [clear-candidates (get-clear-candidates state)]
    (if clear-candidates
      (do-clear-candidates clear-candidates state)
      (let [wish-to-descend (if (= y field-height) elem-height 1)
            distance (how-much-can-descend wish-to-descend state)]
        (if (pos? distance)
          (->> state (descend distance) (merge-if-needed random-element))
          (game-over state))))))



(defn change-state [current-state new-state]
  (if (is-acceptable new-state)
    new-state
    current-state))

(defn move-left [state]
  (change-state state (update state :x dec)))

(defn move-right [state]
  (change-state state (update state :x inc)))

; row to column conversion:
;  [1 0]
;  [1 0]
;  [1 1]
;
; 1. top to bottom, straight indices -> turn left
; 0 0 1
; 1 1 1
;
; 2. bottom to top, inverted indices -> turn right
; 1 1 1
; 1 0 0

(defn rotate-matrix-left [matrix]
  (apply map (comp reverse list) matrix))

(defn rotate-matrix-right [matrix]
  (reverse (apply map list matrix)))

(defn rotate [rotate-fn {{width  :width
                          height :height
                          shape  :shape} :element :as state}]
  (change-state state (assoc state :element {:height width :width height :shape (rotate-fn shape)})))

(defn rotate-right [state]
  (rotate rotate-matrix-right state))

(defn rotate-left [state]
  (rotate rotate-matrix-left state))

(defn complete [state]
  (let [distance (how-much-can-descend 2 state)]
    (if (pos? distance)
      (->> state (descend distance) (merge-if-needed random-element))
      (state))))


(def handlers
  {:game-tick     game-tick-handler
   ::move-left    move-left
   ::move-right   move-right
   ::rotate-right rotate-right
   ::rotate-left  rotate-left
   ::complete     complete})

(defn action-handler [state msg]
  (let [new-state ((get handlers msg identity) state)]
    (log "New coords" (:x new-state) (:y new-state))
    new-state))

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

(derive :game-tick ::action)
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


(defn get-location [] js/window.location.host)

(defn verification []
  (let [host (-> (get-location) (str/split ":") first)
        allowed-hosts ["localhost" "retrogames.com"]]
    (when
      (not-any? #{host} allowed-hosts)
      (throw (js/Error. "Oops!")))))


(defn start! [parameters]
  (stop!)
  (let [timed-ch-ctrl (default-ch)
        timed-ch (create-timed-ch timed-ch-ctrl 500)
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

    (game-started-message!)

    (verification)

    (actor "renderer"
           false
           renderer-ch
           null-inbox
           {}
           (fn [{cmds :msg}] (dorun (for [{x :x y :y color :color} cmds]
                                      (do (log "Setting color" x y color)
                                          (set-color! (:refs state) x y color))))))

    (actor "renderer calculator"
           false
           renderer-calculator-ch
           renderer-ch
           (:field state)
           (fn [{last-displayed-state :state new-state-to-display :msg}]
             {:msg   (field-diff last-displayed-state new-state-to-display)
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
           (fn [{msg :msg}] {:msg :game-tick}))

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


;; - rotation - change coordinates?
;;
;; - protect from copying
;;   - run only on specified host, localhost not allowed in prod
;;   - check that compiled code doesn't contain hostname strings
;;   - compare hashes instead of strings, check hashes are not in the source codes
;;
;; - game is not over if continuously press arrowdown
;; - arrowdown must be handled differently
;; - must be able to move left/right in the end before it is merge - MERGE IS DONE ON A SEPARATE TICK!!!
;; - game tick sync: clearing a level and next element?
;; - recording states so i can debug
;;
;; - design: web page buttons so you can play it on your phone
;; - actors must return their inbox? => less code
;; - finding bugs
;; - refactor, simplify
;; - speed as parameters
;; - show next item
;; - switching levels?
;; - controls info
;; - color schemes to choose
;; - new game button
;; - sounds
;; - hardware looking design
;; - description
;; - statistics

;; - check https://www.goodoldtetris.com
;;
;; - icons https://icones.js.org
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