package edi.md.cookmonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import edi.md.cookmonitor.NetworkUtils.OrdersLinesList;
import edi.md.cookmonitor.NetworkUtils.ResponseAction;
import edi.md.cookmonitor.NetworkUtils.ResponseCurrentOrderLines;
import edi.md.cookmonitor.NetworkUtils.ServiceGetCurrentOrderLines;
import edi.md.cookmonitor.NetworkUtils.ServiceMarkFinished;
import edi.md.cookmonitor.NetworkUtils.ServiceMarkStarted;
import edi.md.cookmonitor.adapters.CustomAdapterExecutable;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    SharedPreferences Settings;

    int MESSAGE_SUCCES = 0, MESSAGE_RESULT_CODE = 1, MESSAGE_NULL_BODY = 2, MESSAGE_FAILURE = 3, period = 0;
    String terminalID, ip, port, UidFinished, UidQueque;

    ListView lv_queque, lv_execut;
    AlertDialog inQueque, executeDialog;

    ArrayList<HashMap<String, Object>> list_in_queque = new ArrayList<>();
    ArrayList<HashMap<String, Object>> list_executable = new ArrayList<>();

    TimerTask timerTaskUpdate;
    Timer updateOrders;

    boolean keyStart = false;

    SimpleAdapter adapterQueque;
    CustomAdapterExecutable adapterExecutable;

    ImageView bomj_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        lv_queque = findViewById(R.id.list_order_in_rind);
        lv_execut = findViewById(R.id.list_order_execut);
        bomj_image = findViewById(R.id.imageView_bomj);


        Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        requestMultiplePermissions();

        terminalID = Settings.getString("DeviceId", "");
        ip = Settings.getString("IP", "");
        port = Settings.getString("Port", "");

        period = Settings.getInt("period", 0);
        keyStart = Settings.getBoolean("Key", false);

        adapterQueque = new SimpleAdapter(MainActivity.this, list_in_queque, R.layout.item_in_queuqe, new String[]{"Name", "PrepRate", "Number", "Count"},
                new int[]{R.id.text_assortment_name, R.id.time_cook, R.id.txt_no_order, R.id.txt_count});

        adapterExecutable = new CustomAdapterExecutable(MainActivity.this,list_executable);

        if (keyStart) {
            bomj_image.setVisibility(View.INVISIBLE);
            getOrderLines(ip, port, terminalID);
            updateOrders = new Timer();
            if (period != 0) {
                startTimetaskSync();
                updateOrders.schedule(timerTaskUpdate, period, period);
            }

        }
        else {
            bomj_image.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Licenta nu este valida!", Toast.LENGTH_SHORT).show();
        }

        lv_queque.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UidQueque = (String) list_in_queque.get(position).get("Uid");
                String Name = (String) list_in_queque.get(position).get("Name");
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.msg_go_to_cook, null);

                if (timerTaskUpdate != null) {
                    timerTaskUpdate.cancel();
                }

                inQueque = new AlertDialog.Builder(MainActivity.this).create();
                inQueque.setView(dialogView);
                inQueque.setCancelable(false);

                Button btnCook = dialogView.findViewById(R.id.btn_cook);
                Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
                final TextView txtName = dialogView.findViewById(R.id.txt_name_to_cook);

                txtName.setText(Name);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inQueque.dismiss();
                        if (period != 0) {
                            updateOrders = new Timer();
                            startTimetaskSync();
                            updateOrders.schedule(timerTaskUpdate, period, period);
                        }
                    }
                });

                btnCook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(3, TimeUnit.MINUTES)
                                .readTimeout(4, TimeUnit.MINUTES)
                                .writeTimeout(2, TimeUnit.MINUTES)
                                .build();
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://" + ip + ":" + port)
                                .addConverterFactory(GsonConverterFactory.create())
                                .client(okHttpClient)
                                .build();
                        ServiceMarkStarted serviceMarkStarted = retrofit.create(ServiceMarkStarted.class);
                        final Call<ResponseAction> responseActionCall = serviceMarkStarted.markAsStart(UidQueque);

                        responseActionCall.enqueue(new Callback<ResponseAction>() {
                            @Override
                            public void onResponse(Call<ResponseAction> call, Response<ResponseAction> response) {
                                if (response.isSuccessful()) {
                                    ResponseAction responseAction = response.body();
                                    int result = responseAction.getResult();
                                    if (result == 0) {
                                        inQueque.dismiss();
                                        getOrderLines(ip, port, terminalID);
                                        if (period != 0) {
                                            updateOrders = new Timer();
                                            startTimetaskSync();
                                            updateOrders.schedule(timerTaskUpdate, period, period);
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
                inQueque.show();
            }
        });

        lv_execut.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UidFinished = (String) list_executable.get(position).get("Uid");
                String Name = (String) list_executable.get(position).get("Name");

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.msg_dishes_final, null);

                if (timerTaskUpdate != null) {
                    timerTaskUpdate.cancel();
                }

                executeDialog = new AlertDialog.Builder(MainActivity.this).create();
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
                            updateOrders = new Timer();
                            startTimetaskSync();
                            updateOrders.schedule(timerTaskUpdate, period, period);
                        }
                    }
                });

                btnGive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(3, TimeUnit.MINUTES)
                                .readTimeout(4, TimeUnit.MINUTES)
                                .writeTimeout(2, TimeUnit.MINUTES)
                                .build();
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://" + ip + ":" + port)
                                .addConverterFactory(GsonConverterFactory.create())
                                .client(okHttpClient)
                                .build();
                        ServiceMarkFinished serviceMarkFinished = retrofit.create(ServiceMarkFinished.class);
                        final Call<ResponseAction> responseActionCall = serviceMarkFinished.markAsFinished(UidFinished);


                        responseActionCall.enqueue(new Callback<ResponseAction>() {
                            @Override
                            public void onResponse(Call<ResponseAction> call, Response<ResponseAction> response) {
                                if (response.isSuccessful()) {
                                    ResponseAction responseAction = response.body();
                                    int result = responseAction.getResult();
                                    if (result == 0) {
                                        executeDialog.dismiss();
                                        getOrderLines(ip, port, terminalID);
                                        if (period != 0) {
                                            updateOrders = new Timer();
                                            startTimetaskSync();
                                            updateOrders.schedule(timerTaskUpdate, period, period);
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

    });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings: {
                if (timerTaskUpdate != null) {
                    timerTaskUpdate.cancel();
                    Log.d("onPause", "timer is cancel");
                }
                Intent setting_activity = new Intent(this, SettingsActivity.class);
                startActivityForResult(setting_activity, 101);
            }
            break;
            case R.id.action_refresh: {
                if (keyStart)
                    getOrderLines(ip, port, terminalID);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getOrderLines(final String ipAdress, final String portNumber, final String terminalId) {
        list_in_queque.clear();
        list_executable.clear();

        if (inQueque != null && inQueque.isShowing()) {
            inQueque.dismiss();
        }
        if (executeDialog != null && executeDialog.isShowing()) {
            executeDialog.dismiss();
        }

        Thread mGetBillsList = new Thread(new Runnable() {
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.MINUTES)
                        .readTimeout(4, TimeUnit.MINUTES)
                        .writeTimeout(2, TimeUnit.MINUTES)
                        .build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://" + ipAdress + ":" + portNumber)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build();
                ServiceGetCurrentOrderLines getCurrentOrderLines = retrofit.create(ServiceGetCurrentOrderLines.class);
                final Call<ResponseCurrentOrderLines> billListCall = getCurrentOrderLines.getCurrentOrdersList(terminalId);

                billListCall.enqueue(new Callback<ResponseCurrentOrderLines>() {
                    @Override
                    public void onResponse(Call<ResponseCurrentOrderLines> call, Response<ResponseCurrentOrderLines> response) {
                        ResponseCurrentOrderLines responseBillsList = response.body();
                        if (responseBillsList != null) {
                            int mErrorCode = responseBillsList.getResult();
                            if (mErrorCode == 0) {
                                List<OrdersLinesList> ordersLinesLists = responseBillsList.getOrdersList();

                                for (int i = 0; i < ordersLinesLists.size(); i++) {
                                    OrdersLinesList ordersLinesList = ordersLinesLists.get(i);

                                    String mAsssortmentName = ordersLinesList.getAssortimentName();
                                    Double mCount = ordersLinesList.getCount();
                                    String mUid = ordersLinesList.getUid();
                                    int mPreparationRate = ordersLinesList.getPreparationRate();
                                    int mNumber = ordersLinesList.getNumber();
                                    int mState = ordersLinesList.getState();
                                    int mMinutesLeft = ordersLinesList.getMinutesLeft();

                                    if (mPreparationRate != 0) {
                                        if (mState == 2) {
                                            HashMap<String, Object> orderMap = new HashMap<>();
                                            orderMap.put("Name", mAsssortmentName);
                                            orderMap.put("Count", String.format("%.2f", Double.valueOf(mCount)));
                                            orderMap.put("PrepRate", mPreparationRate);
                                            orderMap.put("Number", mNumber);
                                            orderMap.put("Uid", mUid);
                                            orderMap.put("MinutesLeft", mMinutesLeft);
                                            list_in_queque.add(orderMap);
                                        }
                                        if (mState == 3) {
                                            int time_remain = 0;
                                            String text_time = "Оставшееся время:";
                                            if (mPreparationRate - mMinutesLeft < 0) {
                                                time_remain = Math.abs(mPreparationRate - mMinutesLeft);
                                            } else {
                                                time_remain = mPreparationRate - mMinutesLeft;
                                            }
                                            HashMap<String, Object> orderMap = new HashMap<>();
                                            orderMap.put("Name", mAsssortmentName);
                                            orderMap.put("Count", String.format("%.2f", Double.valueOf(mCount)));
                                            orderMap.put("PrepRate", mPreparationRate);
                                            orderMap.put("Number", mNumber);
                                            orderMap.put("Minutes", mMinutesLeft);
                                            orderMap.put("Uid", mUid);
                                            if (time_remain < 60 && mPreparationRate - mMinutesLeft > 0) {
                                                orderMap.put("MinutesLeft", time_remain + " минут");
                                            } else if (time_remain < 60 && mPreparationRate - mMinutesLeft < 0) {
                                                orderMap.put("MinutesLeft", "- " + time_remain + " минут");
                                            } else if (time_remain > 60 && time_remain < 180) {
                                                orderMap.put("MinutesLeft", "- 1 час и " + (time_remain - 60) + " мин.");
                                            } else if (time_remain > 180) {
                                                orderMap.put("MinutesLeft", "- " + time_remain + " минут");
                                            }
                                            else if(mPreparationRate - mMinutesLeft == 0){
                                                orderMap.put("MinutesLeft", "0 минут");
                                            }
                                            list_executable.add(orderMap);
                                        }
                                    }
                                }
                                mHandlerBills.obtainMessage(MESSAGE_SUCCES).sendToTarget();
                            } else {
                                mHandlerBills.obtainMessage(MESSAGE_RESULT_CODE, mErrorCode).sendToTarget();
                            }
                        } else {
                            mHandlerBills.obtainMessage(MESSAGE_NULL_BODY).sendToTarget();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseCurrentOrderLines> call, Throwable t) {
                        mHandlerBills.obtainMessage(MESSAGE_FAILURE, t.getMessage()).sendToTarget();
                    }
                });
            }
        });
        mGetBillsList.start();
    }

    private final Handler mHandlerBills = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SUCCES) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list_in_queque.sort(new Comparator<HashMap<String, Object>>() {
                        @Override
                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                            return Integer.valueOf(o1.get("Number").toString()).compareTo(Integer.valueOf(o2.get("Number").toString()));
                        }
                    });
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list_executable.sort(new Comparator<HashMap<String, Object>>() {
                        @Override
                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                            return Integer.valueOf(o1.get("Number").toString()).compareTo(Integer.valueOf(o2.get("Number").toString()));
                        }
                    });
                }
//                SimpleAdapter adapterExecut = new SimpleAdapter(MainActivity.this, list_executable, R.layout.item_execut, new String[]{"Name", "MinutesLeft", "Number", "Count"},
//                        new int[]{R.id.text_assortment_name_fin, R.id.time_remain, R.id.txt_no_order_fin, R.id.txt_count_fin});
                lv_queque.setAdapter(adapterQueque);
                lv_execut.setAdapter(adapterExecutable);

            } else if (msg.what == MESSAGE_RESULT_CODE) {

            }
        }
    };

    private void startTimetaskSync() {
        timerTaskUpdate = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getOrderLines(ip, port, terminalID);
                        Log.d("TimeUpdate", String.valueOf(period));
                    }
                });
            }
        };
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        ip = Settings.getString("IP", "");
        port = Settings.getString("Port", "");
        period = Settings.getInt("period", 0);

        keyStart = Settings.getBoolean("Key", false);

        if (keyStart) {
            bomj_image.setVisibility(View.INVISIBLE);

            getOrderLines(ip, port, terminalID);

            if (period != 0) {
                updateOrders = new Timer();
                startTimetaskSync();
                updateOrders.schedule(timerTaskUpdate, period, period);
            }
        } else {
            bomj_image.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Licenta nu este valida!", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE

                },
                12);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 12 && grantResults.length == 4) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
            } else if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
            } else if (grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
