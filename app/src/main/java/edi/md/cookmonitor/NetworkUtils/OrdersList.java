
package edi.md.cookmonitor.NetworkUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrdersList {

    @SerializedName("Number")
    @Expose
    private Integer number;
    @SerializedName("NumberS")
    @Expose
    private String numberS;
    @SerializedName("State")
    @Expose
    private Integer state;
    @SerializedName("Uid")
    @Expose
    private String uid;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getNumberS() {
        return numberS;
    }

    public void setNumberS(String numberS) {
        this.numberS = numberS;
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
