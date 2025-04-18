(ns gg.tetris
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [cljs.core.async :refer [go-loop go <! >! timeout chan put! alts!] :as async]
            [cljs.core :as core]
            [cljs.test :as test]
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


(derive ::descend ::action)
(derive ::complete ::action)
(derive ::move-left ::action)
(derive ::move-right ::action)
(derive ::rotate-left ::action)
(derive ::rotate-right ::action)


(def ^:dynamic *logging*)
(def LOG true)
(defn log [& objs]
  (when (and LOG *logging*)
    (println objs)))


(defn action-handler [game-state msg]
  (log "This is inside action handler" msg)
  game-state)

(defn listen [down-listener up-listener]
  (gevents/listen js/document "keydown" down-listener)
  (gevents/listen js/document "keyup" up-listener))

(defn create-kbd-ch []
  (let [kbd-inbox (chan)]
    (listen #(put! kbd-inbox {:action :down :key (.-key %)})
            #(put! kbd-inbox {:action :up :key (.-key %)}))
    kbd-inbox))





(defn render [last-displayed-state new-state]
  {})

;; fn : {:msg msg-in :state} -> {:msg msg-out :state state}
;; if msg-out is nil, the message is not sent to channel, only recur is performed
;; current state of the actor is replaced with the state that the function returns
(defn actor [id logging inbox outbox state fn]
  (println "Starting actor" id)
  (go-loop [state state]
           (binding [*logging* logging]
             (let [msg-in (<! inbox)]
               (log "[" id "]" "received a message" msg-in)
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


(defn cake [] (test/run-all-tests))

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


(defn run []
  (let [state (init-state 5 20)

        timed-ch-ctrl (default-ch)
        timed-ch (create-timed-ch timed-ch-ctrl 10000)
        kbd-ch (create-kbd-ch)
        null-inbox (default-ch)

        renderer-ch (default-ch)
        renderer-calculator-ch (default-ch)
        action-ch (default-ch)
        chord-ch (default-ch)]

    (actor "renderer"
           true
           renderer-ch
           null-inbox
           {}
           (fn [{msg :msg}] {:msg {}}))

    (actor "renderer calculator"
           false
           renderer-calculator-ch
           renderer-ch
           {}
           (fn [{last-displayed-state :state new-state-to-display :msg}]
             {:msg   (render last-displayed-state new-state-to-display)
              :state new-state-to-display}))

    (actor "action handler"
           true
           action-ch
           renderer-calculator-ch
           {}
           (fn [{state :state msg :msg}]
             (let [new-game-state (action-handler state msg)]
               {:msg new-game-state :state new-game-state})))

    (actor "ticker"
           true
           timed-ch
           action-ch
           {}
           (fn [{msg :msg}] {:msg :descend}))

    (actor "kbd interpreter"
           true
           chord-ch
           action-ch
           {}
           (fn [{input :msg}] {:msg (interpret-kbd-input input)}))

    (actor "kbd listener"
           true
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

    {:stop (fn []
             (->> [timed-ch-ctrl timed-ch kbd-ch chord-ch action-ch
                   renderer-calculator-ch renderer-ch]
                  (map (fn [ch] (put! ch :quit)))
                  dorun))}))

(defonce game (atom (run)))

(defn stop []
  ((:stop @game)))

(defn start []
  (stop)
  (reset! game (run)))

(start)




;; 1.
;; display a field in html
;; 2.
;; implement renderer
;; [{:x x :y y :color color} {:x x :y y :color color}]
;; 3.
;; implement diff calculator
;; 4.
;; action handler for :descend
;; 5.
;; action handler for :rotate
;; 6.
;; glue together to make game start, generate block, merge it, generate new, lose
;;
;;
;; stealing precaution: hostname and verify what is visible in the obfuscated code
;; domain name
;; use the domain name
;; robots.txt
;; seo
;;
;;
;;
;;
;; -1. google for more methods
;
;0. if check failed
;	- slow down the the browser
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



;; Running the whole app
(defn refresh! [state]
  (render-app! state)
  (attach-event-handlers! state))

(refresh! initial-state)