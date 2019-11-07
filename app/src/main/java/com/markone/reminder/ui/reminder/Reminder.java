package com.markone.reminder.ui.reminder;

import com.markone.reminder.Common;

/**
 * Created by Neeraj on 03-Nov-19
 */
public class Reminder {

    private String id = "";
    private String name = "";
    private String details = "";
    private Common.Status status = Common.Status.None;
    private Common.Frequency frequency = Common.Frequency.None;

    // start
    private int startDate_Year;
    private int startDate_Month;
    private int startDate_Day;
    private int startDate_Hour;
    private int startDate_Minute;
    private int ampm;

    // frequency
    private int frequencyDays;
    private int frequencyHours;
    private int frequencyMinutes;

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

    public int getFrequencyDays() {
        return frequencyDays;
    }

    public void setFrequencyDays(int frequencyDays) {
        this.frequencyDays = frequencyDays;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public void setFrequencyHours(int frequencyHours) {
        this.frequencyHours = frequencyHours;
    }

    public int getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void setFrequencyMinutes(int frequencyMinutes) {
        this.frequencyMinutes = frequencyMinutes;
    }

    public Common.Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Common.Frequency frequency) {
        this.frequency = frequency;
    }


    public int getAmpm() {
        return ampm;
    }

    public void setAmpm(int ampm) {
        this.ampm = ampm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
