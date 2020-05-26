
package edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrdersLinesList {

    @SerializedName("AssortimentName")
    @Expose
    private String assortimentName;
    @SerializedName("Comment")
    @Expose
    private String comment;
    @SerializedName("Count")
    @Expose
    private Double count;
    @SerializedName("MinutesLeft")
    @Expose
    private Integer minutesLeft;
    @SerializedName("Number")
    @Expose
    private Integer number;
    @SerializedName("PreparationRate")
    @Expose
    private Integer preparationRate;
    @SerializedName("State")
    @Expose
    private Integer state;
    @SerializedName("Uid")
    @Expose
    private String uid;

    public String getAssortimentName() {
        return assortimentName;
    }

    public void setAssortimentName(String assortimentName) {
        this.assortimentName = assortimentName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Integer getMinutesLeft() {
        return minutesLeft;
    }

    public void setMinutesLeft(Integer minutesLeft) {
        this.minutesLeft = minutesLeft;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getPreparationRate() {
        return preparationRate;
    }

    public void setPreparationRate(Integer preparationRate) {
        this.preparationRate = preparationRate;
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
}
