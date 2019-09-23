package edi.md.cookmonitor.NetworkUtils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceGetCurrentOrderLines {
    @GET("/OrderMonitor/json/GetCurrentOrderLines")
      Call<ResponseCurrentOrderLines> getCurrentOrdersList(@Query("terminalId") String param1);
}
