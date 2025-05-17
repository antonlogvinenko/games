(ns gg.tetris
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [cljs.core.async :refer [go-loop go <! >! timeout chan put! alts!] :as async]
            [clojure.string :as str]
            [gg.md5 :refer [md5]]))


;; -- Logging
(def ^:dynamic *logging*)
(def LOG true)
(defn log [& objs]
  (when (and LOG *logging*)
    (println objs)))


;; -- Game state
(defrecord FieldDiff [x y color])
(defn at-tt [table x y]
  (-> table (nth y []) (nth x 0)))
(defn at-field
  [table x y]
  (-> table (nth y []) (nth x 1)))

(defn at-field? [table x y]
  (-> table (nth y []) (nth x nil) nil? not))


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
;;
(def empty-space-size 6)

(defn indexed [coll]
  (map-indexed vector coll))

(defn zip [coll-a coll-b]
  (map vector coll-a coll-b))

(defn field-diff [field-a field-b]
  (for [[y [row-a row-b]] (indexed (zip field-a field-b))
        [x [a b]] (indexed (zip row-a row-b))
        :when (not= a b)]
    (FieldDiff. x y b)))


;; https://tetris.fandom.com/wiki/Tetromino

(def tetrominos
  {::super {::I [[[0 0 0 0] [0 0 0 0] [1 1 1 1] [0 0 0 0]]
                 [[0 0 1 0] [0 0 1 0] [0 0 1 0] [0 0 1 0]]
                 [[0 0 0 0] [1 1 1 1] [0 0 0 0] [0 0 0 0]]
                 [[0 1 0 0] [0 1 0 0] [0 1 0 0] [0 1 0 0]]]
            ::O [[[1 1] [1 1]] [[1 1] [1 1]] [[1 1] [1 1]] [[1 1] [1 1]]]
            ::T [[[0 0 0] [1 1 1] [0 1 0]]
                 [[0 1 0] [0 1 1] [0 1 0]]
                 [[0 1 0] [1 1 1] [0 0 0]]
                 [[0 1 0] [1 1 0] [0 1 0]]]
            ::S [[[0 0 0] [1 1 0] [0 1 1]]
                 [[0 0 1] [0 1 1] [0 1 0]]
                 [[1 1 0] [0 1 1] [0 0 0]]
                 [[0 1 0] [1 1 0] [1 0 0]]]
            ::Z [[[0 0 0] [0 1 1] [1 1 0]]
                 [[0 1 0] [0 1 1] [0 0 1]]
                 [[0 1 1] [1 1 0] [0 0 0]]
                 [[1 0 0] [1 1 0] [0 1 0]]]
            ::J [[[0 0 0] [1 1 1] [1 0 0]]
                 [[0 1 0] [0 1 0] [0 1 1]]
                 [[0 0 1] [1 1 1] [0 0 0]]
                 [[1 1 0] [0 1 0] [0 1 0]]]
            ::L [[[0 0 0] [1 1 1] [0 0 1]]
                 [[0 1 1] [0 1 0] [0 1 0]]
                 [[1 0 0] [1 1 1] [0 0 0]]
                 [[0 1 0] [0 1 0] [1 1 0]]]}})

(def tetromino-names [
                      ::I
                      ::O
                      ::T
                      ::S
                      ::Z
                      ::J
                      ::L])

(def rotation-systems [::super])

(defn element-start-x [field-width element-width]
  (- (int (/ field-width 2)) (int (/ element-width 2))))
(defn create-empty-matrix [height width]
  (vec (repeat height (vec (repeat width 0)))))

(defn get-tt [tsys id form]
  (let [tt (-> tetrominos tsys id (nth form))
        tsize (count tt)]
    [tt tsize]))

(defn random-element []
  (->> tetromino-names count rand-int (nth tetromino-names)))

(defn create-tt-gen []
  (let [random-elements-stream (repeatedly random-element)]
    (fn [] random-elements-stream)))

(defn first-gen [gen]
  (first (gen)))

(defn rest-gen [gen]
  (let [all (gen)
        rest-elements (rest all)]
    (fn [] rest-elements)))



(defn init-state [{height :height width :width ticking :ticking tsys :tsys} refs next-elem]
  (let [tt-gen (create-tt-gen)
        id (first-gen tt-gen)
        [tt ts] (get-tt tsys id 0)]
    {:stop      #()
     :height    height
     :width     width
     :refs      refs
     :next-elem next-elem
     :x         (element-start-x width ts)
     :y         height
     :tid       id
     :tform     0
     :tt-gen    (rest-gen tt-gen)
     :tsys      ::super
     :ticking   ticking
     :field     (create-empty-matrix height width)}))


;; - Game UI
(def game-container (gdom/getElement "app"))

; https://www.w3schools.com/css/tryit.asp?filename=trycss_align_container
(defn props [a-map]
  (->> a-map
       (map (fn [[k v]] (str k ":" v)))
       (str/join "; ")))
(defn cell-id [prefix y x]
  (str prefix ":cell:" y ":" x))
(defn render-table [id-prefix height width table-props]
  (let [square-px 30
        sizer (fn [items] (str (* items square-px) "px"))
        table-style (props (merge table-props {"height" (sizer height)
                                               "width"  (sizer width)
                                               ;"margin" "auto"
                                               "border" "1px solid black"}))
        ;"border-collapse" "collapse"})
        create-row (fn [row] (into [:tr]
                                   (map (fn [col] [:td {:id (cell-id id-prefix row col) :style "border: 1px solid"} ""])
                                        (range width))))]
    [:div (into [:table {:style table-style}]
                (map create-row (reverse (range height))))]))


(defn get-rendered-references! [prefix height width]
  (for [y (range height)]
    (for [x (range width)]
      (gdom/getElement (cell-id prefix y x)))))


(defn set-color! [target x y color]
  (gdom/setProperties
    (at-field target x y)
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
  {:height height :width width :ticking ticking :tsys ::super})

(def default-parameters (create-parameters 20 12 2000))
(defn render-game! [{height :height width :width :as parameters}]
  (set-game-html!
    (hiccups/html
      [:div
       [:table {:width "800px" :style "margin: auto"}
        [:tr
         [:td {:style (props {"vertical-align" "top" "text-align" "right"})}
          [:div {:id "game-message"}]]
         [:td (render-table "field" height width {"margin" "auto"})]
         [:td {:style (props {"vertical-align" "top"})}
          (render-table "next-elem" empty-space-size empty-space-size nil)]]]]))
  [(get-rendered-references! "field" height width)
   (get-rendered-references! "next-elem" height width)])



;; Cells incompatibility:
;;   - the field is surrounded by 1s
;;   - tetrominos are surrounded by 0s
;;   - (t, f) = (1, 1) pair at the same idx is not acceptable
;; This is made so that tetrominos
;;   - can't overlap with existing structure in the field
;;   - can go outside the field but only with their empty space
(defn is-acceptable [{x            :x
                      y            :y
                      field-height :height
                      tid          :tid
                      tsys         :tsys
                      tform        :tform
                      field        :field}]
  (let [[tt tsize] (get-tt tsys tid tform)
        cells-acceptability (for [xi (range 0 tsize)
                                  yi (range 0 tsize)
                                  :let [xg (+ xi x)
                                        yg (+ yi y)
                                        tt-cell (at-tt tt xi yi)
                                        field-cell (at-field field xg yg)]
                                  :when (< yg field-height)]
                              (or (= 0 tt-cell) (= 0 field-cell)))]
    (reduce #(and %1 %2) cells-acceptability)))

(defn how-much-can-descend [wish-to state]
  (->> wish-to
       inc
       (range 0)
       reverse
       (map (fn [d] [d (update state :y #(- % d))]))
       (filter (comp is-acceptable second))
       first
       first))

(defn add-element-to-field [{field :field
                             x     :x
                             y     :y
                             tsys  :tsys
                             tid   :tid
                             tform :tform}]
  (let [[tt tsize] (get-tt tsys tid tform)
        blocks (for [ye (range tsize)
                     xe (range tsize)
                     :when (= 1 (at-tt tt xe ye))]
                 [(+ xe x) (+ ye y)])
        visible-blocks (filter (fn [[x y]] (at-field? field x y)) blocks)]
    (loop [[[xb yb :as b] & bs] visible-blocks
           field field]
      (if b
        (recur bs (assoc-in field [yb xb] 1))
        field))))

(defn merge-element [{width    :width
                      height :height
                      tsys   :tsys
                      tt-gen :tt-gen
                      :as    state}]
  (let [id (first-gen tt-gen)
        [tt ts] (get-tt tsys id 0)]
    (merge state {:field  (add-element-to-field state)
                  :x      (element-start-x width ts)
                  :y      (- height 2)
                  :tid    id
                  :tt-gen (rest-gen tt-gen)
                  :tform  0})))

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

;; a game tick does one of three:
;; - descend
;; - game over
;; - merge + clear line if present? + descend next element + generate new next tt
;;
;; important: merge is done separately from the previous descend because the element
;; must be able to move left and right before the last descend and the merge
(defn game-tick-handler [{y            :y
                          field-height :height
                          tid          :tid
                          tform        :tform
                          tsys         :tsys
                          :as          state}]
  (let [[_ ts] (get-tt tsys tid tform)
        wish-to-descend (if (= y field-height) ts 1)
        distance (how-much-can-descend wish-to-descend state)]
    (if (pos? distance)
      (descend distance state)
      (if (== field-height y)
        (game-over state)
        (let [merged-state (merge-element state)
              clear-candidates (get-clear-candidates merged-state)]
          (if clear-candidates
            (do-clear-candidates clear-candidates merged-state)
            merged-state))))))


(defn try-new-state [current-state new-state]
  (if (is-acceptable new-state)
    new-state
    current-state))

(defn move-left [state]
  (update state :x dec))

(defn move-right [state]
  (update state :x inc))

(defn mod4 [op]
  (comp #(mod % 4) op))

(defn rotate-right [state]
  (update state :tform (mod4 inc)))

(defn rotate-left [state]
  (update state :tform (mod4 dec)))

(defn move-left-action [state]
  (->> state move-left (try-new-state state)))

(defn move-right-action [state]
  (->> state move-right (try-new-state state)))

(defn with-wall-kicks [state rotate-fn]
  (let [rotated (rotate-fn state)]
    (->> [rotated (move-left state) (move-right state)]
         (filter is-acceptable)
         first
         (try-new-state state))))

(defn rotate-right-action [state]
  (with-wall-kicks state rotate-right))

(defn rotate-left-action [state]
  (with-wall-kicks state rotate-left))

(defn complete-action [state]
  (let [distance (how-much-can-descend 2 state)]
    (if (pos? distance)
      (descend distance state)
      state)))

(def handlers
  {:game-tick     game-tick-handler
   ::move-left    move-left-action
   ::move-right   move-right-action
   ::rotate-right rotate-right-action
   ::rotate-left  rotate-left-action
   ::complete     complete-action})

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
(def next-element-empty-space (repeat empty-space-size (repeat empty-space-size 0)))
(defn add-element-to-next [elem]
  (let [d (- (int (/ empty-space-size 2)) (int (/ (count elem) 2)))]
    (for [yg (range 0 empty-space-size)]
      (for [xg (range 0 empty-space-size)
            :let [xi (- xg d)
                  yi (- yg d)]]
        (at-tt elem xi yi)))))


(defn generate-scene [{tt-gen :tt-gen tsys :tsys :as state}]
  {:field     (add-element-to-field state)
   :next-elem (add-element-to-next (first (get-tt tsys (first-gen tt-gen) 0)))})

(defonce game (atom {:stop #()}))
(defn stop! []
  ((:stop @game)))


(defn get-location [] js/window.location.host)

(defn verification []
  (let [host (-> (get-location) (str/split ":") first md5)
        allowed-hosts [
                       "8561520c9e13eeb5b228e32c163800c1"
                       "db6d8dce28da168c3546ca7358c91d7a"
                       "0807f8526a95151dd52eef93cdc47876"]]
    (when
      (not-any? #{host} allowed-hosts)
      (throw (js/Error. "Oops!")))))


(defn update-colors [target diff]
  (dorun (for [{x :x y :y color :color} diff]
           (do (log "Setting color" x y color)
               (set-color! target x y color)))))

(defn start! [parameters]
  (stop!)
  (let [timed-ch-ctrl (default-ch)
        timed-ch (create-timed-ch timed-ch-ctrl 1000)
        kbd-ch (create-kbd-ch)
        null-inbox (default-ch)

        renderer-ch (default-ch)
        renderer-calculator-ch (default-ch)
        action-ch (default-ch)
        scene-ch (default-ch)
        chord-ch (default-ch)

        [field, next-elem] (render-game! parameters)
        state (init-state parameters field next-elem)
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
           (fn [{{field-diff     :field
                  next-elem-diff :next-elem} :msg}]
             (update-colors field field-diff)
             (update-colors next-elem next-elem-diff)))

    (actor "renderer calculator"
           false
           renderer-calculator-ch
           renderer-ch
           {:field     (:field state)
            :next-elem next-element-empty-space}
           (fn [{{field-1 :field next-elem-1 :next-elem}             :state
                 {field-2 :field next-elem-2 :next-elem :as state-2} :msg}]
             {:msg   {:field     (field-diff field-1 field-2)
                      :next-elem (field-diff next-elem-1 next-elem-2)}
              :state state-2}))

    (actor "scene generator"
           false
           scene-ch
           renderer-calculator-ch
           {}
           (fn [{state :msg}]
             (let [scene (generate-scene state)]
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
           (fn [_] {:msg :game-tick}))

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


;; - rotate during element generation (or merge) fails the game
;; - game is not over if continuously press arrowdown
;;
;; - show next item
;;   - render the next element visually "in the middle" - check other tetris games
;; - arrowdown must be handled differently - smooth descend
;; - pause the game when the webpage is left
;;
;;
;;
;; - unit tests: eval first to see it in report
;;
;; - compare to another game that descend vs game-over vs merge clean lines show next gen next is correct
;; - try https://domainlockjs.com
;; - log errors so i can debug what i see
;; - domain name
;; - SEO
;;
;; - calculating score
;; - switching levels?
;; - new game button
;; - actors must return their inbox? => less code
;; - design: web page buttons so you can play it on your phone
;; - controls info
;; - sounds
;; - hardware looking design? generic design?
;; - description
;; - SEO
;; - how to track visitors
;;
;;
;; - recording states so I can debug
;; - protect from copying
;;   - https://domainlockjs.com
;;   - less direct: set var, not exception
;;   - call from several places
;;   - hide reading .location .host - get property via eval?
;;   - send js from backend
;;   - send js via websockets
;;   - use hashing for .location .host
;;   - no lists of hosts
;;   - append/prepend random string for hashed/unhashed
;;   - indirect result, no direct exceptions
;;   - call verification from several places
;;   - use https://utf-8.jp/public/jjencode.html
;;   - use sockets https://stackoverflow.com/questions/1660060/how-to-prevent-your-javascript-code-from-being-stolen-copied-and-viewed
;;   - !!! You can use an ajax script injection. This is deters theft because the same domain policy which prevents XSS will make the client side script difficult to run elsewhere.
;;
;; - check https://www.goodoldtetris.com
;; - icons https://icones.js.org
;; - color schemes to choose
;;
;; stealing precaution: hostname and verify what is visible in the obfuscated code
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