package edi.md.cookmonitor.NetworkUtils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceMarkStarted {
    @GET("/OrderMonitor/json/MarkOrderLineAsStarted")
      Call<ResponseAction> markAsStart(@Query("orderUid") String param1);
}
