/**
 * Defines the interface used to read all time series data.
 */


package com.cjl.magistri;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Interface for a time series.
 */
public interface TimeSeries {
    public String getName();
    public int getCount();
    public Calendar getStart();
    public Calendar getEnd();
    public ArrayList<DataPoint> getSampleData();
    public DataPoint getPoint(int index);
    public DataPoint getPoint(Calendar timePoint);
}

