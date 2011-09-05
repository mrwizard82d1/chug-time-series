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
  ([the-name the-start the-period the-bad-data measurements]
     {:tag :regular
      :name the-name
      :start the-start
      :period the-period
      :bad-value the-bad-data
      :sample-data (replace-bad-data the-bad-data BAD-DATA measurements)})
  ([the-name the-start the-period the-measurements]
     (if (has-saved-at-time? (first the-measurements))
       {:tag :irregular
        :name the-name
        :start the-start
        :period the-period
        :sample-data the-measurements}
       {:tag :almost-regular
        :name the-name
        :start the-start
        :period the-period
        :sample-data the-measurements})))


(defn series-name [ts]
  "Extract the name of this time series."
  (:name ts))


(defn sample-period [ts]
  "Extract the sample period of this time series."
  (:period ts))


(defn start-time [ts]
  "Extract the start time of this time series."
  (:start ts))


(defn- make-samples 
  "Make a sequence of samples from sample-data."
  ([start period sample-data]
     (map-indexed
      (fn [n measurement]
        (let [sample-time (.clone start)]
          (.add sample-time Calendar/SECOND (* n period))
          (make-sample measurement sample-time)))
      sample-data))
  ([start period sample-measurements time-deltas]
     (map
      (fn [n measurement delta]
        (let [sample-time (.clone start)]
          (.add sample-time Calendar/SECOND (* n period))
          (.add sample-time Calendar/MILLISECOND (* delta 1000))
          (make-sample measurement sample-time)))
      (range (count sample-measurements)) sample-measurements time-deltas))
  ([start period sample-measurements time-deltas save-deltas]
     (map
      (fn [n measurement time-delta save-delta]
        (let [sample-time (.clone start)
              save-time (.clone start)]
          (doto sample-time
            (.add Calendar/SECOND (* n period))
            (.add Calendar/MILLISECOND (* time-delta 1000)))
          (doto save-time
            (.add Calendar/SECOND (* n period))
            (.add Calendar/MILLISECOND (* save-delta 1000)))
          (make-sample measurement sample-time save-time)))
      (range (count sample-measurements)) sample-measurements
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


(defn sample-count [ts]
  "Return the number of samples in this time series."
  (count (samples ts)))
