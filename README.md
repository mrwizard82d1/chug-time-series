Object-Oriented Time Series
===========================

At one time, I implemented data sets that contained historical time
series. Each data set was composed of a set of individual time
series modeling one measurement. Not only did each data set have a
name but each individual series had a similar name.

Given this problem, the following Java code models an interface for a
time series::

    public interface TimeSeries {
        public String getName();
        public Calendar getStartTime();
        public TimePeriod getPeriod();
        public ArrayList<DataPoint> getSamples();
        public void AddPoint (DataPoint aNewPoint);
        public DataPoint GetPoint(int k);
    };

    public interface DataPoint {
        public double getValue();
        public Calendar getSampleTime();
    };

Implementing this interface does not produce a particularly
object-oriented solution. One can envision a single class that simply
implements this interface. The solution becomes much more interesting
when we consider how to create these series programatically.

Historically, data was available in a sanitized form. In this form,
data had a single period and every measurement had a time stamp that
was a exact multiple of that period. This regularity allowed
programmers to compress the data. In addition, although we had a
measurement at every time period, some of those values were "magic."
That is, legacy data used a sentinel value to indicating that we
actually had no data.  

Current technology in this particular domain now allows us to take
advantage of the actual sample times; in other words, we can take
advantage of the "jiggles" in the actual sample times. In addition,
current technology allows us to take advantage of invalid or missing
data. As a result, the specification preferred storing actual data
instead of the sanitized form.

Finally, we have some additional time series that we capture
irregularly. It still is a time series but it can be thought of as
"sparsely" or "nominally" regular. An example are laboratory samples
taken from a factory floor. Although the samples are taken "every six
hours", the variation from this period is measured in minutes and
hours instead of seconds and milliseconds. 

These different "input types" produce the object-oriented
characteristics of our solution. When we handle legacy data, we want
to take advantage of the regularity of the data. When we create a time
series from data captured now, we want "jiggly" data. Finally, we want
our code to handle our "irregular data." Most importantly, we want to
allow client objects to handle time series from these different
sources in the same way.

Given these constraints, you can probably already design a class
hierarchy:

  * TimeSeries (client interface)
    * AbstractTimeSeries (common implementation)
      * RegularTimeSeries (for regular, sanitized legacy data)
      * AlmostRegTimeSeries (for data collected with today's
        technology)
      * IrregularTimeSeries (for our sparse, "irregular" data)

My question, and hopefully yours, is, "How would we solve this problem
using Clojure?"

"Instructions"
==============

The file *test/chug_time_series/time_series.clj* contains unit
tests. Because I created these tests from my idea of how to build it,
they probably contain some "bias" on how to implement a solution.
