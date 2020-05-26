package edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 19.05.2020
 */

public class SimpleResultService {
    @SerializedName("Result")
    @Expose
    private Integer result;
    @SerializedName("ResultMessage")
    @Expose
    private String resultMessage;

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
