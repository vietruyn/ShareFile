package com.zjk.wifiproject.guide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zjk.wifiproject.R;
import com.zjk.wifiproject.config.SharedKey;
import com.zjk.wifiproject.main.MainActivity;
import com.zjk.wifiproject.util.A;
import com.zjk.wifiproject.util.SP;

import java.util.ArrayList;
import java.util.List;

import rebus.permissionutils.AskAgainCallback;
import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SplashActivity extends Activity implements FullCallback{
    public static final int PERMISSIONS_REQUEST = 22;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //隐藏系统小标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏导航栏navigation bar
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_splash);

        ArrayList<PermissionEnum> permissionEnumArrayList = new ArrayList<>();
        permissionEnumArrayList.add(PermissionEnum.ACCESS_FINE_LOCATION);
        permissionEnumArrayList.add(PermissionEnum.WRITE_EXTERNAL_STORAGE);
        permissionEnumArrayList.add(PermissionEnum.RECORD_AUDIO);
        permissionEnumArrayList.add(PermissionEnum.CAMERA);

        PermissionManager.Builder()
                .permissions(permissionEnumArrayList)
                .askAgain(true)
                .askAgainCallback(new AskAgainCallback() {
                    @Override
                    public void showRequestPermission(UserResponse response) {
                        showDialog(response);
                    }
                })
                .callback(SplashActivity.this)
                .ask(this);

    }

    private void showDialog(final AskAgainCallback.UserResponse response) {
        new AlertDialog.Builder(SplashActivity.this)
                .setTitle("Permission needed")
                .setMessage("This app realy need to use this permission, you wont to authorize it?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(true);
                    }
                })
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(false);
                    }
                })
                .setCancelable(false)
                .show();
    }


    private void goMainActivity() {
        //延迟1秒
//        new Handler() {
//        }.postDelayed(new Runnable() {
//            public void run() {
//                boolean isFirst = (boolean) SP.get(context, SharedKey.isfirst, true);
//                A.goOtherActivityFinish(context, isFirst ? GuideActivity.class : MainActivity.class);
//            }
//        }, 1000);

        A.goOtherActivityFinish(context, MainActivity.class);
    }

    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    @Override
    public void result(ArrayList<PermissionEnum> permissionsGranted, ArrayList<PermissionEnum> permissionsDenied, ArrayList<PermissionEnum> permissionsDeniedForever, ArrayList<PermissionEnum> permissionsAsked) {
        List<String> msg = new ArrayList<>();
        for (PermissionEnum permissionEnum : permissionsDenied) {
            msg.add(permissionEnum.toString() + " [Denied]");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            goMainActivity();
//            createHotspot();
        }

//        String[] items = msg.toArray(new String[msg.size()]);
//        new AlertDialog.Builder(this)
//                .setTitle("Permission result")
//                .setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        goMainActivity();
//                    }
//                })
//                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int all = 0;
        if (requestCode == PERMISSIONS_REQUEST) {
            Log.d("RLV", "Number of permissions : " + permissions.length);
            for (int res : grantResults) {
                if (res == PERMISSION_GRANTED) all++;
            }
            if (all == grantResults.length) {
                Log.d("RLV", "All permissions granted");
//                createHotspot();
                goMainActivity();
            } else {
                Log.d("RLV", "permissions denied : " + (grantResults.length - all));
            }
        }
    }

}
