(ns gg.tetris-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [gg.tetris :as t :refer []]))

; docs
;https://cljs.github.io/api/cljs.test/
;https://figwheel.org/config-options

(deftest field-diff
  (is (empty?
        (t/field-diff [] []))
      "empty field")

  (is (empty?
        (t/field-diff [[0 0 0] [0 0 0] [0 0 0]] [[0 0 0] [0 0 0] [0 0 0]]))
      "empty result")

  (is (=
        [(t/SetColor. 1 1 0) (t/SetColor. 2 2 1)]
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




