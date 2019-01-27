package com.armandasalmd.weeklyroutine.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.armandasalmd.weeklyroutine.R;

import static android.os.Build.*;

public class AnimationUtils {

    public interface AnimationFinishedListener {
        void onAnimationFinished();
    }

    private static int getMediumDuration(Context context) {
        return context.getResources().getInteger(android.R.integer.config_longAnimTime);
    }

    private static void registerCircularRevealAnimation(final Context context, final View view, final RevealAnimationSetting revealSettings) {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @TargetApi(VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                //Simply use the diagonal of the view
                int cx = revealSettings.getCenterX();
                int cy = revealSettings.getCenterY();
                int width = revealSettings.getWidth();
                int height = revealSettings.getHeight();

                float finalRadius = (float) Math.sqrt(width * width + height * height);
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                anim.setDuration(getMediumDuration(context));
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.start();
                startBackgroundColorAnimation(view, 1, 0, getMediumDuration(context));
            }
        });
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private static void startCircularRevealExitAnimation(Context context, final View view, RevealAnimationSetting revealSettings, final AnimationFinishedListener listener) {
        int cx = revealSettings.getCenterX();
        int cy = revealSettings.getCenterY();
        int width = revealSettings.getWidth();
        int height = revealSettings.getHeight();

        float initRadius = (float) Math.sqrt(width * width + height * height);
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius, 0);
        anim.setDuration(getMediumDuration(context));
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Important: This will prevent the view's flashing (visible between the finished animation and the Fragment remove)
                view.setVisibility(View.GONE);
                listener.onAnimationFinished();
            }
        });
        anim.start();
        startBackgroundColorAnimation(view, 0, 1, getMediumDuration(context));
    }

    private static void startBackgroundColorAnimation(final View view, float startAlpha, final float endAlpha, int duration) {
        final View colorView = view.findViewById(R.id.colorView);

        Animation fadeOut = new AlphaAnimation(startAlpha, endAlpha);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(duration);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                if (endAlpha == 0)
                    colorView.setVisibility(View.GONE);
                else
                    colorView.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        colorView.startAnimation(fadeOut);
    }

    //Specific cases for our share link screen

    public static void registerCreateShareLinkCircularRevealAnimation(Context context, View view, RevealAnimationSetting revealSettings, boolean anim) {
        if (anim)
            registerCircularRevealAnimation(context, view, revealSettings);
        else
            view.findViewById(R.id.colorView).setVisibility(View.GONE);
        OpenData.createPlansIsShown = true;
    }

    public static void startCreateShareLinkCircularRevealExitAnimation(Context context, View view, RevealAnimationSetting revealSettings, AnimationFinishedListener  listener, boolean anim) {
        if (anim)
            startCircularRevealExitAnimation(context, view, revealSettings, listener);
        else
            listener.onAnimationFinished();
        OpenData.createPlansIsShown = false;
    }
}