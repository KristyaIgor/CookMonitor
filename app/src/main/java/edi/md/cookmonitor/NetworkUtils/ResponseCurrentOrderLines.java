
package edi.md.cookmonitor.NetworkUtils;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseCurrentOrderLines {

    @SerializedName("OrdersList")
    @Expose
    private List<OrdersLinesList> ordersList = null;
    @SerializedName("Result")
    @Expose
    private Integer result;
    @SerializedName("ResultMessage")
    @Expose
    private String resultMessage;

    public List<OrdersLinesList> getOrdersList() {
        return ordersList;
    }

    public void setOrdersList(List<OrdersLinesList> ordersList) {
        this.ordersList = ordersList;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

}
