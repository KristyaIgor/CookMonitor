package edi.md.cookmonitor.NetworkUtils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceMarkFinished {
    @GET("/OrderMonitor/json/MarkOrderLineAsFinished")
      Call<ResponseAction> markAsFinished(@Query("orderUid") String param1);
}
