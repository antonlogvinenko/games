(ns gg.md5
  (:require
    [goog.dom :as gdom]
    [goog.events :as gevents]
    [goog.crypt :as crypt]
    [goog.crypt.Md5]))


(defn md5 [value]
  (println value)
  (crypt/byteArrayToHex
    (let [md5 (goog.crypt.Md5.)]
      (.update md5 (crypt/stringToUtf8ByteArray
                     (str "a329te)&^65483sfas" (interpose "e45WERGSRQ#" value) "234tl-0658sefhd49rtew")))
      (.digest md5))))
