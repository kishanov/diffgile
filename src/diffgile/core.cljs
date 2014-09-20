(ns diffgile.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [figwheel.client :as fw]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
;; (defonce app-data (atom {}))

(println "Edits to this text should show up in your developer console.")

(fw/watch-and-reload
  :jsload-callback (fn []
                     ;; (stop-and-start-my app)
                     ))


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



;(defn expand-dates-range
;  [tasks]
;  (mapcat)
;
;  )


(def tasks [{:summary  "Imlpement Sign Up Page"
             :type     (rand-nth task-types)
             :assignee (rand-nth staff)
             :log      [{:date "2014-09-17" :status (task-states 0)}
                        {:date "2014-09-18" :status (rand-nth task-states)}
                        {:date "2014-09-19" :status (rand-nth task-states)}]}

            {:summary  "Configure CI server"
             :type     (rand-nth task-types)
             :assignee (rand-nth staff)
             :log      [{:date "2014-09-10" :status (task-states 0)}
                        {:date "2014-09-15" :status (rand-nth task-states)}
                        {:date "2014-09-18" :status (rand-nth task-states)}]}

            {:summary  "Reflect & contemlpate"
             :type     (rand-nth task-types)
             :assignee (rand-nth staff)
             :log      [{:date "2014-09-19" :status (task-states 0)}]}

            {:summary  "Conquer the World"
             :type     (rand-nth task-types)
             :assignee (rand-nth staff)
             :log      [{:date "2014-09-12" :status (task-states 0)}
                        {:date "2014-09-13" :status (rand-nth task-states)}
                        {:date "2014-09-14" :status (rand-nth task-states)}
                        {:date "2014-09-16" :status (rand-nth task-states)}
                        {:date "2014-09-19" :status (rand-nth task-states)}]}])



(defn empty-row
  []
  (into [] (concat [:div.row]
                   (let [cols-count (count task-states)
                         col-grid-size (quot 12 cols-count)
                         div (keyword (str "div.col-md-" col-grid-size))]
                     (into [] (repeat cols-count [div]))))))


(defn set-cell-text
  [row idx text]
  (let [cur-val (get-in row [(inc idx)])]
    (assoc-in row [(inc idx)] (conj cur-val text))))


(defn board-header
  []
  (reduce (fn [acc [idx text]]
            (set-cell-text acc idx [:h3 text]))
          (empty-row) (map-indexed vector (map :title task-states))))


(board-header)

(defn task-row
  [task]
  (let [last-status (-> task :log last :status :key)]
    (set-cell-text (empty-row)
                   (task-states-index last-status)
                   [:div {:class (clojure.string/join " " ["task" (get-in task [:type :class])])}
                    [:div.task-icon
                     [:span {:class "glyphicon glyphicon-tasks"}]]
                    [:h4 (task :summary)]
                    [:div.assignee [:span {:class "glyphicon glyphicon-user"}] (task :assignee)]
                    ])))


(defn board
  []
  [:div.row
   [:div.col-md-9
    [board-header]
    (for [t tasks] [task-row t])
    ]

   [:div.col-md-3
    [:h5 "Filters"]]

   ])


(reagent/render-component [board]
                          (.getElementById js/document "board"))
