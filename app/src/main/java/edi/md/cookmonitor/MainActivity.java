package edi.md.cookmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import edi.md.cookmonitor.NetworkUtils.ApiUtils;
import edi.md.cookmonitor.NetworkUtils.CommandServices;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.LineOrdersList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseOrderList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.SimpleResultService;
import edi.md.cookmonitor.Obejcts.Order;
import edi.md.cookmonitor.adapters.ExecutableListAdapter;
import edi.md.cookmonitor.adapters.InQueueListAdapter;
import edi.md.cookmonitor.adapters.OrdersListGridAdapter;
import io.fabric.sdk.android.Fabric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersLinesList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseAction;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseCurrentOrderLines;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String terminalID, ip, port, UidFinished, UidQueque;
    int  period = 0;
    ListView lv_queues, lv_execut;
    AlertDialog inQueue, executeDialog;

    TimerTask timerTaskGetOrderLines,timerTaskGetOrdersList;
    Timer timerGetOrderLines,timerGetOrdersList;

    boolean keyStart = false;
    ImageView bomj_image;

    SharedPreferences settingsPreference;

    List<OrdersLinesList> listInQueue = new ArrayList<>();
    List<OrdersLinesList> listExecutable = new ArrayList<>();

    InQueueListAdapter adapterInQueue;
    ExecutableListAdapter adapterExecutable;

    GridView gridOrders;
    OrdersListGridAdapter adapterOrders;

    String orderUid;

    private ProgressDialog pgH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);
        int selectedModeWork = settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE);

        if(selectedModeWork == BaseEnum.CookMonitor){
            initUIElementsCookMonitor();
        }
        else if(selectedModeWork == BaseEnum.OrderMonitor){
            initUIElementsOrderMonitor();
        }


    }

    private void initUIElementsCookMonitor() {
        setContentView(R.layout.activity_main);

        lv_queues = findViewById(R.id.list_order_in_rind);
        lv_execut = findViewById(R.id.list_order_execut);
        bomj_image = findViewById(R.id.imageView_bomj);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);

        terminalID = settingsPreference.getString("DeviceId", "");
        ip = settingsPreference.getString("IP", "");
        port = settingsPreference.getString("Port", "");

        period = settingsPreference.getInt("period", 0);
        keyStart = settingsPreference.getBoolean("Key", false);

        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        if (keyStart) {
            bomj_image.setVisibility(View.INVISIBLE);
            GetCurrentOrderLines(ip, port, terminalID,false);

            if (period != 0) {
                startTimerTaskGetOrderLines(period);
            }
        }
        else {
            bomj_image.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Licenta nu este valida!", Toast.LENGTH_SHORT).show();
        }

        lv_queues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                OrdersLinesList lines = adapterInQueue.getItem(position);
                if(lines != null){
                    UidQueque = lines.getUid();
                    String Name = lines.getAssortimentName();

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_go_to_cook, null);

                    stopTimerOrderLines();

                    inQueue = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    inQueue.setView(dialogView);
                    inQueue.setCancelable(false);

                    Button btnCook = dialogView.findViewById(R.id.btn_cook);
                    Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
                    Button btnFinished = dialogView.findViewById(R.id.btn_finished_cook);
                    final TextView txtName = dialogView.findViewById(R.id.txt_name_to_cook);

                    txtName.setText(Name);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            inQueue.dismiss();
                            if (period != 0) {
                                startTimerTaskGetOrderLines(period);
                            }
                        }
                    });

                    btnCook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            terminalID = settingsPreference.getString("DeviceId", "");
                            ip = settingsPreference.getString("IP", "");
                            port = settingsPreference.getString("Port", "");

                            CommandServices serviceMarkStarted = ApiUtils.commandService( ip + ":" + port);
                            final Call<ResponseAction> responseActionCall = serviceMarkStarted.markAsStart(UidQueque);

                            responseActionCall.enqueue(new Callback<ResponseAction>() {
                                @Override
                                public void onResponse(Call<ResponseAction> call, Response<ResponseAction> response) {
                                    if (response.isSuccessful()) {
                                        ResponseAction responseAction = response.body();
                                        int result = responseAction.getResult();
                                        if (result == 0) {
                                            inQueue.dismiss();

                                            GetCurrentOrderLines(ip, port, terminalID,false);
                                            if (period != 0) {
                                                startTimerTaskGetOrderLines(period);
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseAction> call, Throwable t) {

                                }
                            });
                        }
                    });

                    btnFinished.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            terminalID = settingsPreference.getString("DeviceId", "");
                            ip = settingsPreference.getString("IP", "");
                            port = settingsPreference.getString("Port", "");

                            CommandServices commandServices = ApiUtils.commandService(ip+ ":" + port);
                            final Call<ResponseAction> responseActionCall = commandServices.markAsFinished(UidQueque);

                            responseActionCall.enqueue(new Callback<ResponseAction>() {
                                @Override
                                public void onResponse(Call<ResponseAction> call, Response<ResponseAction> response) {
                                    if (response.isSuccessful()) {
                                        ResponseAction responseAction = response.body();
                                        int result = responseAction.getResult();
                                        if (result == 0) {
                                            inQueue.dismiss();

                                            GetCurrentOrderLines(ip, port, terminalID,false);
                                            if (period != 0) {
                                                startTimerTaskGetOrderLines(period);
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseAction> call, Throwable t) {

                                }
                            });
                        }
                    });

                    inQueue.show();
                }
            }
        });

        lv_execut.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                OrdersLinesList lines = adapterExecutable.getItem(position);
                if(lines != null){
                    UidFinished = lines.getUid();
                    String Name = lines.getAssortimentName();

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_dishes_final, null);

                    stopTimerOrderLines();

                    executeDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    executeDialog.setView(dialogView);
                    executeDialog.setCancelable(false);

                    Button btnGive = dialogView.findViewById(R.id.btn_execut);
                    Button btnCancel = dialogView.findViewById(R.id.btn_cancel_execut);
                    final TextView txtName = dialogView.findViewById(R.id.txt_name_to_give);

                    txtName.setText(Name);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executeDialog.dismiss();
                            if (period != 0) {
                                startTimerTaskGetOrderLines(period);
                            }
                        }
                    });

                    btnGive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            terminalID = settingsPreference.getString("DeviceId", "");
                            ip = settingsPreference.getString("IP", "");
                            port = settingsPreference.getString("Port", "");

                            CommandServices commandServices = ApiUtils.commandService(ip+ ":" + port);
                            final Call<ResponseAction> responseActionCall = commandServices.markAsFinished(UidFinished);

                            responseActionCall.enqueue(new Callback<ResponseAction>() {
                                @Override
                                public void onResponse(Call<ResponseAction> call, Response<ResponseAction> response) {
                                    if (response.isSuccessful()) {
                                        ResponseAction responseAction = response.body();
                                        int result = responseAction.getResult();
                                        if (result == 0) {
                                            executeDialog.dismiss();

                                            GetCurrentOrderLines(ip, port, terminalID,false);
                                            if (period != 0) {
                                                startTimerTaskGetOrderLines(period);
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseAction> call, Throwable t) {

                                }
                            });
                        }


                    });
                    executeDialog.show();
                }
            }
        });
    }

    private void initUIElementsOrderMonitor() {
        setContentView(R.layout.activity_main_order);

        gridOrders = findViewById(R.id.grid_view_list_orders);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);

        terminalID = settingsPreference.getString("DeviceId", "");
        ip = settingsPreference.getString("IP", "");
        port = settingsPreference.getString("Port", "");

        period = settingsPreference.getInt("period", 0);
        keyStart = settingsPreference.getBoolean("Key", false);
        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        if (keyStart) {
            GetCurrentOrdersList(ip+ ":" + port, 36,true,false);

            if (period != 0) {
                startTimerTaskGetOrdersList(period);
            }

        }

        gridOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Order order = adapterOrders.getItem(i);
                if (order != null) {
                    orderUid = order.getUid();
                    String number = String.valueOf(order.getNumber());

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_make_order_final, null);

                    if (timerTaskGetOrdersList != null) {
                        timerTaskGetOrdersList.cancel();
                    }

                    executeDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    executeDialog.setView(dialogView);
                    executeDialog.setCancelable(false);

                    Button btnGive = dialogView.findViewById(R.id.btn_finis_order);
                    Button btnCancel = dialogView.findViewById(R.id.btn_cancel_order);
                    final TextView txtName = dialogView.findViewById(R.id.txt_number_order);

                    txtName.setText(number);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            executeDialog.dismiss();
                            if (period != 0) {
                                startTimerTaskGetOrdersList(period);
                            }
                        }
                    });

                    btnGive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ip = settingsPreference.getString("IP", "");
                            port = settingsPreference.getString("Port", "");

                            CommandServices commandServices = ApiUtils.commandService(ip+ ":" + port);
                            final Call<SimpleResultService> responseActionCall = commandServices.markOrderAsFinished(orderUid);

                            responseActionCall.enqueue(new Callback<SimpleResultService>() {
                                @Override
                                public void onResponse(Call<SimpleResultService> call, Response<SimpleResultService> response) {
                                    if (response.isSuccessful()) {
                                        SimpleResultService responseAction = response.body();
                                        int result = responseAction.getResult();
                                        if (result == 0) {
                                            executeDialog.dismiss();

                                            GetCurrentOrdersList(ip+ ":" + port, 36,true,false);
                                            if (period != 0) {
                                                startTimerTaskGetOrdersList(period);
                                            }

                                        } else {
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<SimpleResultService> call, Throwable t) {

                                }
                            });
                        }


                    });
                    executeDialog.show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings: {
                if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.CookMonitor){
                    stopTimerOrderLines();
                }
                else if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.OrderMonitor){
                    stopTimerOrdersList();
                }

                Intent setting_activity = new Intent(this, SettingsActivity.class);
                startActivityForResult(setting_activity, 101);
            }
            break;
            case R.id.action_refresh: {
                pgH.setMessage("loading...");
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();

                if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.CookMonitor){
                    if (keyStart){
                        if(period == 0){
                            GetCurrentOrderLines(ip, port, terminalID,false);
                        }
                        else{
                            stopTimerOrderLines();
                            GetCurrentOrderLines(ip, port, terminalID,true);
                        }
                    }
                }
                else if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.OrderMonitor){
                    if (keyStart){
                        if(period == 0){
                            GetCurrentOrderLines(ip, port, terminalID,false);
                        }
                        else{
                            stopTimerOrdersList();
                            GetCurrentOrdersList(ip + ":" + port, 36,true,true);
                        }
                    }
                }

            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GetCurrentOrderLines(final String ipAddress, final String portNumber, final String terminalId, final boolean refresh) {
        if (adapterExecutable != null)
            adapterExecutable.clear();
        if (adapterInQueue != null)
            adapterInQueue.clear();

        if (inQueue != null && inQueue.isShowing()) {
            inQueue.dismiss();
        }
        if (executeDialog != null && executeDialog.isShowing()) {
            executeDialog.dismiss();
        }

        CommandServices getCurrentOrderLines = ApiUtils.commandService( ipAddress + ":" + portNumber);
        final Call<ResponseCurrentOrderLines> billListCall = getCurrentOrderLines.getCurrentOrdersList(terminalId);

        billListCall.enqueue(new Callback<ResponseCurrentOrderLines>() {
            @Override
            public void onResponse(Call<ResponseCurrentOrderLines> call, Response<ResponseCurrentOrderLines> response) {
                ResponseCurrentOrderLines responseBillsList = response.body();

                if (responseBillsList != null && responseBillsList.getResult() == 0) {
                    if(responseBillsList.getOrdersList() != null){


                        List<OrdersLinesList> ordersLinesLists = responseBillsList.getOrdersList();

                        for (OrdersLinesList ordersLinesList : ordersLinesLists) {
                            int mPreparationRate = ordersLinesList.getPreparationRate();
                            int mState = ordersLinesList.getState();

                            if (mState == 2) {
                                listInQueue.add(ordersLinesList);
                            }
                            if (mState == 3) {
                                listExecutable.add(ordersLinesList);
                            }

                            sortListInQueue(listInQueue);
                            sortListInQueue(listExecutable);

                            adapterInQueue = new InQueueListAdapter(MainActivity.this,R.layout.item_in_queuqe,listInQueue);
                            adapterExecutable = new ExecutableListAdapter(MainActivity.this,R.layout.item_execut,listExecutable);

                            lv_queues.setAdapter(adapterInQueue);
                            lv_execut.setAdapter(adapterExecutable);

                            if(refresh){
                                if (period != 0) {
                                    startTimerTaskGetOrderLines(period);
                                }
                            }
                            if(pgH != null && pgH.isShowing())
                                pgH.dismiss();
                        }
                    }
                    else {
                        //TODO list line null
                    }

                }
                else {
                    //TODO error download orderLines
                }

            }

            @Override
            public void onFailure(Call<ResponseCurrentOrderLines> call, Throwable t) {
                //TODO error download orderLines
            }
        });
    }

    private void GetCurrentOrdersList(String address, int hours, boolean withLines, final boolean refresh){

        CommandServices commandServices = ApiUtils.commandService(address);
        Call<ResponseOrderList> call = commandServices.getOrdersList(hours,withLines);

        call.enqueue(new Callback<ResponseOrderList>() {
            @Override
            public void onResponse(Call<ResponseOrderList> call, Response<ResponseOrderList> response) {
                ResponseOrderList responseOrderList = response.body();

                if(responseOrderList != null && responseOrderList.getResult() == 0){
                    if(responseOrderList.getOrdersList() != null ){
                        List<OrdersList> ordersLists = responseOrderList.getOrdersList();
                        List<Order> orderToShow = new ArrayList<>();

                        for(OrdersList order : ordersLists){
                            Order newOrder = copyFromOrdersList(order);

//                            if(order.getState() != 6){
                                orderToShow.add(newOrder);
//                            }
                        }
                        adapterOrders = new OrdersListGridAdapter(MainActivity.this,R.layout.item_grid_orders_list,orderToShow);

//                        adapterOrders = new OrdersListGridAdapter(MainActivity.this,R.layout.item_grid_orders_list,ordersLists);

                        gridOrders.setAdapter(adapterOrders);
                        if(refresh){
                            if (period != 0) {
                                startTimerTaskGetOrdersList(period);
                            }
                        }
                        if(pgH != null && pgH.isShowing())
                            pgH.dismiss();

                    }
                }
                else{
                    //TODO error null or 0
                }
            }

            @Override
            public void onFailure(Call<ResponseOrderList> call, Throwable t) {
                //TODO failure
            }
        });

    }

    private void startTimerTaskGetOrderLines(final int periodSchedule) {
        timerGetOrderLines = new Timer();
        timerTaskGetOrderLines = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GetCurrentOrderLines(ip, port, terminalID,false);
                        Log.d("timerTaskGetOrderLines", timerTaskGetOrderLines.toString());
                    }
                });
            }
        };

        timerGetOrderLines.schedule(timerTaskGetOrderLines, periodSchedule, periodSchedule);
    }

    private void startTimerTaskGetOrdersList(final int periodSchedule) {
        timerGetOrdersList = new Timer();
        timerTaskGetOrdersList = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GetCurrentOrdersList(ip + ":" + port, 36,true,false);
                        Log.d("TimeUpdate order list ", timerGetOrdersList.toString());
                    }
                });
            }
        };

        timerGetOrdersList.schedule(timerTaskGetOrdersList, periodSchedule, periodSchedule);
    }

    private void stopTimerOrderLines(){
        if(timerGetOrderLines != null){
            timerGetOrderLines.cancel();
            timerGetOrderLines.purge();
            timerGetOrderLines = null;
            Log.d("onPause", "timerGetOrderLines is cancel");
        }
    }

    private void stopTimerOrdersList(){
        if(timerGetOrdersList != null){
            timerGetOrdersList.cancel();
            timerGetOrdersList.purge();
            timerGetOrdersList = null;
            Log.d("onPause", "timerGetOrdersList is cancel");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.CookMonitor){
            initUIElementsCookMonitor();
        }
        else if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.OrderMonitor){
            initUIElementsOrderMonitor();
        }
    }

    private void sortListInQueue (List<OrdersLinesList> ordersLinesLists){
        ordersLinesLists.sort(new Comparator<OrdersLinesList>() {
            @Override
            public int compare(OrdersLinesList o1, OrdersLinesList o2) {
                return o1.getNumber().compareTo(o2.getNumber());
            }
        });
    }

    private Order copyFromOrdersList(OrdersList ordersList){
        Order order = new Order();
        String dateCreated = ordersList.getDateCreated();
        String dateStarted = ordersList.getDateStarted();
        String datePlanedDelivery = ordersList.getDeliveryPlannedDate();

        order.setDateCreated(replaceDateParseToLong(dateCreated));
        order.setDeliveryPlannedDate(replaceDateParseToLong(datePlanedDelivery));
        order.setDateStarted(replaceDateParseToLong(dateStarted));
        order.setNumber(ordersList.getNumber());

        order.setUid(ordersList.getUid());
        order.setState(ordersList.getState());
        order.setDeliveryType(ordersList.getDeliveryType());

        if(ordersList.getLines() != null && ordersList.getLines().size() > 0){
            order.setLines(ordersList.getLines());

            boolean orderDone = true;
            for(LineOrdersList line :ordersList.getLines() ){
                if(line.getState() != 6)
                    orderDone = false;
            }

            order.setDone(orderDone);
        }

        return order;
    }

    public static long replaceDateParseToLong (String date){
        if(date !=null ){
            date = date.replace("/Date(","");
            date = date.replace("+0200)/","");
            date = date.replace("+0300)/","");
            return Long.parseLong(date);
        }
        else
            return 0;

    }
}
