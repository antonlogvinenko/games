(ns gg.tetris-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [gg.tetris :as t :refer []]))

; docs
;https://cljs.github.io/api/cljs.test/
;https://figwheel.org/config-options

(deftest at-test
  (is (== 42 (t/at [[0 0 0] [0 42 0] [0 0 0]] 1 1)) "get the element"))

(deftest at?-test
  (is (true? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 1 1)) "element is present")
  (is (true? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 0 0)) "element is present")
  (is (true? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 2 2)) "element is present")

  (is (false? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 3 0)) "element is absent")
  (is (false? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 0 3)) "element is absent")
  (is (false? (t/at? [[0 0 0] [0 42 0] [0 0 0]] -1 0)) "element is absent")
  (is (false? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 0 -1)) "element is absent")

  (is (false? (t/at? [[0 0 0] [0 42 0] [0 0 0]] 3 3)) "element is absent"))

(deftest indexed-test
  (is (== [] (t/indexed [])) "empty index")
  (is (== [[0 100] [1 101] [2 102]] (t/indexed [100 101 102])) "non empty index"))

(deftest zip-test
  (is (== [] (t/zip [] [])) "empty zip")
  (is (== [[1 'a'] [2 'b'] [3 'c']] (t/zip [1 2 3] ['a' 'b' 'c'])) "non empty zip")
  (is (== [[1 'a'] [2 'b'] [3 'c']] (t/zip [1 2 3] ['a' 'b' 'c' 'd'])) "non empty zip")
  (is (== [[1 'a'] [2 'b'] [3 'c']] (t/zip [1 2 3 4] ['a' 'b' 'c'])) "non empty zip"))

(deftest element-start-x-test
  (is (= 4 (t/element-start-x 10 3))))

(deftest add-element-to-the-field
  (is (= [[0 0 0] [0 1 0] [0 1 1]]
         (t/add-element-to-field {:x       1 :y 1
                                  :element {:height 2 :width 2 :shape [[1 0] [1 1]]}
                                  :field   [[0 0 0] [0 0 0] [0 0 0]]}))
      "add big element to the field")

  (is (= [[0 0 0] [0 1 0] [0 0 0]]
         (t/add-element-to-field {:x       1 :y 1
                                  :element {:height 1 :width 1 :shape [[1]]}
                                  :field   [[0 0 0] [0 0 0] [0 0 0]]}))
      "add small element to the field")

  (is (= [[0 0 0] [0 0 0] [0 0 0]]
         (t/add-element-to-field {:x       1 :y 1
                                  :element {:height 0 :width 0 :shape []}
                                  :field   [[0 0 0] [0 0 0] [0 0 0]]}))
      "add nothing to the field")


  (is (= [[1 0 0] [1 1 1] [0 0 1]]
         (t/add-element-to-field {:x       0 :y 0
                                  :element {:height 3 :width 3 :shape [[1 0 0] [1 1 1] [0 0 1]]}
                                  :field   [[0 0 0] [0 0 0] [0 0 0]]}))
      "add complex element to the field")

  (is (= [[0 0 0 0] [0 1 0 0] [0 1 1 1] [0 0 0 1]]
         (t/add-element-to-field {:x       1 :y 1
                                  :element {:height 3 :width 3 :shape [[1 0 0] [1 1 1] [0 0 1]]}
                                  :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "add complex element to the bigger field"))


(deftest props-test
  (is (== "a:1; b:2" (t/props {"a" 1 "b" 2})))
  (is (== "a:1" (t/props {"a" 1})))
  (is (== "" (t/props {}))))

(deftest cell-id-test
  (is (== "cell:1:2" (t/cell-id 1 2))))

(deftest field-diff
  (is (empty?
        (t/field-diff [] []))
      "empty field")

  (is (empty?
        (t/field-diff [[0 0 0] [0 0 0] [0 0 0]] [[0 0 0] [0 0 0] [0 0 0]]))
      "empty result")

  (is (=
        [(t/FieldDiff. 1 1 0) (t/FieldDiff. 2 2 1)]
        (t/field-diff [[0 0 0] [0 1 0] [0 0 0]] [[0 0 0] [0 0 0] [0 0 1]]))
      "non empty result"))

(deftest kbd-interpreter
  (is (= :gg.tetris/rotate-right
         (t/interpret-kbd-input {:modifiers [] :pressed "w"}))
      "wasd rotate right")

  (is (= :gg.tetris/rotate-left
         (t/interpret-kbd-input {:modifiers ["shift"] :pressed "w"}))
      "wasd rotate left")

  (is (= :gg.tetris/complete
         (t/interpret-kbd-input {:modifiers [] :pressed "s"}))
      "wasd complete")

  (is (= :gg.tetris/move-left
         (t/interpret-kbd-input {:modifiers [] :pressed "a"}))
      "wasd left")

  (is (= :gg.tetris/move-right
         (t/interpret-kbd-input {:modifiers [] :pressed "d"}))
      "wasd right")



  (is (= :gg.tetris/rotate-right
         (t/interpret-kbd-input {:modifiers [] :pressed "arrowup"}))
      "arrows rotate right")

  (is (= :gg.tetris/rotate-left
         (t/interpret-kbd-input {:modifiers ["shift"] :pressed "arrowup"}))
      "arrows rotate left")

  (is (= :gg.tetris/complete
         (t/interpret-kbd-input {:modifiers [] :pressed "arrowdown"}))
      "arrows complete")

  (is (= :gg.tetris/move-left
         (t/interpret-kbd-input {:modifiers [] :pressed "arrowleft"}))
      "arrows left")

  (is (= :gg.tetris/move-right
         (t/interpret-kbd-input {:modifiers [] :pressed "arrowright"}))
      "arrows right")



  (is (= :gg.tetris/complete
         (t/interpret-kbd-input {:modifiers [] :pressed " "}))
      "space complete")

  (is (= :gg.tetris/rotate-right
         (t/interpret-kbd-input {:modifiers [] :pressed "enter"}))
      "enter rotate right")

  (is (= :gg.tetris/rotate-left
         (t/interpret-kbd-input {:modifiers ["shift"] :pressed "enter"}))
      "enter rotate left"))


(deftest is-acceptable-test
  (is (true? (t/is-acceptable {:x       1 :y 1
                               :height  4 :width 4
                               :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                               :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "no intersection")

  (is (false? (t/is-acceptable {:x       1 :y 1
                                :height  4 :width 4
                                :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                :field   [[0 1 1 0] [0 1 0 0] [0 0 0 0] [0 0 0 0]]}))
      "intersection")

  (is (false? (t/is-acceptable {:x       -1 :y 1
                                :height  4 :width 4
                                :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved left too far")

  (is (false? (t/is-acceptable {:x       4 :y 1
                                :height  4 :width 4
                                :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved right too far")

  (is (false? (t/is-acceptable {:x       4 :y -1
                                :height  4 :width 4
                                :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "moved too low"))


(deftest how-much-can-descend-test
  (is (== 4 (t/how-much-can-descend 100 {:x       1 :y 4
                                         :height  4 :width 4
                                         :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                         :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "descend from start")

  (is (== 4 (t/how-much-can-descend 4 {:x       1 :y 4
                                       :height  4 :width 4
                                       :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                       :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "descend from start, real request")

  (is (== 2 (t/how-much-can-descend 100 {:x       2 :y 2
                                         :height  4 :width 4
                                         :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                         :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "descend in the middle")

  (is (== 1 (t/how-much-can-descend 1 {:x       2 :y 2
                                       :height  4 :width 4
                                       :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                       :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "descend in the middle, real request"))


(deftest add-element-to-the-field-test
  (is (== [[0 0 0 0] [0 1 1 0] [0 1 1 0] [0 0 0 0]] (t/add-element-to-field {:x       1 :y 1
                                                                             :height  4 :width 4
                                                                             :element {:width 2 :height 2 :shape [[1 1] [1 1]]}
                                                                             :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "add square")

  (is (== [[0 0 0 0] [0 1 1 0] [0 1 0 0] [0 1 0 0]] (t/add-element-to-field {:x       1 :y 1
                                                                             :height  4 :width 4
                                                                             :element {:width 2 :height 3 :shape [[1 1] [1 0] [1 0]]}
                                                                             :field   [[0 0 0 0] [0 0 0 0] [0 0 0 0] [0 0 0 0]]}))
      "add L"))
