package com.markone.reminder.ui.reminder;

import com.markone.reminder.Common;

import java.io.Serializable;

/**
 * Created by Neeraj on 03-Nov-19
 */
public class Reminder implements Serializable {

    private String id = "";
    private String name = "";
    private String details = "";
    private Common.Status status = Common.Status.Low;
    private Common.Frequency frequency = Common.Frequency.Once;

    // start
    private int startDate_Year;
    private int startDate_Month;
    private int startDate_Day;
    private int startDate_Hour;
    private int startDate_Minute;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Common.Status getStatus() {
        return status;
    }

    public void setStatus(Common.Status status) {
        this.status = status;
    }

    public int getStartDate_Year() {
        return startDate_Year;
    }

    public void setStartDate_Year(int startDate_Year) {
        this.startDate_Year = startDate_Year;
    }

    public int getStartDate_Month() {
        return startDate_Month;
    }

    public void setStartDate_Month(int startDate_Month) {
        this.startDate_Month = startDate_Month;
    }

    public int getStartDate_Day() {
        return startDate_Day;
    }

    public void setStartDate_Day(int startDate_Day) {
        this.startDate_Day = startDate_Day;
    }

    public int getStartDate_Hour() {
        return startDate_Hour;
    }

    public void setStartDate_Hour(int startDate_Hour) {
        this.startDate_Hour = startDate_Hour;
    }

    public int getStartDate_Minute() {
        return startDate_Minute;
    }

    public void setStartDate_Minute(int startDate_Minute) {
        this.startDate_Minute = startDate_Minute;
    }

    public Common.Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Common.Frequency frequency) {
        this.frequency = frequency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
