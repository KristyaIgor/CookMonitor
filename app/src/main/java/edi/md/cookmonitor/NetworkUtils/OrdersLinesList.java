
package edi.md.cookmonitor.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrdersLinesList {

    @SerializedName("AssortimentName")
    @Expose
    private String assortimentName;
    @SerializedName("Count")
    @Expose
    private Double count;
    @SerializedName("DateCreated")
    @Expose
    private String dateCreated;
    @SerializedName("DateStarted")
    @Expose
    private Object dateStarted;
    @SerializedName("Delivery")
    @Expose
    private Boolean delivery;
    @SerializedName("DepartmentTypeName")
    @Expose
    private String departmentTypeName;
    @SerializedName("GroupIndex")
    @Expose
    private Integer groupIndex;
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

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Object getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Object dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public String getDepartmentTypeName() {
        return departmentTypeName;
    }

    public void setDepartmentTypeName(String departmentTypeName) {
        this.departmentTypeName = departmentTypeName;
    }

    public Integer getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Integer groupIndex) {
        this.groupIndex = groupIndex;
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
