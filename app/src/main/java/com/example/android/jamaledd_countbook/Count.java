package com.example.android.jamaledd_countbook;

import java.util.Calendar;

public class Count {

    private String name;
    private Calendar date;
    private int newValue;
    //private int initialValue;
    private String comment;

    public Count() {

    }

    public String getName() {
        if (name == null) return null;
        if (name.isEmpty()) return null;
        return name;
    }

    public Calendar getDate() {
        if (date == null) return null;
        return date;
    }

    public int getNewValue() { return newValue; }

    //public int getInitialValue() { return initialValue; }

    public String getComment() {
        if (comment == null) return null;
        return comment;
    }

    public void setName(String name) throws CountException {
        if (name.isEmpty()) {
            throw new CountException();
        }

        this.name = name;
    }

    public void setDate(Calendar date) { this.date = date; }

    public void setNewValue(int newValue) { this.newValue = newValue; }

    //public void setInitialValue(int initialValue) { this.initialValue = initialValue;}

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
