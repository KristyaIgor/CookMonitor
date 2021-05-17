package edi.md.cookmonitor.NetworkUtils;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseAction;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseCurrentOrderLines;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseOrderList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.SimpleResultService;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Igor on 31.01.2020
 */

public interface CommandServices {

    @GET("/OrderMonitor/json/GetCurrentOrderLines")
    Call<ResponseCurrentOrderLines> getCurrentOrdersList(@Query("terminalId") String param1);

    @GET("/OrderMonitor/json/MarkOrderLineAsFinished")
    Call<ResponseAction> markAsFinished(@Query("orderUid") String param1);

    @GET("/OrderMonitor/json/MarkOrderLineAsStarted")
    Call<ResponseAction> markAsStart(@Query("orderUid") String param1);

    @GET("/OrderMonitor/json/Ping")
    Call<Boolean> ping();

    @GET("/OrderMonitor/json/RegisterDevice")
    Call<SimpleResultService> registerDevice (@Query("terminalId") String terminalId);

    @GET("/OrderMonitor/json/GetCurrentOrdersList")
    Call<ResponseOrderList> getOrdersList (@Query("hours") int hours, @Query("withLines") boolean lines);

    @GET("/OrderMonitor/json/MarkOrderAsFinished")
    Call<SimpleResultService> markOrderAsFinished (@Query("orderUid") String orderUid);

    @GET("/OrderMonitor/json/MarkOrderAsPrepared")
    Call<SimpleResultService> markOrderAsPrepared (@Query("orderUid") String orderUid);
}
