
package edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrdersList {

    @SerializedName("Lines")
    @Expose
    private List<LineOrdersList> lines = null;
    @SerializedName("Number")
    @Expose
    private Integer number;
    @SerializedName("State")
    @Expose
    private Integer state;
    @SerializedName("Uid")
    @Expose
    private String uid;
    @SerializedName("DateCreated")
    @Expose
    private String dateCreated;
    @SerializedName("DateStarted")
    @Expose
    private String dateStarted;
    @SerializedName("DeliveryPlannedDate")
    @Expose
    private String deliveryPlannedDate;
    @SerializedName("DeliveryType")
    @Expose
    private Integer deliveryType;

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

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDeliveryPlannedDate() {
        return deliveryPlannedDate;
    }

    public void setDeliveryPlannedDate(String deliveryPlannedDate) {
        this.deliveryPlannedDate = deliveryPlannedDate;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }
}
