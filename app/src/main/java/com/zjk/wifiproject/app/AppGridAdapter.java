package com.zjk.wifiproject.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;

import androidx.annotation.NonNull;

import com.zjk.wifiproject.BaseApplication;
import com.zjk.wifiproject.presenters.BasePresenterAdapter;

import java.util.List;

public class AppGridAdapter extends BasePresenterAdapter<AppEntity, AppItemVu> {

    public AppGridAdapter( Context context, List<AppEntity> list) {
        super(context, list);
    }

    @Override
    protected Class<AppItemVu> getVuClass() {
        return AppItemVu.class;
    }

    /**
     * 网格的单个item的视图填充数据
     * @param position
     */
    @Override
    protected void onBindItemVu(int position) {
        AppEntity item = list.get(position);
        vu.setAppIcon(getBitmapFromDrawable(item.getIcon()));
        vu.setAppName(item.getAppName());
        vu.setAppSize(Formatter.formatFileSize(context, item.length()));
        vu.setChecked(BaseApplication.sendFileStates.containsKey(item.getAbsolutePath()));
    }
    @NonNull
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}
