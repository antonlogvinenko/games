(ns gg.tetris
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [cljs.core.async :refer [go-loop go <! >! timeout chan put! alts!] :as async]
            [clojure.string :as str]))



;; -- Game state
(def none 0)
(def filled 1)
(defn none? [cell] (== cell none))
(defn filled? [cell] (== cell filled))

(def no-element nil)
(defn has-element? [elem] (seq? elem))

(defrecord Coord [x y])

(defn init-state [xs ys]
  {:xs      xs
   :ys      ys
   :field   (replicate ys (replicate xs none))
   :element no-element})

(def settings (atom {:on true :logging true}))
(defn ctrl [& objs]
  (when (:logging @settings) (println objs))
  (if (:on @settings)
    nil
    (do (println "terminating")
        (throw (js/Error "terminated")))))


;; -- Map of channels
;; user actions: key pressed
;;    -> (chord-ch)
;;    -> kbd interpreter: keys pressed to user action
;;    -> (action-ch)
;; game ticker: descending cmd
;;    -> (action-ch)
;; (action-ch)
;;    -> engine: state + action = state
;;    -> (game-state-ch)
;;    -> render calculator: old_state + new_state = diff
;;    -> (render-ch)
;;    -> renderer: render the diff
(derive ::descend ::action)
(derive ::drop ::action)
(derive ::move-left ::action)
(derive ::move-right ::action)
(derive ::rotate-left ::action)
(derive ::rotate-right ::action)

(defn start-ticker [action-ch interval-ms]
  (go-loop []
           (<! (timeout interval-ms))
           (ctrl "Game ticker" interval-ms)
           (>! action-ch :descend)
           (recur)))

(defn game-engine [game-state msg]
  (ctrl "Message is handled by the game engine" msg)
  game-state)

(defn start-game-engine [init-state action-ch game-state-ch]
  (go-loop [current-state init-state]
           (let [msg (<! action-ch)]
             (ctrl "Action handler" msg)
             (let [new-state (game-engine current-state msg)]
               (>! game-state-ch new-state)
               (recur new-state)))))





(defn start-kbd-listener [chord-ch]
  (let [is-modifier? #{"Control" "Meta" "Alt" "Shift"}
        keydown-ch (chan)
        keyup-ch (chan)]
    (gevents/listen js/document "keydown" #(put! keydown-ch (.-key %)))
    (gevents/listen js/document "keyup" #(put! keyup-ch (.-key %)))
    (go-loop [modifiers [] pressed nil]
             (when (and pressed)
               (>! chord-ch (conj modifiers pressed))
               (recur [] nil))
             (let [[key ch] (alts! [keydown-ch keyup-ch])]
               (condp = ch
                 keydown-ch (if (is-modifier? key)
                              (recur (conj modifiers key) pressed)
                              (recur modifiers key))
                 keyup-ch (if (is-modifier? key)
                            (recur (filterv #(not= % key) modifiers) pressed)
                            (recur modifiers nil)))))))

(defn start-kbd-interpreter [chord-ch action-ch]
  (go-loop []
           (let [input (<! chord-ch)]
             (ctrl "kbd interpreter" input)
             (>! action-ch input)
             (recur))))



(defn render [last-displayed-state new-state]
  {})

(defn start-render-calculator [init-state game-state-ch render-ch]
  (go-loop [last-displayed-state init-state]
           (let [new-state (<! game-state-ch)]
             (ctrl "Render calculator")
             (>! render-ch (render last-displayed-state new-state))
             (recur new-state))))

(defn start-render [render-ch]
  (go-loop []
           (let [cmd (<! render-ch)]
             (ctrl "Renderer" cmd)
             (recur))))

(defn start []
  (let [chord-ch (chan (async/sliding-buffer 10))
        action-ch (chan (async/sliding-buffer 10))
        game-state-ch (chan (async/sliding-buffer 10))
        render-ch (chan (async/sliding-buffer 10))
        state (init-state 5 20)]
    (swap! settings assoc :on true)
    (start-ticker action-ch 2000)
    (start-kbd-listener chord-ch)
    (start-kbd-interpreter chord-ch action-ch)
    (start-game-engine state action-ch game-state-ch)
    (start-render-calculator state game-state-ch render-ch)
    (start-render render-ch)
    state))

(defn stop []
  (swap! settings assoc :on false))

(defn restart []
  (stop)
  (start))

;; interpret keyboard to commands
;; render the field
;; field diff calculator
;; start the game
;; state + descending
;; state + rotation
;; state + arrival



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



;; Running the whole app
(defn refresh! [state]
  (render-app! state)
  (attach-event-handlers! state))

(refresh! initial-state)