package edi.md.cookmonitor.Obejcts;
import java.util.List;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.LineOrdersList;

/**
 * Created by Igor on 20.05.2020
 */

public class Order {
    private List<LineOrdersList> lines = null;
    private Integer number;
    private Integer state;
    private String uid;
    private long dateCreated;
    private long dateStarted;
    private long deliveryPlannedDate;
    private Integer deliveryType;
    private boolean isDone;

    public List<LineOrdersList> getLines() {
        return lines;
    }

    public void setLines(List<LineOrdersList> lines) {
        this.lines = lines;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(long dateStarted) {
        this.dateStarted = dateStarted;
    }

    public long getDeliveryPlannedDate() {
        return deliveryPlannedDate;
    }

    public void setDeliveryPlannedDate(long deliveryPlannedDate) {
        this.deliveryPlannedDate = deliveryPlannedDate;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
