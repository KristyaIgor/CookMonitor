package edi.md.cookmonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import edi.md.cookmonitor.NetworkUtils.ApiUtils;
import edi.md.cookmonitor.NetworkUtils.CommandServices;
import edi.md.cookmonitor.NetworkUtils.RemoteConfigHelper;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.LineOrdersList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseOrderList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.SimpleResultService;
import edi.md.cookmonitor.Obejcts.Order;
import edi.md.cookmonitor.adapters.AdapterLinesDialogOrder;
import edi.md.cookmonitor.adapters.ExecutableListAdapter;
import edi.md.cookmonitor.adapters.InQueueListAdapter;
import edi.md.cookmonitor.adapters.OrdersListGridAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.OrdersLinesList;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseAction;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.ResponseCurrentOrderLines;
import edi.md.cookmonitor.utils.Beeper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String terminalID, ip, port, UidFinished, UidQueque;
    int  period = 0;
    GridView lv_queues, lv_execut;
    AlertDialog inQueue, executeDialog, orderDialog;

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

    Button changeMonitor, changeMonitortoCook;

    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    Beeper beeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);

        simpleDateFormatMD = new SimpleDateFormat("HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);

        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);


        pgH.setMessage(getResources().getString(R.string.validaiton) + "...");
        pgH.setCancelable(false);
        pgH.setIndeterminate(true);
        pgH.show();

        FirebaseApp.initializeApp(this);

        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        //defaultvalue

        Map<String,Object> defaultValue = new HashMap<>();
        defaultValue.put(RemoteConfigHelper.KEY_COMPANY_NAME,true);

        remoteConfig.setDefaultsAsync(defaultValue);

        remoteConfig.fetchAndActivate().addOnCompleteListener( new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if(task.isSuccessful()){
                    Log.d("TAG", "remote config is fetched.");

                    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

                    boolean companyName = remoteConfig.getBoolean("star_kebab");

                    pgH.dismiss();
                    checkLicense(true,companyName);
                }
                else {
                    pgH.dismiss();
                    checkLicense(false, true);
                }
            }
        });

    }

    public void checkLicense(boolean remoteFetched, boolean isActive){
        keyStart = settingsPreference.getBoolean("Key", false);
        int mode = settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE);
        if(remoteFetched){
            if (isActive){
                if(keyStart){
                    if(mode == BaseEnum.CookMonitor){
                        stopTimerOrdersList();
                        initUIElementsCookMonitor();
                    }
                    else if(mode == BaseEnum.OrderMonitor){
                        stopTimerOrderLines();
                        initUIElementsOrderMonitor();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_NoAccess), Toast.LENGTH_SHORT).show();
                    initUIElementsNoAccess();
                }
            }
            else{
                Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_NoAccess), Toast.LENGTH_SHORT).show();
                initUIElementsNoAccess();
            }
        }
        else {
            if (keyStart) {
                if (mode == BaseEnum.CookMonitor) {
                    stopTimerOrdersList();
                    initUIElementsCookMonitor();
                } else if (mode == BaseEnum.OrderMonitor) {
                    stopTimerOrderLines();
                    initUIElementsOrderMonitor();
                }
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_NoAccess), Toast.LENGTH_SHORT).show();
                initUIElementsNoAccess();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            keyStart = settingsPreference.getBoolean("Key", false);
            int mode = settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE);
            if (keyStart) {
                if (mode == BaseEnum.CookMonitor) {
                    stopTimerOrdersList();
                    initUIElementsCookMonitor();
                } else if (mode == BaseEnum.OrderMonitor) {
                    stopTimerOrderLines();
                    initUIElementsOrderMonitor();
                }
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_NoAccess), Toast.LENGTH_SHORT).show();
                initUIElementsNoAccess();
            }
        }
    }

    private void initUIElementsNoAccess() {
        setContentView(R.layout.activity_main_no_access);

        Toolbar toolbar = findViewById(R.id.toolbar_no_acces);
        setSupportActionBar(toolbar);

        Button exit = findViewById(R.id.buttonExit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * initializarea elementelor si toate procesele/functii pentru modul bucatarului
     * primirea comenzilor de la casa
     * functia pentru primirea comenzilor in regim de bucatar GetCurrentOrderLines
     */

    private void initUIElementsCookMonitor() {
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_cook);
        setSupportActionBar(toolbar);

        lv_queues = findViewById(R.id.list_order_in_rind);
        lv_execut = findViewById(R.id.list_order_execut);
        bomj_image = findViewById(R.id.imageView_bomj);

        changeMonitor = findViewById(R.id.button_switch);


        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);

        terminalID = settingsPreference.getString("DeviceId", "");
        ip = settingsPreference.getString("IP", "");
        port = settingsPreference.getString("Port", "");

        period = settingsPreference.getInt("period", 0);


        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        getCookMonitorLines(ip, port, terminalID,false);
        if (period != 0) {
            timerTaskGetCookMonitorLines(period);
        }

        int workMode = settingsPreference.getInt("ModeWork",BaseEnum.NoneMode);

        if(workMode == BaseEnum.OneMode)
            changeMonitor.setVisibility(View.GONE);
        else
            changeMonitor.setVisibility(View.VISIBLE);

        lv_queues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                OrdersLinesList lines = adapterInQueue.getItem(position);
                if(lines != null){
                    UidQueque = lines.getUid();
                    String productName = lines.getAssortimentName();

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_go_to_cook, null);

                    stopTimerOrderLines();

                    inQueue = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    inQueue.setView(dialogView);

                    Button cook = dialogView.findViewById(R.id.btn_cook);
                    Button cooked = dialogView.findViewById(R.id.btn_finished_cook);
                    Button cancel = dialogView.findViewById(R.id.btn_cancel_coock);
                    final TextView productTitle = dialogView.findViewById(R.id.txt_name_to_cook);

                    productTitle.setText(productName);

                    cook.setOnClickListener(v -> {
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

                                        getCookMonitorLines(ip, port, terminalID,false);
                                        if (period != 0) {
                                            timerTaskGetCookMonitorLines(period);
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
                    });

                    cooked.setOnClickListener(v -> {
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
                                        getCookMonitorLines(ip, port, terminalID,false);
                                        inQueue.dismiss();
                                    } else {
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.errorCod) + result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseAction> call, Throwable t) {

                            }
                        });
                    });

                    cancel.setOnClickListener(v -> {
                        inQueue.dismiss();
                    });

                    inQueue.show();

                    inQueue.setOnDismissListener(dialog -> {
                        if (period != 0) {
                            timerTaskGetCookMonitorLines(period);
                        }
                    });
                }
            }
        });

        lv_execut.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                OrdersLinesList lines = adapterExecutable.getItem(position);
                if(lines != null){
                    UidFinished = lines.getUid();
                    String productName = lines.getAssortimentName();

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_dishes_final, null);

                    stopTimerOrderLines();

                    executeDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    executeDialog.setView(dialogView);

                    Button confirm = dialogView.findViewById(R.id.btn_execut);
                    Button cancel = dialogView.findViewById(R.id.btn_cancel_execut);
                    TextView productTitle = dialogView.findViewById(R.id.txt_name_to_give);

                    productTitle.setText(productName);

                    confirm.setOnClickListener(v -> {
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
                                        getCookMonitorLines(ip, port, terminalID,false);
                                        executeDialog.dismiss();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseAction> call, Throwable t) {

                            }
                        });
                    });

                    cancel.setOnClickListener(v -> {
                        executeDialog.dismiss();
                    });

                    executeDialog.show();

                    executeDialog.setOnDismissListener(dialog -> {
                        if (period != 0) {
                            timerTaskGetCookMonitorLines(period);
                        }
                    });
                }
            }
        });

        changeMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimerOrderLines();
                getSharedPreferences("Settings", MODE_PRIVATE).edit().putInt("WorkAs",BaseEnum.OrderMonitor).apply();

                Log.d("TAG", "Start activity: change monitor: cook monitor -> order monitor");
                MainActivity.this.startActivity(new Intent(MainActivity.this,MainActivity.class));
            }
        });
    }

    private void initUIElementsOrderMonitor() {
        setContentView(R.layout.activity_main_order);

        Toolbar toolbar = findViewById(R.id.toolbar_request);
        setSupportActionBar(toolbar);

        changeMonitortoCook = findViewById(R.id.button_switch_to_cook);

        gridOrders = findViewById(R.id.grid_view_list_orders);

        settingsPreference = getSharedPreferences("Settings", MODE_PRIVATE);

        terminalID = settingsPreference.getString("DeviceId", "");
        ip = settingsPreference.getString("IP", "");
        port = settingsPreference.getString("Port", "");

        period = settingsPreference.getInt("period", 0);
        pgH = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);


        getOrderMonitorList(ip+ ":" + port, 36,true,false);
        if (period != 0) {
            timerTaskOrderMonitorList(period);
        }

        int workMode = settingsPreference.getInt("ModeWork",BaseEnum.NoneMode);

        if(workMode == BaseEnum.OneMode)
            changeMonitortoCook.setVisibility(View.GONE);
        else
            changeMonitortoCook.setVisibility(View.VISIBLE);

        gridOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Order order = adapterOrders.getItem(i);
                if (order != null) {
                    orderUid = order.getUid();
                    String number = String.valueOf(order.getNumber());

                    List<LineOrdersList> orderLines = order.getLines();

                    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.msg_make_order_final, null);

                    stopTimerOrdersList();

                    orderDialog = new AlertDialog.Builder(MainActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                    orderDialog.setView(dialogView);

                    Button btnGive = dialogView.findViewById(R.id.btn_finis_order);
                    Button btnPrepared = dialogView.findViewById(R.id.btn_order_prepared);
                    Button btnCancel = dialogView.findViewById(R.id.btn_cancel_order);
                    final TextView txtName = dialogView.findViewById(R.id.txt_number_order);
                    TextView textDateCooked = dialogView.findViewById(R.id.textDateBillCooked);
                    RecyclerView recyclerView = dialogView.findViewById(R.id.lineOrderList);

                    long currDate = new Date().getTime();
                    long orderCreatedDate = order.getDateCreated();
                    long orderCooked = currDate - orderCreatedDate;

                    long minute = (orderCooked / (1000 * 60)) % 60;
                    long hour = (orderCooked / (1000 * 60 * 60)) % 24;

                    String time = String.format("%02d:%02d", hour, minute);
                    Log.d("TimerShift", "Shift need closed! " + time);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);
//                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
//                    recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(2));

                    AdapterLinesDialogOrder adapterLinesDialogOrder = new AdapterLinesDialogOrder(orderLines);
                    recyclerView.setAdapter(adapterLinesDialogOrder);

                    textDateCooked.setText("In proces: " + time + " min.");

                    txtName.setText("Comanda : " + number);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            orderDialog.dismiss();
                            if (period != 0) {
                                timerTaskOrderMonitorList(period);
                            }
                        }
                    });

                    btnPrepared.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ip = settingsPreference.getString("IP", "");
                            port = settingsPreference.getString("Port", "");

                            CommandServices commandServices = ApiUtils.commandService(ip+ ":" + port);
                            final Call<SimpleResultService> responseActionCall = commandServices.markOrderAsPrepared(orderUid);

                            responseActionCall.enqueue(new Callback<SimpleResultService>() {
                                @Override
                                public void onResponse(Call<SimpleResultService> call, Response<SimpleResultService> response) {
                                    if (response.isSuccessful()) {
                                        SimpleResultService responseAction = response.body();
                                        int result = responseAction.getResult();
                                        if (result == 0) {
                                            Log.d("TAG", "markOrderAsPrepared - error code: " + result);
                                            orderDialog.dismiss();

                                            getOrderMonitorList(ip+ ":" + port, 36,true,false);
                                            if (period != 0) {
                                                timerTaskOrderMonitorList(period);
                                            }

                                        } else {
                                            Log.d("TAG", "markOrderAsPrepared - error code: " + result);
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Log.d("TAG", "markOrderAsPrepared: un success");
                                    }
                                }

                                @Override
                                public void onFailure(Call<SimpleResultService> call, Throwable t) {
                                    Toast.makeText(MainActivity.this, "Erroare!Mesaj: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", "markOrderAsPrepared: onFailure: " + t.getMessage());
                                }
                            });
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
                                            orderDialog.dismiss();
                                            Log.d("TAG", "markOrderAsFinished - error code: " + result);

                                            getOrderMonitorList(ip+ ":" + port, 36,true,false);
                                            if (period != 0) {
                                                timerTaskOrderMonitorList(period);
                                            }

                                        } else {
                                            Log.d("TAG", "markOrderAsFinished - error code: " + result);
                                            Toast.makeText(MainActivity.this, "Erroare!Codul:" + result, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Log.d("TAG", "markOrderAsFinished: un success");
                                    }
                                }

                                @Override
                                public void onFailure(Call<SimpleResultService> call, Throwable t) {
                                    Log.d("TAG", "markOrderAsFinished - onFailure: " + t.getMessage());
                                }
                            });
                        }


                    });
                    orderDialog.show();

                    orderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (period != 0) {
                                timerTaskOrderMonitorList(period);
                            }
                        }
                    });
                }
            }
        });

        changeMonitortoCook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimerOrdersList();
                getSharedPreferences("Settings", MODE_PRIVATE).edit().putInt("WorkAs",BaseEnum.CookMonitor).apply();

                Log.d("TAG", "Start activity: change monitor: order monitor -> cook monitor");
                MainActivity.this.startActivity(new Intent(MainActivity.this,MainActivity.class));
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
                if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.CookMonitor){
                    if (keyStart){
                        pgH.setMessage("loading lines...");
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();

                        if(period == 0){
                            getCookMonitorLines(ip, port, terminalID,false);
                        }
                        else{
                            stopTimerOrderLines();
                            getCookMonitorLines(ip, port, terminalID,true);
                        }
                    }
                }
                else if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.OrderMonitor){
                    if (keyStart){
                        pgH.setMessage("loading order list...");
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();

                        if (period != 0) {
                            stopTimerOrdersList();
                        }
                        getOrderMonitorList(ip + ":" + port, 36,true,true);
                    }
                }

            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCookMonitorLines(final String ipAddress, final String portNumber, final String terminalId, final boolean refresh) {
        CommandServices getCurrentOrderLines = ApiUtils.commandService( ipAddress + ":" + portNumber);
        final Call<ResponseCurrentOrderLines> billListCall = getCurrentOrderLines.getCurrentOrdersList(terminalId);

        billListCall.enqueue(new Callback<ResponseCurrentOrderLines>() {
            @Override
            public void onResponse(Call<ResponseCurrentOrderLines> call, Response<ResponseCurrentOrderLines> response) {
                ResponseCurrentOrderLines responseBillsList = response.body();
                if (responseBillsList != null && responseBillsList.getResult() == 0) {
                    Log.d("TAG", "getCookMonitorLines - error code: " + responseBillsList.getResult());
                    if(responseBillsList.getOrdersList() != null){
                        if(inQueue == null || !inQueue.isShowing() && executeDialog == null || !executeDialog.isShowing()) {
                            if (adapterExecutable != null)
                                adapterExecutable.clear();
                            if (adapterInQueue != null)
                                adapterInQueue.clear();

                            List<OrdersLinesList> ordersLinesLists = responseBillsList.getOrdersList();

                            for (OrdersLinesList ordersLinesList : ordersLinesLists) {
                                int mPreparationRate = ordersLinesList.getPreparationRate();
                                int mState = ordersLinesList.getState();
                                String departamentTypeName = ordersLinesList.getDepartmentTypeName();
                                if (!departamentTypeName.toLowerCase().contains("bar")){
                                    if (mState == 2) {
                                        listInQueue.add(ordersLinesList);
                                    }
                                    if (mState == 3) {
                                        listExecutable.add(ordersLinesList);
                                    }
                                }
                            }
                            sortListInQueue(listInQueue);
                            sortListInQueue(listExecutable);

                            adapterInQueue = new InQueueListAdapter(MainActivity.this,R.layout.item_in_queuqe,listInQueue);
                            adapterExecutable = new ExecutableListAdapter(MainActivity.this,R.layout.item_execut,listExecutable);

                            lv_queues.setAdapter(adapterInQueue);
                            lv_execut.setAdapter(adapterExecutable);

                            if(refresh){
                                if (period != 0) {
                                    timerTaskGetCookMonitorLines(period);
                                }
                                if(pgH != null && pgH.isShowing())
                                    pgH.dismiss();
                            }
                            if(pgH != null && pgH.isShowing())
                                pgH.dismiss();
                        }
                    }
                    else {
                        if(pgH != null && pgH.isShowing())
                            pgH.dismiss();
                        Toast.makeText(MainActivity.this, "Error, list line is null!" , Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(pgH != null && pgH.isShowing())
                        pgH.dismiss();
                    Toast.makeText(MainActivity.this, "Error from service order lines: " + responseBillsList.getResult(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "GetCurrentOrderLines - error code:" + responseBillsList.getResult());
                }

            }

            @Override
            public void onFailure(Call<ResponseCurrentOrderLines> call, Throwable t) {
                pgH.dismiss();
                Beeper.getInstance().startBeep(MainActivity.this, 2, 1, 1000,10);
                Toast.makeText(MainActivity.this, "Error from service order lines: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "GetCurrentOrderLines - onFailure:" + t.getMessage());
            }
        });
    }

    private void getOrderMonitorList(String address, int hours, boolean withLines, final boolean refresh){

        CommandServices commandServices = ApiUtils.commandService(address);
        Call<ResponseOrderList> call = commandServices.getOrdersList(hours,withLines);

        call.enqueue(new Callback<ResponseOrderList>() {
            @Override
            public void onResponse(Call<ResponseOrderList> call, Response<ResponseOrderList> response) {
                ResponseOrderList responseOrderList = response.body();
                if(responseOrderList != null && responseOrderList.getResult() == 0){
                    Log.d("TAG", "GetCurrentOrdersList - error code: " + responseOrderList.getResult());
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
                                timerTaskOrderMonitorList(period);
                            }
                            if(pgH != null || pgH.isShowing())
                                pgH.dismiss();
                        }
                        if(pgH != null || pgH.isShowing())
                            pgH.dismiss();
                    }
                    else{
                        Log.d("TAG", "GetCurrentOrdersList - getOrdersList: null");
                        if(pgH != null || pgH.isShowing())
                            pgH.dismiss();
                    }
                }
                else{
                    pgH.dismiss();
                    Toast.makeText(MainActivity.this, "Error from service order list: " + responseOrderList.getResult(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "GetCurrentOrdersList - error code:"+ responseOrderList.getResult());
                }
            }

            @Override
            public void onFailure(Call<ResponseOrderList> call, Throwable t) {
                //TODO failure
                Beeper.getInstance().startBeep(MainActivity.this, 2, 1, 1000,10);
                pgH.dismiss();
                Toast.makeText(MainActivity.this, "Error from service order list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "GetCurrentOrdersList - onFailure:" + t.getMessage());
            }
        });

    }

    private void timerTaskGetCookMonitorLines(final int periodSchedule) {
        timerGetOrderLines = new Timer();
        timerTaskGetOrderLines = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getCookMonitorLines(ip, port, terminalID,false);
                        Log.d("TAG", "startTimerTaskGetOrderLines - GetCurrentOrderLines(ip, port, terminalID,false);");
                    }
                });
            }
        };

        timerGetOrderLines.schedule(timerTaskGetOrderLines, periodSchedule, periodSchedule);
    }

    private void timerTaskOrderMonitorList(final int periodSchedule) {
        timerGetOrdersList = new Timer();
        timerTaskGetOrdersList = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getOrderMonitorList(ip + ":" + port, 36,true,false);
                        Log.d("TAG", "startTimerTaskGetOrdersList - GetCurrentOrdersList(ip + : + port, 36,true,false)");
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
            Log.d("TAG", "stopTimerOrderLines - cook monitor stopped");
        }
        else{
            Log.d("TAG", "stopTimerOrderLines - is null");
        }
    }

    private void stopTimerOrdersList(){
        if(timerGetOrdersList != null){
            timerGetOrdersList.cancel();
            timerGetOrdersList.purge();
            timerGetOrdersList = null;
            Log.d("TAG", "stopTimerOrdersList - order monitor stopped");
        }
        else{
            Log.d("TAG", "stopTimerOrdersList - is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.CookMonitor){
            Log.d("TAG", "onPause: cook monitor");
            stopTimerOrderLines();
        }
        else if(settingsPreference.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE) == BaseEnum.OrderMonitor){
            Log.d("TAG", "onPause: order monitor");
            stopTimerOrdersList();
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
