/**
 * Defines the interface used to read all time series data.
 */


package com.cjl.magistri;

import java.util.Calendar;


/**
 * Interface for time series data points.
 */
public interface DataPoint {
    public Calendar getTimePoint();
    public double getValue();
}


