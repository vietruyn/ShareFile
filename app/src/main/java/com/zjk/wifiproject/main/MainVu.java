package com.zjk.wifiproject.main;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.legacy.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zjk.wifiproject.BaseApplication;
import com.zjk.wifiproject.R;
import com.zjk.wifiproject.app.AppFragment;
import com.zjk.wifiproject.config.ConfigIntent;
import com.zjk.wifiproject.connection.CreateConnectionActivity;
import com.zjk.wifiproject.entity.FileState;
import com.zjk.wifiproject.entity.WFile;
import com.zjk.wifiproject.event.RefreshTipEvent;
import com.zjk.wifiproject.file.FileFragment;
import com.zjk.wifiproject.music.MusicFragment;
import com.zjk.wifiproject.picture.PictureFragment;
import com.zjk.wifiproject.presenters.Vu;
import com.zjk.wifiproject.util.A;
import com.zjk.wifiproject.util.BlurBuilder;
import com.zjk.wifiproject.util.FileUtils;
import com.zjk.wifiproject.util.L;
import com.zjk.wifiproject.util.PixelUtil;
import com.zjk.wifiproject.util.WifiUtils;
import com.zjk.wifiproject.vedio.VedioFragment;
import com.zjk.wifiproject.view.tabs.SlidingTabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 主界面的框架
 *
 * @author zyh
 * @version 1.0
 */
public class MainVu implements Vu, SendFileListener, View.OnClickListener {

    private View             view;
    private FragmentManager  fm;
    private ViewPager        mViewPager;
    private SlidingTabLayout mTabs;
    private ImageView        createButton;
    private DrawerLayout     drawer;

    //标题bar
    private ImageButton ib_menu;
    private ImageButton ib_more;
    private ImageButton ib_search;


    private View     layout_bottom;//底部隐藏布局
    private TextView tv_select_size;//选中的数目

    private Context context;
    private boolean showAnim = false;
    private List<Fragment> list;
    private Bitmap         background;
    private int lastCount = 0;

    private boolean isShowing = false;  //底部布局是否正在显示
    private boolean isAnim    = false;   //是否正在进行动画

    @Override
    public void init(LayoutInflater inflater, ViewGroup container) {
        context = inflater.getContext();
        view = inflater.inflate(R.layout.vu_main, container, false);
        bindViews();
        setListener();


    }

    /**
     * findView操作
     */
    private void bindViews() {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        createButton = (ImageView) view.findViewById(R.id.createButton);
        tv_select_size = (TextView) view.findViewById(R.id.tv_select_size);

        drawer = (DrawerLayout) view.findViewById(R.id.drawer);

        ib_menu = (ImageButton) view.findViewById(R.id.ib_menu);
        ib_more = (ImageButton) view.findViewById(R.id.ib_more);
        ib_search = (ImageButton) view.findViewById(R.id.ib_search);

        layout_bottom = view.findViewById(R.id.layout_bottom);
        ib_more.setOnClickListener(this);
    }

    @Override
    public View getView() {
        return view;
    }

    private void setListener() {
        ib_menu.setOnClickListener(this);
        createButton.setOnClickListener(this);
        tv_select_size.setOnClickListener(this);
        view.findViewById(R.id.ib_close).setOnClickListener(this);
    }

    public void setViewPager(List<Fragment> list) {
        this.list = list;
        // 设置Viewpager缓存页数
        mViewPager.setOffscreenPageLimit(list.size());
        mViewPager.setAdapter(new MainPageAdapter(fm, list));
        initSlidingTabLayout();
    }

    /**
     * 设置tab
     */
    private void initSlidingTabLayout() {
        mTabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        mTabs.setCustomTabView(R.layout.custom_tab, 0);        // Set custom tab layout
        // mTabs.setDistributeEvenly(true);  // Center the tabs in the layout
        mTabs.setSelectedIndicatorColors(Color.WHITE);    // Customize tab color
        mTabs.setViewPager(mViewPager);
    }

    /**
     * 侧滑菜单
     *
     * @param fm
     * @param drawerMenu
     */
    public void setDrawerMenu(FragmentManager fm, Fragment drawerMenu) {
        this.fm = fm;
        if (fm != null) {
            fm.beginTransaction().replace(R.id.left_drawer, drawerMenu).commit();
        } else {
            L.e("FragmentManager is null");
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ib_menu://打开或关闭菜单
                if (drawer.isDrawerOpen(Gravity.START)) {
                    drawer.closeDrawer(Gravity.START);
                } else {
                    drawer.openDrawer(Gravity.START);
                }
                break;
            case R.id.createButton://进入创建热点的界面
//                A.goOtherActivity(context, ChatActivity.class);
                createButton.setVisibility(View.GONE);
//                String blurPath = takeScreenShot((Activity)context);
                Intent intent = new Intent(context, CreateConnectionActivity.class);
//                Logger.d(blurPath);
//                intent.putExtra(ConfigIntent.EXTRA_BLUR_PATH,blurPath);
                ((Activity) context).startActivityForResult(intent, ConfigIntent.REQUEST_SHOW_CREATE);
                break;
            case R.id.ib_close:
                hideBottomLayout();
                break;
            case R.id.tv_select_size:
                A.goOtherActivity(context, CreateConnectionActivity.class);
                break;
            case R.id.ib_more:
                closeWifiOrAp();
                break;
        }
    }

    private void closeWifiOrAp() {

        View v = View.inflate(context, R.layout.pop_closewifi, null);



        final PopupWindow popWin = new PopupWindow(v, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popWin.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.card_bg));
        // popWin.setFocusable(true);
        popWin.setOutsideTouchable(true); // 点击popWin
        // 以处的区域，自动关闭
        // popWin.showAtLocation(iv_sort, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置在屏幕中的显示位置
        popWin.showAsDropDown(ib_more, 0, 0);

        v.findViewById(R.id.tv_close_ap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiUtils.closeWifiAp();
                popWin.dismiss();
                EventBus.getDefault().post(new RefreshTipEvent());
            }
        });
        v.findViewById(R.id.tv_close_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiUtils.closeWifi();
                popWin.dismiss();
                EventBus.getDefault().post(new RefreshTipEvent());
            }
        });
    }

    public String takeScreenShot(Activity activity) {

        String filePath = FileUtils.getProjectPictureDir();

        View rootView = activity.getWindow().getDecorView();
        // 允许当前窗口保存缓存信息
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();

        // 去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        File imagePath = new File(filePath, System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap = BlurBuilder.blur(context, bitmap);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
                bitmap.recycle();
                bitmap = null;
            } catch (Exception e) {
            }
            rootView.destroyDrawingCache();
            rootView.setDrawingCacheEnabled(false);
        }
        return imagePath.getAbsolutePath();
    }

    /**
     * 返回主界面
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if(resultCode == Activity.RESULT_OK){
            if(requestCode == ConfigIntent.REQUEST_SHOW_CREATE){
                shakeButtonAnimation();
            }
        }*/
        shakeButtonAnimation();
    }

    class MainPageAdapter extends FragmentPagerAdapter {

        private List<Fragment> list;
        private String tabs[] = new String[] { "application", "music", "image", "video", "file" };

        public MainPageAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }

    //------------------------ 隐藏在底部的布局---------------------------------------------
    @Override
    public void addSendFile(WFile sendFile) {
        FileState fs = new FileState(sendFile.getAbsolutePath());
        BaseApplication.sendFileStates.put(sendFile.getAbsolutePath(), fs);
        handleAnim();
    }

    @Override
    public void removeSendFile(WFile sendFile) {
        BaseApplication.sendFileStates.remove(sendFile.getAbsolutePath());
        handleAnim();
    }

    /**
     * 判断当前动画是显示还是关闭
     */
    public void handleAnim() {
        if (BaseApplication.sendFileStates.keySet().size() > 0) {
            showBottomLayout();
        } else {
            hideBottomLayout();
        }
    }

    /**
     * 展示底部布局
     */
    private void showBottomLayout() {
        if (lastCount == 0 && BaseApplication.sendFileStates.keySet().size() > 0) {
            showAnim = true;
        } else {//如果已经出现了,就不用再展示出现动画了
            showAnim = false;
        }

        if (showAnim) {
            isShowing = true;
            // 防止出现缝隙
            final int height = layout_bottom.getHeight() - 2;
            ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(300);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    layout_bottom.setTranslationY(-height * value);
                }
            });
            va.start();
            hideButtonAnimation();
        }

        lastCount = BaseApplication.sendFileStates.keySet().size();
        tv_select_size.setText("transmission（" + lastCount + "）");
        layout_bottom.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏底部布局
     */
    private void hideBottomLayout() {
        if (!isAnim && isShowing) { //没有动画 && 布局在上方

            isAnim = true;
            isShowing = false;

            // 防止出现缝隙
            final int height = layout_bottom.getHeight() - 2;
            ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(300);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    layout_bottom.setTranslationY(height * value - height);
                    if (value >= 1) {
                        isAnim = false;
                    }
                }
            });
            va.start();
            showButtonAnimation();
        }
        BaseApplication.sendFileStates.clear();
        ((AppFragment) list.get(0)).vu.adapter.notifyDataSetChanged();
        ((MusicFragment) list.get(1)).vu.adapter.notifyDataSetChanged();
        ((PictureFragment) list.get(2)).vu.adapter.notifyDataSetChanged();
        ((VedioFragment) list.get(3)).vu.adapter.notifyDataSetChanged();
        ((FileFragment) list.get(4)).vu.adapter.notifyDataSetChanged();
        lastCount = 0;
    }

    /**
     * 抖动按钮的动画
     */
    private void shakeButtonAnimation() {
        createButton.setVisibility(View.VISIBLE);
        createButton.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.shake));
    }

    /**
     * 从下面出现按钮的动画
     */
    private void showButtonAnimation() {
        final int dis = PixelUtil.dp2px(150);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(200);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                createButton.setTranslationY((1 - value) * dis);
            }
        });
        valueAnimator.start();
    }

    /**
     * 隐藏按钮的动画
     */
    private void hideButtonAnimation() {
        final int dis = PixelUtil.dp2px(150);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(200);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                createButton.setTranslationY(value * dis);
            }
        });
        valueAnimator.start();
    }
}
