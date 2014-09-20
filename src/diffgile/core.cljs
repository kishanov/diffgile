(ns diffgile.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [figwheel.client :as fw]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(fw/watch-and-reload
  :jsload-callback (fn []
                     ;; (stop-and-start-my app)
                     ))


; App's State
; -----------

(def staff ["Yossarian" "Orr" "Dunbar" "Milo"])

(def task-types [{:key :story :title "Story" :class "task-story"}
                 {:key :bug :title "Bug" :class "task-bug"}
                 {:key :ai :title "Action Item" :class "task-ai"}])

(def task-states [{:key :open :title "Open"}
                  {:key :blocked :title "Blocked"}
                  {:key :in-progress :title "In Progress"}
                  {:key :done :title "Completed"}])

(def task-states-index (into {}
                             (map-indexed (fn [idx itm] [itm idx])
                                          (map :key task-states))))

(def date-range (mapv #(str "2014-09-" %) (range 10 30)))


(defn generate-activity
  [date-range task-states]
  (let [start (rand-int (dec (count date-range)))
        log (mapv (fn [date]
                    {:date date :status (rand-nth task-states)})
                  (drop start date-range))]

    (assoc-in log [0 :status] (task-states 0))))


; Generated list of sample tasks
; ------------------------------
(def tasks (mapv (fn [summary]
                   {:summary  summary
                    :type     (rand-nth task-types)
                    :assignee (rand-nth staff)
                    :log      (generate-activity date-range task-states)})
                 ["Imlpement Sign Up Page"
                  "Conifgure CI Server"
                  "Reflect & contemplate"
                  "Deploy OpenStack"
                  "Fix Issue #65"
                  "Design Doc for messaging system"
                  "database migration"
                  "Issue 1"
                  "Issue 2"
                  "Issue 3"
                  "She's got issues."
                  "Publish coverage report"]))



; Search Filter atom (reacts on changes in "Select" boxes)
; --------------------------------------------------------
(def search-filter (atom {:start-date (last date-range)
                          :end-date   (last date-range)}))



(defn filter-start-date
  [tasks start-date]
  (search-filter (fn [task]
                   (>= 0 (compare (-> task :log first :date) start-date)))
                 tasks))


(defn empty-row
  []
  (into [] (concat [:div.row]
                   (let [cols-count (count task-states)
                         col-grid-size (quot 12 cols-count)
                         div (keyword (str "div.col-md-" col-grid-size))]
                     (into [] (repeat cols-count [div ""]))))))


(defn set-cell-text
  "Magic Number 1 identifies a place in 'empty-row' function where to insert content for a div"
  []
  [row idx text]
  (assoc-in row [(inc idx) 1] text))


; Reagent Components
; ------------------
(defn board-header
  []
  (reduce (fn [acc [idx text]]
            (set-cell-text acc idx [:h3 text]))
          (empty-row) (map-indexed vector (map :title task-states))))


(defn task-box
  [task class]
  [:div {:class (clojure.string/join " " ["task" class])}
   [:div.task-icon
    [:span {:class "glyphicon glyphicon-tasks"}]]
   [:h4 (task :summary)]
   [:div.assignee [:span {:class "glyphicon glyphicon-user"}] (task :assignee)]])



(defn find-task-status
  [task date]
  (let [sr (filter #(= date (% :date)) (task :log))]
    (if (empty? sr)
      nil
      (get-in (last sr) [:status :key]))))


(defn task-row
  "A component which row with appropriate task box positioning AND ghost box positioning"
  [task]
  (fn []
    (let [last-status (find-task-status task (@search-filter :end-date))
          first-status (find-task-status task (@search-filter :start-date))
          last-box-position (set-cell-text (empty-row)
                                           (task-states-index last-status)
                                           (task-box task (get-in task [:type :class])))]

      (if (or (nil? last-status) (nil? first-status))
        (empty-row)
        (if (and (= last-status first-status))
          last-box-position
          (set-cell-text last-box-position
                         (task-states-index first-status)
                         (task-box task "task-old")))))))


(defn date-picker
  [label key date-range]
  (fn []
    [:div
     [:label label]
     [:select.form-control {:on-change (fn [e] (swap! search-filter assoc key (-> e .-target .-value)))}
      (mapcat (fn [date] [[:option {:key date}
                           date]])
              (reverse date-range))]]))

(defn board
  []
  (fn []
    [:div.row

     [:div.col-md-9
      [board-header]
      (for [t tasks] [task-row t])]

     [:div.col-md-3
      [:h3 "Filters"]
      [date-picker "Start Date" :start-date date-range]
      [date-picker "End Date" :end-date date-range]]]))


(reagent/render-component [board]
                          (.getElementById js/document "board"))
