package edi.md.cookmonitor;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edi.md.cookmonitor.NetworkUtils.ApiUtils;
import edi.md.cookmonitor.NetworkUtils.CommandServices;
import edi.md.cookmonitor.NetworkUtils.ServiceResultAndBody.SimpleResultService;
import edi.md.cookmonitor.utils.UpdateHelper;
import edi.md.cookmonitor.utils.UpdateInformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity  implements UpdateHelper.OnUpdateCheckListener{
    TextView txtCod,tv_device_id, textAppVersion;
    EditText et_adress, et_port,key_input;
    Button btn_test,btn_verific;
    ProgressBar pgBar;
    SharedPreferences Settings;
    Spinner spinner_update;
    ProgressDialog pgH;

    private String ip_address,device_id,port;

    private RadioButton rd_btnCookMonitor,rd_btnOrderMonitor;

    Button updateApp;
    ToggleButton selectModeWork;
    RadioGroup radioGroup;
    ImageButton ruLang;
    ImageButton roLang;
    ImageButton enLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        et_adress=findViewById(R.id.et_ip_conect);
        et_port=findViewById(R.id.et_port_conect);
        btn_test=findViewById(R.id.btn_test_conect);
        pgBar=findViewById(R.id.progressBar_conect);
        txtCod= findViewById(R.id.txt_cod_licenta);
        key_input = findViewById(R.id.et_input_licenta);
        btn_verific = findViewById(R.id.btn_verific_licenta);
        tv_device_id = findViewById(R.id.txt_device_id);
        spinner_update = findViewById(R.id.spinner_time_update);
        updateApp = findViewById(R.id.button);
        selectModeWork = findViewById(R.id.toggleButton);
        radioGroup = findViewById(R.id.radioGroup);
        rd_btnCookMonitor = findViewById(R.id.btn_cook_monitor);
        rd_btnOrderMonitor = findViewById(R.id.rd_order_monitor);
        textAppVersion = findViewById(R.id.textAppVersion);
        ruLang = findViewById(R.id.select_lng_ru);
        roLang = findViewById(R.id.select_lng_ro);
        enLang = findViewById(R.id.select_lng_en);

        Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences.Editor inputSeting =Settings.edit();
        pgH = new ProgressDialog(SettingsActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        et_adress.setText(Settings.getString("IP",""));
        et_port.setText(Settings.getString("Port",""));

        int workMethod = Settings.getInt("WorkAs",BaseEnum.NONE_SELECTED_MODE);
        int workMode = Settings.getInt("ModeWork",BaseEnum.NoneMode);

        if(workMethod == BaseEnum.CookMonitor){
            rd_btnCookMonitor.setChecked(true);
        }
        else if( workMethod == BaseEnum.OrderMonitor){
            rd_btnOrderMonitor.setChecked(true);
        }

        if(workMode == BaseEnum.OneMode) {
            rd_btnCookMonitor.setEnabled(false);
            rd_btnOrderMonitor.setEnabled(false);
            selectModeWork.setChecked(false);
        }
        else{
            rd_btnCookMonitor.setEnabled(true);
            rd_btnOrderMonitor.setEnabled(true);
            selectModeWork.setChecked(true);
        }

        final String tmDevice, androidId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }
        }

        //ask necessary permisions
        AskForPermissions();

        tmDevice = "KitKatABCDEFGHIJKLMNOPQRSTUVWXYZMars";
        androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID( androidId.hashCode(), tmDevice.hashCode());
        String deviceId = deviceUuid.toString();

        tv_device_id.setText(deviceId);
        device_id = deviceId;
        inputSeting.putString("DeviceId",deviceId);
        inputSeting.apply();

        deviceId=deviceId.replace("-","");
        deviceId=deviceId.replace("f","q");
        deviceId=deviceId.replace("1","t");
        deviceId=deviceId.replace("3","s");
        deviceId=deviceId.replace("5","o");
        deviceId=deviceId.replace("9","x");
        deviceId=deviceId.replace("6","a");

        String code =  Settings.getString("CodeLicense","");
        if(code.equals("")){
            for (int k = 0; k < deviceId.length(); k++) {
                if (Character.isLetter(deviceId.charAt(k))) {
                    code = code + deviceId.charAt(k);
                }
            }
            code = code.substring(0, 8);
            txtCod.setText(code.toUpperCase());

            SharedPreferences.Editor keyEdit = Settings.edit();
            keyEdit.putString("CodeLicense",code.toUpperCase());
            keyEdit.apply();
        }
        else{
            txtCod.setText(code.toUpperCase());
        }
        key_input.setText(Settings.getString("KeyText",""));

        final String internKey = md5(code.toUpperCase() + "ENCEFALOMIELOPOLIRADICULONEVRITA");

        List<String> categories_spiner = new ArrayList<String>();
        categories_spiner.add("Manual");
        categories_spiner.add("10 secunde");
        categories_spiner.add("15 secunde");
        categories_spiner.add("30 secunde");
        categories_spiner.add("1 minut");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spiner_item, categories_spiner);
        spinner_update.setAdapter(dataAdapter);

        spinner_update.setSelection(Settings.getInt("position",0));

        spinner_update.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                SharedPreferences.Editor editor_sync = Settings.edit();
                switch (position){
                    case 0:{
                        editor_sync.putInt("period",0);
                        editor_sync.putInt("position",0);
                        editor_sync.apply();
                    }break;
                    case 1:{
                        editor_sync.putInt("period",10000);
                        editor_sync.putInt("position",1);
                        editor_sync.apply();
                    }break;
                    case 2:{
                        editor_sync.putInt("period",15000);
                        editor_sync.putInt("position",2);
                        editor_sync.apply();
                    }break;
                    case 3:{
                        editor_sync.putInt("period",30000);
                        editor_sync.putInt("position",3);
                        editor_sync.apply();
                    }break;
                    case 4:{
                        editor_sync.putInt("period",60000);
                        editor_sync.putInt("position",4);
                        editor_sync.apply();
                    }break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_verific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = key_input.getText().toString().toUpperCase();
                SharedPreferences.Editor ed = Settings.edit();

                if (Test(key,internKey)){
                    key_input.setBackgroundResource(R.drawable.ping_true_conect);
                    ed.putBoolean("Key",true);
                    ed.putString("KeyText",key);
                    ed.apply();
                }
                else{
                    key_input.setBackgroundResource(R.drawable.ping_false_connect);
                    ed.putBoolean("Key",false);
                    ed.putString("KeyText",key_input.getText().toString().toUpperCase());
                    ed.apply();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                finish();
            }
        });

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgBar.setVisibility(ProgressBar.VISIBLE);
                inputSeting.putString("IP",et_adress.getText().toString());
                inputSeting.putString("Port",et_port.getText().toString());
                inputSeting.apply();

                ip_address = et_adress.getText().toString();
                port = et_port.getText().toString();

                CommandServices commandServices = ApiUtils.commandService(ip_address + ":" + port);
                Call<Boolean> call = commandServices.ping();

                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.body() != null && response.body()) {
                            et_adress.setBackgroundResource(R.drawable.ping_true_conect);

                            CommandServices commandServices = ApiUtils.commandService(ip_address + ":" + port);
                            Call<SimpleResultService> callReg = commandServices.registerDevice(device_id);

                            callReg.enqueue(new Callback<SimpleResultService>() {
                                @Override
                                public void onResponse(Call<SimpleResultService> call, Response<SimpleResultService> response) {
                                    SimpleResultService simpleResultService = response.body();

                                    if(simpleResultService != null && simpleResultService.getResult() == 0) {
                                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                                        Toast.makeText(SettingsActivity.this, "Dispozitivul este inregistrat!", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(SettingsActivity.this, "Dispozitivul nu este inregistrat! Eroare:" + simpleResultService.getResult(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<SimpleResultService> call, Throwable t) {
                                    Toast.makeText(SettingsActivity.this, "Dispozitivul nu este inregistrat! Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            pgBar.setVisibility(ProgressBar.INVISIBLE);
                            et_adress.setBackgroundResource(R.drawable.ping_false_connect);
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        et_adress.setBackgroundResource(R.drawable.ping_false_connect);
                    }
                });
            }
        });

        selectModeWork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    rd_btnCookMonitor.setEnabled(true);
                    rd_btnOrderMonitor.setEnabled(true);
                    Settings.edit().putInt("ModeWork",BaseEnum.DoubleMode).apply();
                }
                else{
                    rd_btnCookMonitor.setEnabled(false);
                    rd_btnOrderMonitor.setEnabled(false);
                    Settings.edit().putInt("ModeWork",BaseEnum.OneMode).apply();
                }
            }
        });

        rd_btnCookMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.edit().putInt("WorkAs",BaseEnum.CookMonitor).apply();
            }
        });
        rd_btnOrderMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.edit().putInt("WorkAs",BaseEnum.OrderMonitor).apply();
            }
        });

        ruLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        roLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        enLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        updateApp.setOnClickListener(v -> {
            pgH.setMessage("loading...");
            pgH.setIndeterminate(true);
            pgH.show();
            UpdateHelper.with(SettingsActivity.this).onUpdateCheck(this).check();
        });



        textAppVersion.setText(getResources().getString(R.string.app_name) + " "  + getAppVersion(this));
    }
    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            byte[] encode = Base64.encode(messageDigest,0);
            String respencode = new String(encode).toUpperCase();
            // Create String
            String digits="";
            for (int i = 0; i < respencode.length(); i++) {
                char chrs = respencode.charAt(i);
                if (!Character.isDigit(chrs))
                    digits = digits+chrs;
            }
            String keyLic = "";
            for (int k=0;k<digits.length();k++){
                if (Character.isLetter(digits.charAt(k))){
                    keyLic=keyLic + digits.charAt(k);
                }
            }
            keyLic=keyLic.substring(0,8);

            return keyLic;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void AskForPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int install_packages_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES);
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (install_packages_permission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    private String getAppVersion(Context context){
        String result = "";
        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean Test (String key,String entern_key){
        return key.equals(entern_key);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onUpdateCheckListener(UpdateInformation information) {
        boolean update = information.isUpdate();
        pgH.dismiss();

        if(update && !information.getNewVerion().equals(information.getCurrentVersion())){
            AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme)
                    .setTitle("New version " + information.getNewVerion() + " available")
                    .setMessage("You can update to new version.Current version: " + information.getCurrentVersion())
                    .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                        pgH.setMessage("download new version...");
                        pgH.setIndeterminate(true);

                        pgH.show();
                        downloadAndInstallApk(information.getUrl());
                    })
                    .setNegativeButton("Nu,mersi", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create();
            alertDialog.show();
        }
    }

    private void downloadAndInstallApk(String url){
        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better

        String destination = Environment.getExternalStorageDirectory()+ "/IntelectSoft";
        String fileName = "/cook.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //set download manager

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download new version...");
        request.setTitle("MonitorHelper update");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) SettingsActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                pgH.dismiss();
                File file = new File(Environment.getExternalStorageDirectory()+ "/IntelectSoft","/cook.apk"); // mention apk file path here

                Uri uri = FileProvider.getUriForFile(SettingsActivity.this, BuildConfig.APPLICATION_ID + ".provider",file);
                if(file.exists()){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                }
                unregisterReceiver(this);
                finish();

            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
