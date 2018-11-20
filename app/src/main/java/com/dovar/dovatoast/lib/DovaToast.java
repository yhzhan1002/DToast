package com.dovar.dovatoast.lib;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.dovar.dovatoast.R;
import com.dovar.dovatoast.lib.inner.IToast;
import com.dovar.dovatoast.lib.inner.TN;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description: 使用系统Toast的问题 {@link android.widget.Toast}：当通知权限被关闭时在华为等手机上Toast不显示。因此采取自定义Toast解决.
 * 使用{@link DovaToast}出现{@link WindowManager.BadTokenException}时，
 * 如果通知权限未被关闭，则改用{@link com.dovar.dovatoast.lib.inner.SystemToast}，否则使用{@link com.dovar.dovatoast.lib.inner.ActivityToast}
 */
public class DovaToast implements Cloneable, IToast {

    private Context mContext;
    private View contentView;
    private int animation = android.R.style.Animation_Toast;
    private int gravity = Gravity.BOTTOM | Gravity.CENTER;
    private int xOffset;
    private int yOffset;
    private int width = WindowManager.LayoutParams.WRAP_CONTENT;
    private int height = WindowManager.LayoutParams.WRAP_CONTENT;
    private int priority;//优先级
    private @Duration
    int duration = DURATION_SHORT;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DURATION_SHORT, DURATION_LONG})
    public @interface Duration {
    }

    public static final int DURATION_SHORT = 2000;
    public static final int DURATION_LONG = 3500;

    /**
     * @param mContext 建议使用Activity。如果使用AppContext则当通知权限被禁用且TYPE_TOAST被WindowManager.addView()抛出异常时，无法正常显示弹窗。
     *                 在API25+的部分手机上TYPE_TOAST被WindowManager.addView()时会抛出异常
     */
    public DovaToast(@NonNull Context mContext) {
        this.mContext = mContext;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) throw new RuntimeException("LayoutInflater is null!");
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null);
    }

    public WindowManager.LayoutParams getWMParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //申请悬浮窗权限
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
                String[] permission = {Manifest.permission.SYSTEM_ALERT_WINDOW};
                ActivityCompat.requestPermissions((Activity) mContext, permission, 1);
            }
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {*/
        lp.type = WindowManager.LayoutParams.TYPE_TOAST;
//        }

        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            mParams.y = mToast.getYOffset();
        } else {
            Context topActivityOrApp = Utils.getTopActivityOrApp();
            if (topActivityOrApp instanceof Activity) {
                mWM = ((Activity) topActivityOrApp).getWindowManager();
            }
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            mParams.y = mToast.getYOffset() + getNavBarHeight();
        }*/

        lp.height = this.height;
        lp.width = this.width;
        lp.windowAnimations = this.animation;
        lp.gravity = this.gravity;
        lp.x = this.xOffset;
        lp.y = this.yOffset;
        return lp;
    }

    //展示Toast
    @Override
    public void show() {
        TN.instance().add(this);
    }

    /**
     * 取消Toast,会清除队列中所有Toast任务
     * 因为TN中使用的是{@link this#clone()}，外部没有Toast队列中单个任务的引用，所以外部无法单独取消一个Toast任务
     */
    @Override
    public void cancel() {
        TN.instance().cancelAll();
    }

    public Context getContext() {
        return this.mContext;
    }

    public DovaToast setView(View mView) {
        this.contentView = mView;
        return this;
    }

    public View getView() {
        return this.contentView;
    }

    public DovaToast setDuration(@Duration int duration) {
        this.duration = duration;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    public DovaToast setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    /**
     * @param gravity {@link android.view.Gravity#TOP etc..}
     * @param xOffset pixel
     * @param yOffset pixel
     */
    public DovaToast setGravity(int gravity, int xOffset, int yOffset) {
        this.gravity = gravity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    public DovaToast setGravity(int gravity) {
        return setGravity(gravity, 0, 0);
    }

    public int getGravity() {
        return gravity;
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public int getPriority() {
        return priority;
    }

    public DovaToast setPriority(int mPriority) {
        this.priority = mPriority;
        return this;
    }

    /**
     * Toast引用的contentView的可见性
     *
     * @return toast是否正在展示
     */
    public boolean isShowing() {
        return contentView != null && contentView.isShown();
    }

    @Override
    public DovaToast clone() {
        DovaToast mToast = null;
        try {
            mToast = (DovaToast) super.clone();
            mToast.mContext = this.mContext;
            mToast.contentView = this.contentView;
            mToast.duration = this.duration;
            mToast.animation = this.animation;
            mToast.gravity = this.gravity;
            mToast.height = this.height;
            mToast.width = this.width;
            mToast.xOffset = this.xOffset;
            mToast.yOffset = this.yOffset;
            mToast.priority = this.priority;
        } catch (CloneNotSupportedException mE) {
            mE.printStackTrace();
        }
        return mToast;
    }
}
