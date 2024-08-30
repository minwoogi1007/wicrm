package com.wio.crm.model;

public class History {
    private String csNote;
    private String prcNote;
    private String prcGubn;
    private String inDate;
    private String inTime;

    private String gubn;

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
    }

    public String getInDate() {
        return inDate;
    }

    public void setInDate(String inDate) {
        this.inDate = inDate;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getCsNote() {
        return csNote;
    }

    public void setCsNote(String csNote) {
        this.csNote = csNote;
    }

    public String getPrcNote() {
        return prcNote;
    }

    public void setPrcNote(String prcNote) {
        this.prcNote = prcNote;
    }

    public String getPrcGubn() {
        return prcGubn;
    }

    public void setPrcGubn(String prcGubn) {
        this.prcGubn = prcGubn;
    }
}
