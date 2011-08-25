;; Implementation of time series in Clojure for the Clojure Houston
;; User Group.


(ns chug-time-series.time-series
  (:import (java.util Calendar GregorianCalendar)))


;; Utility functions - needed by the automated tests and the
;; implementation but not critical for learning about implementing an
;; object-oriented solution in Clojure.


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


;;
;; Empty interface implementation.
;;
(defn series-name 
  "Extract the name of this time series."
  [ts])


(defn sample-count
  "Return the number of samples in this time series."
  [ts]
  -1)


(defn sample-period
  "Extract the sample period of this time series."
   [ts]
   -1.0)


(defn start-time
  "Extract the start time of this time series."
   [ts]
   (GregorianCalendar. 1970 1 1 0 0 0))


(defn add-point
  "Add a new point to a time series."
  ([ts additional-sample])
  ([ts additional-value additional-time])
  ([ts additional-value additional-time additional-save]))

 
(defn samples
  "Extract the samples for this time series."
   [ts]
   )


(defn get-point
  "Return the specified point."
   [ts k])


(defn make-time-series
  "Make a time series."
  ([the-name the-start the-period])
  ([the-name the-start the-period the-bad-data measurements])
  ([the-name the-start the-period the-measurements]))

