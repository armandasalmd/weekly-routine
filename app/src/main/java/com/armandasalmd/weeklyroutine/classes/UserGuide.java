package com.armandasalmd.weeklyroutine.classes;

import android.app.Activity;
import android.support.v7.widget.Toolbar;

import com.armandasalmd.weeklyroutine.MainActivity;
import com.armandasalmd.weeklyroutine.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UserGuide {


    public static void firstGuide(final Activity activity, Toolbar toolbar) {
        //TapTargetView.showFor(activity, TapTarget.forView(OpenData.tutoViews.get(1), "title", "description").tintTarget(false));
        final List<TapTarget> targets = new ArrayList<>();

        targets.add(TapTarget.forToolbarMenuItem(toolbar, R.id.action_lock,
                activity.getString(R.string.guideT_lock), activity.getString(R.string.guideD_lock)));
        targets.add(TapTarget.forToolbarOverflow(toolbar, activity.getString(R.string.guideT_overflow), activity.getString(R.string.guideD_overflow)));
        targets.add(TapTarget.forView(OpenData.tutoViews.get(0), activity.getString(R.string.guideT_add)).tintTarget(false));
        targets.add(TapTarget.forView(OpenData.tutoViews.get(1), activity.getString(R.string.guideT_event),
                activity.getString(R.string.guideD_event)).tintTarget(false));
        targets.add(TapTarget.forView(OpenData.tutoViews.get(2), activity.getString(R.string.guideT_special),
                activity.getString(R.string.guideD_special)).tintTarget(false));
        targets.add(TapTarget.forView(OpenData.tutoViews.get(2).findViewById(R.id.check_done), activity.getString(R.string.guideT_done),
                activity.getString(R.string.guideD_done)));

        targets.add(TapTarget.forView(OpenData.tutoViews.get(1), activity.getString(R.string.guideT_longclick)).tintTarget(false));
        targets.add(TapTarget.forView(OpenData.tutoViews.get(1).findViewById(R.id.swipe_content), activity.getString(R.string.guideT_swipe)).targetRadius(80).transparentTarget(true));

        new TapTargetSequence(activity)
                .targets(targets)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        ((MainActivity)activity).endTuto1();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if (targets.get(targets.size() - 2) == lastTarget)
                            ((MainActivity)activity).showMoreEventInfo(); // plius atidaro meniu kitam stepsui
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        Toasty.warning(activity, activity.getString(R.string.guide_canceled)).show();
                        ((MainActivity)activity).endTuto1();
                    }
                }).start();
    }

}
