(ns gg.tetris-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [gg.tetris :as t :refer []]))

; docs
;https://cljs.github.io/api/cljs.test/
;https://figwheel.org/config-options



(deftest next-or-game-over!-test
  (is (== (t/next-or-game-over! {:height 4
                                 :width  4
                                 :tsys   ::t/super
                                 :field  [[1 1 1 1] [1 1 1 1] [1 1 1 1] [1 1 1 1]]
                                 :tt-gen (fn [] [::t/L ::t/O])
                                 :stop   (fn [] (fn [] nil))})
          nil)
      "game over")
  (is (== (select-keys (t/next-or-game-over! {:height 4
                                              :width  4
                                              :tsys   ::t/super
                                              :field  [[1 1 1 1] [1 1 1 1] [1 1 1 1] [0 0 0 0]]
                                              :tt-gen (fn [] [::t/L ::t/O])
                                              :stop   (fn [] (fn [] nil))})
                       [:x :y :tid])
          {:x 1 :y 2 :tid ::t/L})
      "game continued"))

(deftest calculate-x-start-test
  (is (== 4 (t/calculate-x-start 10 ::t/super ::t/L)))
  (is (== 3 (t/calculate-x-start 10 ::t/super ::t/I)))
  (is (== 4 (t/calculate-x-start 10 ::t/super ::t/O))))

(deftest calculate-y-start-test
  (is (== (t/calculate-y-start 20 ::t/super ::t/L) 18))
  (is (== (t/calculate-y-start 20 ::t/super ::t/O) 19))
  (is (== (t/calculate-y-start 20 ::t/super ::t/I) 17)))

(deftest for-xs-test
  (is (== ((t/for-xs [[1 2 3] [4 5 6] [7 8 9]] 1) 2)
          8)))

(deftest for-ys-test
  (is (== ((t/for-ys [[1 2 3] [4 5 6] [7 8 9]] 1) 2)
          6)))

(deftest get-filled-test
  (is (= (t/get-filled t/for-xs 3 [[1 1 1] [0 0 1] [0 0 0]])
         [0 2])
      "all xs")

  (is (= (t/get-filled t/for-xs 3 [[0 1 1] [0 0 1] [0 0 0]])
         [1 2])
      "some xs")

  (is (= (t/get-filled t/for-ys 3 [[1 0 0] [0 1 0] [0 0 1]])
         [0 2])
      "all ys")

  (is (= (t/get-filled t/for-ys 3 [[0 0 0] [0 1 0] [0 0 1]])
         [1 2])
      "some ys"))

(deftest at-test
  (is (== (t/at-field [[0 0 0] [0 42 0] [0 0 0]] 1 1) 42) "get the element"))

(deftest at?-test
  (is (true? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 1 1)) "element is present")
  (is (true? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 0 0)) "element is present")
  (is (true? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 2 2)) "element is present")

  (is (false? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 3 0)) "element is absent")
  (is (false? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 0 3)) "element is absent")
  (is (false? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] -1 0)) "element is absent")
  (is (false? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 0 -1)) "element is absent")

  (is (false? (t/at-field? [[0 0 0] [0 42 0] [0 0 0]] 3 3)) "element is absent"))


(deftest get-clear-candidates-test
  (is (== (t/get-clear-candidates {:height 4 :field [[0 0 0 1] [1 0 0 0] [0 1 0 1] [1 0 1 0]]})
          nil))
  (is (= (t/get-clear-candidates {:height 4 :field [[1 1 1 1] [1 0 0 0] [0 1 0 1] [1 0 1 0]]})
         [0 0]))
  (is (= (t/get-clear-candidates {:height 4 :field [[1 1 1 1] [1 1 1 1] [1 1 1 1] [1 0 1 0]]})
         [0 2]))
  (is (= (t/get-clear-candidates {:height 4 :field [[0 0 0 0] [1 1 1 1] [1 1 1 1] [1 0 1 0]]})
         [1 2])))

(deftest do-clear-candidates-test
  (is (== (:field
            (t/do-clear-candidates {:height 4 :width 4 :field [[1 1 1 1] [1 0 0 0] [0 1 0 0] [0 0 1 0]]}))
          [[1 0 0 0] [0 1 0 0] [0 0 1 0] [0 0 0 0]])
      "has candidates")

  (is (== (:field
            (t/do-clear-candidates {:height 4 :width 4 :field [[1 0 0 0] [1 1 1 1] [1 1 1 1] [0 1 0 0]]}))
          [[1 0 0 0] [0 1 0 0] [0 0 0 0] [0 0 0 0]])
      "has no candidates"))

(deftest indexed-test
  (is (== (t/indexed []) [])
      "empty index")
  (is (== (t/indexed [100 101 102]) [[0 100] [1 101] [2 102]])
      "non empty index"))

(deftest zip-test
  (is (== (t/zip [] []) [])
      "empty zip")
  (is (== (t/zip [1 2 3] ['a' 'b' 'c']) [[1 'a'] [2 'b'] [3 'c']])
      "non empty zip")
  (is (== (t/zip [1 2 3] ['a' 'b' 'c' 'd']) [[1 'a'] [2 'b'] [3 'c']])
      "non empty zip")
  (is (== (t/zip [1 2 3 4] ['a' 'b' 'c']) [[1 'a'] [2 'b'] [3 'c']])
      "non empty zip"))

;(deftest element-start-x-test
;  (is (= 4 (t/element-start-x 10 3))))

(deftest add-element-to-the-field
  (is (= (t/add-element-to-field {:x     1 :y 0
                                  :tsys  ::t/super
                                  :tid   ::t/L
                                  :tform 0
                                  :field [[0 0 0 0] [0 0 0 0] [0 0 0 0]]})
         [[0 0 0 0] [0 1 1 1] [0 0 0 1]])
      "add big element to the field")

  (is (= (t/add-element-to-field {:x     0 :y 0
                                  :tsys  ::t/super
                                  :tid   ::t/J
                                  :tform 0
                                  :field [[0 0 0 0] [0 0 0 0] [0 0 0 0]]})
         [[0 0 0 0] [1 1 1 0] [1 0 0 0]])
      "add complex element to the field")

  (is (= (t/add-element-to-field {:x     1 :y 1
                                  :tsys  ::t/super
                                  :tid   ::t/Z
                                  :tform 0
                                  :field [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
         [[0 0 0 0] [0 0 0 0] [0 0 1 1] [0 1 1 0]])
      "add complex element to the bigger field"))


(deftest props-test
  (is (== (t/props {"a" 1 "b" 2}) "a:1; b:2"))
  (is (== (t/props {"a" 1}) "a:1"))
  (is (== (t/props {}) "")))

(deftest cell-id-test
  (is (== (t/cell-id "field" 1 2) "field:cell:1:2")))

(deftest field-diff
  (is (empty?
        (t/field-diff [] []))
      "empty field")
  (is (empty?
        (t/field-diff [[0 0 0] [0 0 0] [0 0 0]] [[0 0 0] [0 0 0] [0 0 0]]))
      "empty result")
  (is (=
        (t/field-diff [[0 0 0] [0 1 0] [0 0 0]] [[0 0 0] [0 0 0] [0 0 1]])
        [(t/FieldDiff. 1 1 0) (t/FieldDiff. 2 2 1)])
      "non empty result"))

(deftest kbd-interpreter
  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "w"})
         :gg.tetris/rotate-right)
      "wasd rotate right")

  (is (= (t/interpret-kbd-input {:modifiers ["shift"] :pressed "w"})
         :gg.tetris/rotate-left)
      "wasd rotate left")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "s"})
         :gg.tetris/complete)
      "wasd complete")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "a"})
         :gg.tetris/move-left)
      "wasd left")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "d"})
         :gg.tetris/move-right)
      "wasd right")


  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "arrowup"})
         :gg.tetris/rotate-right)
      "arrows rotate right")

  (is (= (t/interpret-kbd-input {:modifiers ["shift"] :pressed "arrowup"})
         :gg.tetris/rotate-left)
      "arrows rotate left")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "arrowdown"})
         :gg.tetris/complete)
      "arrows complete")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "arrowleft"})
         :gg.tetris/move-left)
      "arrows left")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "arrowright"})
         :gg.tetris/move-right)
      "arrows right")



  (is (= (t/interpret-kbd-input {:modifiers [] :pressed " "})
         :gg.tetris/complete)
      "space complete")

  (is (= (t/interpret-kbd-input {:modifiers [] :pressed "enter"})
         :gg.tetris/rotate-right)
      "enter rotate right")

  (is (= (t/interpret-kbd-input {:modifiers ["shift"] :pressed "enter"})
         :gg.tetris/rotate-left)
      "enter rotate left"))


(deftest is-acceptable-test
  (is (true? (t/is-acceptable {:x      1 :y 1
                               :height 4 :width 4
                               :tid    ::t/O
                               :tform  0
                               :tsys   ::t/super
                               :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "no intersection")

  (is (false? (t/is-acceptable {:x      1 :y 1
                                :height 4 :width 4
                                :tid    ::t/O
                                :tform  0
                                :tsys   ::t/super
                                :field  [[0 1 1 0] [0 1 0 0] [0 0 0 0] [0 0 0 0]]}))
      "intersection")

  (is (false? (t/is-acceptable {:x      -1 :y 1
                                :height 4 :width 4
                                :tid    ::t/O
                                :tform  0
                                :tsys   ::t/super
                                :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved left too far")

  (is (false? (t/is-acceptable {:x      4 :y 1
                                :height 4 :width 4
                                :tid    ::t/O
                                :tform  0
                                :tsys   ::t/super
                                :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved right too far")

  (is (false? (t/is-acceptable {:x      4 :y -1
                                :height 4 :width 4
                                :tid    ::t/O
                                :tform  0
                                :tsys   ::t/super
                                :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved too low"))


(deftest how-much-can-descend-test
  (is (== (t/how-much-can-descend 100 {:x      1 :y 4
                                       :height 4 :width 4
                                       :tid    ::t/O
                                       :tform  0
                                       :tsys   ::t/super
                                       :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          4)
      "descend from start")

  (is (== (t/how-much-can-descend 4 {:x      1 :y 4
                                     :height 4 :width 4
                                     :tid    ::t/O
                                     :tform  0
                                     :tsys   ::t/super
                                     :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          4)
      "descend from start, real request")

  (is (== (t/how-much-can-descend 100 {:x      2 :y 2
                                       :height 4 :width 4
                                       :tid    ::t/O
                                       :tform  0
                                       :tsys   ::t/super
                                       :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          2)
      "descend in the middle")

  (is (== (t/how-much-can-descend 1 {:x      2 :y 2
                                     :height 4 :width 4
                                     :tid    ::t/O
                                     :tform  0
                                     :tsys   ::t/super
                                     :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          1)
      "descend in the middle, real request"))


(deftest add-element-to-the-field-test
  (is (== (t/add-element-to-field {:x      1 :y 1
                                   :tsys   ::t/super :tid ::t/O :tform 0
                                   :height 4 :width 4
                                   :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          [[0 0 0 0] [0 1 1 0] [0 1 1 0] [0 0 0 0]])
      "add square")

  (is (== (t/add-element-to-field {:x      1 :y 1
                                   :height 4 :width 4
                                   :tsys   ::t/super :tid ::t/L :tform 1
                                   :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          [[0 0 0 0] [0 0 1 1] [0 0 1 0] [0 0 1 0]])
      "add L"))
;
(deftest merge-element-test
  (let [elem-generator (fn [] [::t/L])]
    (is (== (select-keys (t/merge-element {:x      1 :y 1
                                           :height 4 :width 4
                                           :tt-gen elem-generator
                                           :tsys   ::t/super :tid ::t/L :tform 0
                                           :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
                         [:x :y :height :width :tsys :tid :tform :field])
            {:x      1 :y 1
             :height 4 :width 4
             :tsys   ::t/super :tid ::t/L :tform 0
             :field  [[0 0 0 0] [0 0 0 0] [0 1 1 1] [0 0 0 1]]})
        "not merged")

    (is (== (select-keys (t/merge-element {:x      1 :y 0
                                           :height 4 :width 4
                                           :tt-gen elem-generator
                                           :tsys   ::t/super :tid ::t/L :tform 0
                                           :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
                         [:x :y :height :width :tsys :tid :tform :field])
            {:x      1 :y 0
             :height 4 :width 4
             :tsys   ::t/super :tid ::t/L :tform 0
             :field  [[0 0 0 0] [0 1 1 1] [0 0 0 1] [0 0 0 0]]})
        "merged")))

(deftest descend-test
  (is (== (t/descend 1 {:x      1 :y 1
                        :height 4 :width 4
                        :tid    ::t/O :tform 0
                        :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
          {:x      1 :y 0
           :height 4 :width 4
           :tid    ::t/O :tform 0
           :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]})
      "descended"))

(deftest change-state-test
  (let [acceptable-1 {:x      1 :y 1
                      :height 4 :width 4
                      :tsys   ::t/super :tid ::t/O :tform 0
                      :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        acceptable-2 {:x      1 :y 2
                      :height 4 :width 4
                      :tsys   ::t/super :tid ::t/O :tform 0
                      :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        unacceptable {:x      1 :y 1
                      :height 4 :width 4
                      :tsys   ::t/super :tid ::t/O :tform 0
                      :field  [[0 1 1 0] [0 1 1 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/try-new-state acceptable-1 unacceptable) acceptable-1)
        "unacceptable state rejected")

    (is (== (t/try-new-state acceptable-1 acceptable-2) acceptable-2)
        "new acceptable state accepted")))
;
(deftest move-left-test
  (let [to-move {:x      1 :y 2
                 :height 4 :width 4
                 :tsys   ::t/super :tid ::t/O :tform 0
                 :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        moved {:x      0 :y 2
               :height 4 :width 4
               :tsys   ::t/super :tid ::t/O :tform 0
               :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/move-left-action to-move) moved)
        "can move left")

    (is (== (t/move-left-action moved) moved)
        "cannot move left")))
;
(deftest move-right-test
  (let [to-move {:x      1 :y 2
                 :height 4 :width 4
                 :tsys   ::t/super :tid ::t/O :tform 0
                 :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        moved {:x      2 :y 2
               :height 4 :width 4
               :tsys   ::t/super :tid ::t/O :tform 0
               :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/move-right-action to-move) moved)
        "can move right")

    (is (== (t/move-right-action moved) moved)
        "cannot move right")))
;
(deftest rotate-right-test
  (let [to-rotate {:x      1 :y 1
                   :height 4 :width 4
                   :tsys   ::t/super :tid ::t/L :tform 0
                   :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        rotated {:x      1 :y 1
                 :height 4 :width 4
                 :tsys   ::t/super :tid ::t/L :tform 1
                 :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/rotate-right-action to-rotate) rotated)
        "rotate right")))

(deftest rotate-left-test
  (let [to-rotate {:x      1 :y 1
                   :height 4 :width 4
                   :tsys   ::t/super :tid ::t/L :tform 0
                   :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        rotated {:x      1 :y 1
                 :height 4 :width 4
                 :tsys   ::t/super :tid ::t/L :tform 3
                 :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/rotate-left-action to-rotate) rotated)
        "rotate left")))

(deftest action-handler-test
  (let [to-move {:x      1 :y 2
                 :height 4 :width 4
                 :tsys   ::t/super :tid ::t/O :tform 0
                 :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        moved {:x      0 :y 2
               :height 4 :width 4
               :tsys   ::t/super :tid ::t/O :tform 0
               :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}]
    (is (== (t/action-handler to-move ::t/move-left) moved))))

(deftest complete-test
  (let [next-element-fn (fn [] [::t/O])
        to-complete {:x      0 :y 4
                     :height 4 :width 4
                     :tsys   ::t/super :tid ::t/L :tform 0
                     :tt-gen next-element-fn
                     :field  [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}
        completed (t/complete-action to-complete)]
    (is (== (select-keys completed [:x :y :field])
            {:x 0 :y 2 :field [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))))

