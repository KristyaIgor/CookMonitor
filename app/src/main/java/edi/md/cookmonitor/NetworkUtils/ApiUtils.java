package edi.md.cookmonitor.NetworkUtils;

/**
 * Created by Igor on 25.11.2019
 */

public class ApiUtils {
    public static CommandServices commandService (String uri){
        return RetrofitClient.getRemoteServiceClient("http://" + uri).create(CommandServices.class);
    }
}