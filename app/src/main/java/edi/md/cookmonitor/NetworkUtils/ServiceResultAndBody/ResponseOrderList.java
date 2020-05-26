
package edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersList;

public class ResponseOrderList {

    @SerializedName("OrdersList")
    @Expose
    private List<OrdersList> ordersList = null;
    @SerializedName("Result")
    @Expose
    private Integer result;
    @SerializedName("ResultMessage")
    @Expose
    private Object resultMessage;

    public List<OrdersList> getOrdersList() {
        return ordersList;
    }

    public void setOrdersList(List<OrdersList> ordersList) {
        this.ordersList = ordersList;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Object getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(Object resultMessage) {
        this.resultMessage = resultMessage;
    }

}
