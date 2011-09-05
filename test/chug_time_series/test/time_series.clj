;; Unit tests for implementation of time series in Clojure for the
;; Clojure Houston User Group.


(ns chug-time-series.test.time-series
  (:use [chug-time-series.time-series])
  (:use [clojure.test])
  (:import [java.util Calendar GregorianCalendar]))
  

;; Utility functions to support testing.
;;
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


;; Unit tests.
;;
(deftest make-typical-sample
  (let [sample-time (GregorianCalendar. 1999 4 23 18 17 29)
	sample-measurement -1098.374
	sample (make-sample sample-measurement sample-time)]
    (is (= sample-measurement (measurement sample)))
    (is (= sample-time (time-stamp sample)))
    (is (not (has-saved-at-time? sample)))))


(deftest make-lab-sample
  (let [sample-time (GregorianCalendar. 1999 4 23 18 17 29)
	sample-measurement -1098.374
	log-time (let [t sample-time]
		    (.add t Calendar/SECOND 3793) t)
	sample (make-sample sample-measurement sample-time log-time)]
    (is (= sample-measurement (measurement sample)))
    (is (= sample-time (time-stamp sample)))
    (is (= log-time (log-stamp sample)))
    (is (has-saved-at-time? sample))))


(declare reg-expect-name reg-expect-start reg-expect-period
	 reg-expect-bad reg-sample-data reg-expect-samples
	 reg-time-series)
(declare areg-expect-name areg-expect-start areg-expect-period
	 areg-sample-data areg-sample-deltas areg-expect-samples
	 areg-time-series)
(declare irr-expect-name irr-expect-start irr-expect-period
	 irr-sample-data irr-sample-deltas irr-save-deltas
	 irr-expect-samples irr-time-series)


(deftest make-regular-query-regular
  (is (= reg-expect-name (series-name reg-time-series)))
  (is (= reg-expect-start (start-time reg-time-series)))
  (is (= reg-expect-period (sample-period reg-time-series)))
  (is (partial (complement every?) has-saved-at-time?)
      (samples reg-time-series))
  (is (= reg-expect-samples (samples reg-time-series))))


(deftest make-aregular-query-aregular
  (is (= areg-expect-name (series-name areg-time-series)))
  (is (= areg-expect-start (start-time areg-time-series)))
  (is (= areg-expect-period (sample-period areg-time-series)))
  (is (partial (complement every?) has-saved-at-time?)
      (samples areg-time-series))
  (is (= areg-expect-samples (samples areg-time-series))))


(deftest make-irregular-query-irregular
  (is (= irr-expect-name (series-name irr-time-series)))
  (is (= irr-expect-start (start-time irr-time-series)))
  (is (= irr-expect-period (sample-period irr-time-series)))
  (is (every? has-saved-at-time? (samples irr-time-series)))
  (is (= irr-expect-samples (samples irr-time-series))))


;; (deftest regular-add-point-get-point
;;   (let [k-value 44
;; 	k-end (sample-count reg-time-series)
;; 	additional-point (add-point reg-time-series k-value)
;; 	k-time (.clone (start-time additional-point))]
;;     (.add k-time Calendar/SECOND
;; 	  (* k-end (sample-period additional-point)))
;;     (is (= (inc k-end) (sample-count additional-point)))
;;     (is (= (make-sample k-value k-time)
;; 	   (get-point additional-point k-end)))))


;; (deftest aregular-add-point-get-point
;;   (let [k-value 44
;; 	k-end (sample-count areg-time-series)
;;         k-delta 4253                    ; millisecond
;;         k-time (doto (.clone (start-time areg-time-series))
;;                  (.add Calendar/MILLISECOND k-delta))
;; 	additional-point (add-point areg-time-series
;;                                     k-value k-time)]
;;     (is (= (inc k-end) (sample-count additional-point)))
;;     (is (= (make-sample k-value k-time)
;; 	   (get-point additional-point k-end)))))


;; (deftest irregular-add-point-get-point
;;   (let [k-value 44
;; 	k-end (sample-count irr-time-series)
;;         k-delta -2973095		; data delta millisecond
;;         k-time (doto (.clone (start-time irr-time-series))
;;                  (.add Calendar/MILLISECOND k-delta))
;; 	k-save-delta -3049903		; save delta milliseconds
;;  	k-save-time (doto (.clone (start-time irr-time-series))
;; 		      (.add Calendar/MILLISECOND k-save-delta))
;; 	additional-point (add-point irr-time-series
;;                                     k-value k-time k-save-time)]
;;     (is (= (inc k-end) (sample-count additional-point)))
;;     (is (= (make-sample k-value k-time k-save-time)
;; 	   (get-point additional-point k-end)))))


(defn regular-time-series-fixture [f]
  (binding [reg-expect-name "pharetra"
	    reg-expect-start (GregorianCalendar. 2011 7 25 13 18 22)
	    reg-expect-period 60
	    reg-expect-bad -9999.
	    reg-sample-data [2 3 5 8 -9999. 21]]
    (let [sample-times
	  (map
	   (fn [n]
	     (doto (.clone reg-expect-start)
	       (.add Calendar/SECOND (* n reg-expect-period))))
	   (range (count reg-sample-data)))
	  sample-measurements
	  (map #(if (= % reg-expect-bad) :na %) reg-sample-data)]
      (binding [reg-expect-samples (map #(vector %1 %2)
					sample-measurements sample-times)]
	(binding [reg-time-series (make-time-series reg-expect-name
						    reg-expect-start
						    reg-expect-period
						    reg-expect-bad
						    reg-sample-data)]
	  (f))))))


(defn aregular-time-series-fixture [f]
  (binding [areg-expect-name "pharetra"
	    areg-expect-start (GregorianCalendar. 2005 6 5 8 51 17)
	    areg-expect-period 60
	    areg-sample-data [2 3 5 8 :na 21]
	    areg-sample-deltas [0.567 3.335 -1.681 -3.375 -4.502]]
    (let [sample-times
	  (map (fn [n delta]
		 (doto (.clone areg-expect-start)
		   (.add Calendar/SECOND (* n areg-expect-period))
		   (.add Calendar/MILLISECOND (* delta 1000))))
	       (range (count areg-sample-data)) areg-sample-deltas)]
	  (binding [ areg-expect-samples (map #(vector %1 %2)
					      areg-sample-data sample-times)]
	    (binding [areg-time-series
		      (make-time-series areg-expect-name
					areg-expect-start
					areg-expect-period
					areg-expect-samples)]
	      (f))))))


(defn irregular-time-series-fixture [f]
  (binding [irr-expect-name "pharetra"
	    irr-expect-start (GregorianCalendar. 2006 11 7 13 20 52)
	    irr-expect-period (* 6 3600) ; six hours
	    irr-sample-data [2 3 5 8 BAD-DATA 21]
	    irr-sample-deltas [-269.671 1124.597 -2638.302
			       -1490.886 1160.05 173.414]
	    irr-save-deltas [6039.423 2923.674 3840.677
			     2196.422 5293.118 -6097.565]]
    (binding [irr-expect-samples (make-samples irr-expect-start
					       irr-expect-period
					       irr-sample-data
					       irr-sample-deltas
					       irr-save-deltas)]
      (binding [irr-time-series (make-time-series irr-expect-name
						  irr-expect-start
						  irr-expect-period
						  irr-expect-samples)]
	(f)))))


(use-fixtures :each
	      regular-time-series-fixture
	      aregular-time-series-fixture
	      irregular-time-series-fixture)


