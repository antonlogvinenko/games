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

; https://tetris.wiki/Tetris_(NES)
(def fps 60.0988)
(def levels {0 48 1 43 2 38 3 33 4 28 5 23 6 18 7 13 8 8 9 6 10 5 11 5 12 5 13 4 14 4 15 4 16 3 17 3 18 3 19 2})
(defn sleep-ms-at-level [level]
  (* 1000 (/ (levels level) fps)))

;; -- Game state
(def game-state (atom {:timeout-ms (sleep-ms-at-level 0)}))

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
(def next-item-height 2)
(def next-item-width 4)

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




(defn filled? [x]
  (== x 1))

(defn for-xs [tt x]
  (fn [y]
    (at-tt tt x y)))

(defn for-ys [tt y]
  (fn [x]
    (at-tt tt x y)))

(defn get-filled [for-dimension ts tt]
  ((juxt first last)
   (for [d (range 0 ts)
         :let [filled (->> (range 0 ts)
                           (map (for-dimension tt d))
                           (filter filled?)
                           empty?
                           not)]
         :when filled]
     d)))

(defn calculate-x-start [width tsys id]
  (let [[tt ts] (get-tt tsys id 0)
        [xl xr] (get-filled for-xs ts tt)]
    (- (int (/ width 2)) (int (/ (+ xr 1 (- xl)) 2)))))

(defn calculate-y-start [height tsys id]
  (let [[tt ts] (get-tt tsys id 0)
        [yb yt] (get-filled for-ys ts tt)]
    ; split the field in half, fill the left part with the "least half"
    ; -yb, so that at least one row is visible
    (- height 1 yb)))

(defn create-empty-matrix [height width]
  (vec (repeat height (vec (repeat width 0)))))



(defn init-state [{height :height width :width ticking :ticking tsys :tsys} refs next-elem]
  (let [tt-gen (create-tt-gen)
        id (first-gen tt-gen)]
    {:stop      #()
     :height    height
     :width     width
     :refs      refs
     :next-elem next-elem
     :x         (calculate-x-start width tsys id)
     ;; inc-ed so that it is displayed on first :descend event
     :y         (inc (calculate-y-start height tsys id))
     :tid       id
     :tform     0
     :tt-gen    (rest-gen tt-gen)
     :tsys      ::super
     :game-over false
     :ticking   ticking
     :level     0
     :cleared   0
     :score     0
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
  (let [square-px 25
        sizer (fn [items] (str (* items square-px) "px"))
        table-style (props (merge table-props {"height" (sizer height)
                                               "width"  (sizer width)
                                               ;"margin" "auto"
                                               "border" "0px solid black"}))
        ;"border-collapse" "collapse"})
        create-row (fn [row] (into [:tr]
                                   (map (fn [col] [:td {:id (cell-id id-prefix row col) :style "border: 1px solid grey"} ""])
                                        (range width))))]
    [:div (into [:table {:width "300px" :style table-style}]
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

(defn button-props [m]
  (props
    (merge m
           {"touch-action"        "none"
            "text-align"          "center"
            "-moz-user-select"    "-moz-none"
            "-khtml-user-select"  "none"
            "-webkit-user-select" "none"
            "-o-user-select"      "none"
            "user-select"         "none"})))

(def default-parameters (create-parameters 20 10 2000))

(defn render-control-buttons []
  [:table {:width "270px" :style "margin: auto"}
   [:tr
    [:td {:id "move-left-btn" :style (button-props {"font-size" "37px"})} "&larr;"]
    [:td {:id "rotate-left-btn" :style (button-props {"font-size" "45px"})} "&#10226;"]
    [:td {:id "rotate-right-btn" :style (button-props {"font-size" "45px"})} "&#10227;"]
    [:td {:id "move-right-btn" :style (button-props {"font-size" "37px"})} "&rarr;"]]])


(defn button [id text]
  [:button {:id id :style (props {"border-radius"    "4px;"
                                  "background-color" "#e7e7e7"
                                  "border"           "none"
                                  "color"            "black"
                                  "padding"          "15px 16px"
                                  "text-align"       "center"
                                  "text-decoration"  "none"
                                  "display"          "inline-block"
                                  "width"            "100%"
                                  "font-size"        "14px"})} text])

(defn render-right-column []
  [:div {:style (props {})}
   (render-table "next-elem" next-item-height next-item-width nil)
   [:div {:style (props {"height" "110px"})}]
   [:div {:style (props {"height" "80px"})} (button "score-field" "Score: 0")]
   [:div {:style (props {"height" "80px"})} (button "level-field" "Level: 0")]
   ;[:div {:style (props {"height" "80px" })} (button "controls-field" "Controls")]
   [:div {:style (props {"height" "80px"})} (button "cleared-lines-field" "Cleans: 0")]
   [:div {:style (props {"height" "80px" "text-align" "center"})}
    (button "new-game-btn" "New game")]])

(def description "Tetris is a puzzle video game with a consistent general design across its numerous versions. Gameplay consists of a rectangular field in which tetromino pieces, geometric shapes consisting of four connected squares, descend from the top-center. During the descent, the player can move the piece horizontally and rotate them until they touch the bottom of the field or another piece. The player's goal is to stack the pieces in the field to create horizontal lines of blocks. When a line is completed, it disappears, and the blocks placed above fall one row. As lines are cleared, the speed of the descending pieces increase. The game ends if the accumulated pieces in the field block other pieces from entering the field, a process known as \"topping out\". Common mechanics among Tetris variants include soft drop (the ability to increase the descent of the piece), hard drop (instantly placing the piece as far down as it can go), and holding (reserving a piece for later use).")
(def description2 "The objective of Tetris is to collect as many points as possible during a gameplay session by clearing lines. Tetris's scoring system has remained mostly consistent since Tetris DS (2006) with some exceptions. Points gained during gameplay increase with the descent speed. The more lines cleared at once, the higher the score for a line clear; clearing four lines at once using an I-piece is referred to as a \"Tetris\". The player can also gain points by using hard drops or soft drops. There are advanced techniques that can gain more points than a Tetris, including T-spins (spinning a T-piece into a blocked gap), perfect clears (emptying the field following a line clear), and combos (clearing lines with multiple pieces in a row).")

(defn render-game! [{height :height width :width}]
  (set-game-html!
    (hiccups/html
      [:div
       [:table {:width "1000px" :style "margin: auto; margin-left: 50px"}
        ;[:tr
        ; [:td {:style (props {"text-align" "center"})}
        ;  [:div {:id "game-message"}]]
        ; [:td]]
        [:tr
         [:td {:style (props {"vertical-align" "top" "width" "500px"})}
          [:p description] [:p description2]]
         [:td {:style (props {"width" "100px"})}]
         [:td {:style (props {"vertical-align" "top"})}
          (render-table "field" height width {"margin" "auto" "margin-right" "0px"})]
         [:td {:style (props {"vertical-align" "top"})}
          (render-right-column)]]
        [:tr
         [:td {:style "width: 500px"} ""]
         [:td {:style (props {"width" "100px"})}]
         [:td (render-control-buttons)]
         [:td {:id "complete-btn" :style (button-props {"font-size" "30px" "font-weight" "normal"})} "&darr;"]]]]))
  [(get-rendered-references! "field" height width)
   (get-rendered-references! "next-elem" next-item-height next-item-width)])



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

(defn merge-element [state]
  (merge state {:field (add-element-to-field state)}))

(defn game-over! [{stop :stop :as state}]
  (stop)
  ;(game-over-message!)
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



(defn calculate-level [cleared]
  (quot cleared 10))

(defn calculate-score [score cleared]
  (+ score (* 10 cleared)))

; score <- cleared -> level -> speed
; https://tetris.wiki/Tetris_(NES)
; more advanced details:
; cleared + started at level -> level
; cleared + how much at once -> score
(defn do-clear-candidates [{height  :height
                            width   :width
                            field   :field
                            cleared :cleared
                            score   :score
                            :as     state}]
  (if-let [[low-y high-y] (get-clear-candidates state)]
    (let [new-cleared (inc (- high-y low-y))
          overall-cleared (+ cleared new-cleared)
          new-level (calculate-level overall-cleared)
          new-score (calculate-score score new-cleared)]
      (assoc state
        :level new-level
        :cleared overall-cleared
        :score new-score
        :field (vec (concat
                      (when (pos? low-y) (subvec field 0 low-y))
                      (when (< high-y height) (subvec field (inc high-y) height))
                      (create-empty-matrix new-cleared width)))))
    state))



(defn next-or-game-over! [{width  :width
                           height :height
                           tsys   :tsys
                           tt-gen :tt-gen
                           :as    state}]
  (let [id (first-gen tt-gen)
        next-tt-gen (rest-gen tt-gen)
        new-state (merge state {:x      (calculate-x-start width tsys id)
                                :y      (calculate-y-start height tsys id)
                                :tid    id
                                :tt-gen next-tt-gen
                                :tform  0})]
    (if (is-acceptable new-state)
      new-state
      nil)))

;; a game tick does one of three:
;; - descend
;; - game over
;; - merge + clear line if present? + descend next element + generate new next tt
;; important: merge is done separately from the previous descend because the element
;; must be able to move left and right before the last descend and the merge
(defn game-tick-handler [state]
  (let [distance (how-much-can-descend 1 state)]
    (if (pos? distance)
      (descend distance state)
      (let [new-state (-> state
                          merge-element
                          do-clear-candidates
                          next-or-game-over!)]
        (if new-state new-state (game-over! state))))))

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
    ;; current state is the last one since no states might be acceptable
    (->> [rotated (move-left rotated) (move-right rotated) state]
         (filter is-acceptable)
         first)))

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
  (log "Starting actor" id)
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
(def next-element-empty-space (repeat next-item-height (repeat next-item-width 0)))
(defn add-element-to-next [elem]
  (let [ts (count elem)
        [xl xr] (get-filled for-xs ts elem)
        [yb yt] (get-filled for-ys ts elem)]
    ;; + next-item-* so that 2x4 is always filled with 0 or 1 so that the diff can be calculated
    (for [y (range yb (max (inc yt) (+ next-item-height yb)))]
      (for [x (range xl (max (inc xr) (+ next-item-width xl)))]
        (at-tt elem x y)))))

(defn generate-scene [{tt-gen :tt-gen tsys :tsys cleared :cleared score :score level :level :as state}]
  {:cleared   cleared
   :level     level
   :score     score
   :field     (add-element-to-field state)
   :next-elem (add-element-to-next (first (get-tt tsys (first-gen tt-gen) 0)))})

(defonce game (atom {:stop #()}))
(defn stop! []
  ((:stop @game)))


(defn get-location [] js/window.location.host)

(defn verification []
  (let [host (-> (get-location) (str/split ":") first md5)
        allowed-hosts [
                       "8561520c9e13eeb5b228e32c163800c1"
                       "1669c40770a55e175923e6c5ed064cd0"
                       "db6d8dce28da168c3546ca7358c91d7a"
                       "0807f8526a95151dd52eef93cdc47876"]]
    (when
      (not-any? #{host} allowed-hosts)
      (throw (js/Error. "Oops!")))))

(defn update-cleared [cleared]
  (set-value! "cleared-lines-field" (str "Cleans: " cleared)))

(defn update-score [score]
  (set-value! "score-field" (str "Score: " score)))

(defn update-level [level]
  (set-value! "level-field" (str "Level: " level)))

(defn update-speed [level]
  (swap! game-state update-in [:timeout-ms] (fn [x] (sleep-ms-at-level level))))

(defn update-colors [target diff]
  (dorun (for [{x :x y :y color :color} diff]
           (do (log "Setting color" x y color)
               (set-color! target x y color)))))

(defn create-button-listeners [action-ch restart-game]
  (doseq [[name event] {"move-left-btn"    ::move-left
                        "move-right-btn"   ::move-right
                        "rotate-left-btn"  ::rotate-left
                        "rotate-right-btn" ::rotate-right
                        "complete-btn"     ::complete}]
    (gevents/listen (gdom/getElement name) "click" #(put! action-ch event)))
  (gevents/listen (gdom/getElement "new-game-btn") "click" restart-game))


(defn start! [parameters]
  (stop!)
  (let [
        timed-ch (default-ch)
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
                                   (->> [timed-ch kbd-ch chord-ch action-ch scene-ch
                                         renderer-calculator-ch renderer-ch]
                                        (map (fn [ch] (put! ch :quit)))
                                        dorun)))]
    (create-button-listeners action-ch #(do (:stop state) (start! default-parameters)))
    ;(game-started-message!)

    (verification)

    (actor "renderer"
           false
           renderer-ch
           null-inbox
           {}
           (fn [{{field-diff     :field
                  next-elem-diff :next-elem
                  cleared        :cleared
                  level          :level
                  score          :score} :msg}]
             (update-speed level)
             (update-level level)
             (update-score score)
             (update-cleared cleared)
             (update-colors field field-diff)
             (update-colors next-elem next-elem-diff)))

    (actor "renderer calculator"
           false
           renderer-calculator-ch
           renderer-ch
           {:field     (:field state)
            :next-elem next-element-empty-space}
           (fn [{{field-1 :field next-elem-1 :next-elem} :state
                 {field-2 :field next-elem-2 :next-elem
                  cleared :cleared
                  level   :level
                  score   :score :as state-2}            :msg}]
             {:msg   {:cleared   cleared
                      :level     level
                      :score     score
                      :field     (field-diff field-1 field-2)
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

    (let [id "ticker"]
      (log "Starting actor" id)
      (go-loop []
               (let [[v _] (alts! [timed-ch (timeout (:timeout-ms @game-state))])]
                 (if (= v :quit)
                   (log "Quitting" id)
                   (do (>! action-ch :game-tick)
                       (recur))))))

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
                                      (->> [timed-ch kbd-ch chord-ch action-ch scene-ch
                                            renderer-calculator-ch renderer-ch]
                                           (map (fn [ch] (put! ch :quit)))
                                           dorun))))
    nil))


(start! default-parameters)

