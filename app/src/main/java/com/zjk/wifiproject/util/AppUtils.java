package com.zjk.wifiproject.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.zjk.wifiproject.app.AppEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * App工具类
 */
public class AppUtils {

    /**
     * 返回用户已安装应用列表
     */
    public static List<AppEntity> getAppList(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        List<AppEntity> appInfos = new ArrayList<AppEntity>();
        for (PackageInfo packageInfo : packageInfos) {

            ApplicationInfo app = packageInfo.applicationInfo;
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                // 非系统应用
                File apkfile = new File(app.sourceDir);
                PackageStats stats = new PackageStats(packageInfo.packageName);
                AppEntity appInfo = new AppEntity(app.sourceDir);
                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setVersionCode(packageInfo.versionCode);
                appInfo.setVersionName(packageInfo.versionName);
                appInfo.setUid(app.uid);
                appInfo.setIcon(app.loadIcon(pm));
                appInfo.setAppName(app.loadLabel(pm).toString());
                appInfo.setCacheSize(stats.cacheSize);
                appInfo.setDataSize(stats.dataSize);
                appInfos.add(appInfo);
            }

        }

        return appInfos;
    }



}
