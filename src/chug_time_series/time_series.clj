;;;
;;; Models perfectly regular time series data.
;;;


(ns chug-time-series.time-series
  (:import [java.util Calendar]))


(def BAD-DATA :na)


(defn make-sample
  "Make a sample from a measurement and a time stamp."
  ([measurement time-stamp]
     [measurement time-stamp])
  ([measurement sample-time log-time]
     [measurement sample-time log-time]))


(defn has-saved-at-time? [sample]
  "Has this sample a saved at time stamp?"
  (= 3 (count sample)))


(defn log-stamp [sample]
  "Extract the time this sample was logged."
  (nth sample 2))


(defn measurement [sample]
  "Extract the measurement from this sample."
  (first sample))


(defn time-stamp [sample]
  "Extract the time stamp of this sample."
  (second sample))


(defn- replace-bad-data [bad-datum with-datum in-sample-data]
  "Replace bad-datum with-datum in-sample-data."
  (map #(if (= %1 bad-datum) BAD-DATA %1) in-sample-data))


(defn make-time-series
  "Make a time series."
  ;; This first expression captures the data common to all types of
  ;; time series.  It should not be called directly.
  ([the-name the-start the-period]
     {:name the-name
      :start the-start
      :period the-period})
  ([the-name the-start the-period the-bad-data measurements]
     (let [abstract-series (make-time-series the-name
                                             the-start
                                             the-period)]
       (-> abstract-series
           (assoc :tag :regular)
           (assoc :bad-value the-bad-data)
           (assoc :sample-data (replace-bad-data the-bad-data
                                                 BAD-DATA
                                                 measurements)))))
  ([the-name the-start the-period the-measurements]
     (let [abstract-series (make-time-series the-name
                                             the-start
                                             the-period)]
       (-> abstract-series
           (assoc :sample-data the-measurements)
           (assoc :tag (if (has-saved-at-time?
                            (first the-measurements))
                         :irregular
                         :almost-regular))))))


(defn series-name [ts]
  "Extract the name of this time series."
  (:name ts))


(defn sample-period [ts]
  "Extract the sample period of this time series."
  (:period ts))


(defn start-time [ts]
  "Extract the start time of this time series."
  (:start ts))


(defn- nth-period-from-start
  "Calculate the nth period from start."
  ([n period start]
     (doto (.clone start)
       (.add Calendar/SECOND (* n period))))
  ([n period start delta]
     (doto (nth-period-from-start n period start)
       (.add Calendar/MILLISECOND (* delta 1000)))))


(defn- make-samples 
  "Make a sequence of samples from sample-data."
  ([start period sample-data]
     (map-indexed
      (fn [n measurement]
        (make-sample measurement (nth-period-from-start n period start)))
      sample-data))
  ([start period sample-measurements time-deltas]
     (map
      (fn [n measurement delta]
	(make-sample measurement
		     (nth-period-from-start n period start delta)))
      (range (count sample-measurements)) sample-measurements time-deltas))
  ([start period sample-measurements time-deltas save-deltas]
     (map
      (fn [n measurement time-delta save-delta]
	(make-sample measurement
		     (nth-period-from-start n period start time-delta)
		     (nth-period-from-start n period start save-delta)))
      (range
       (count sample-measurements)) sample-measurements
       time-deltas save-deltas)))


(defmulti samples
  "Extract the samples for this time series."
  :tag)
(defmethod samples :regular [ts]
  (make-samples (start-time ts)
                (sample-period ts)
                (:sample-data ts)))
(defmethod samples :almost-regular [ts]
  (:sample-data ts))
(defmethod samples :irregular [ts]
  (:sample-data ts))


(defn sample-count
  "Return the number of samples in this time series."
  [ts]
  (count (samples ts)))

  