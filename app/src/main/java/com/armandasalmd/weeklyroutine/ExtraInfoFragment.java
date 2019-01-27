package com.armandasalmd.weeklyroutine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.classes.ExtraDataHolder;
import com.armandasalmd.weeklyroutine.classes.OpenData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

public class ExtraInfoFragment extends SupportBlurDialogFragment {

    private ExtraDataHolder data;
    private View mView;

    private static final int TITLE = 0, DEC = 1, DATE = 2, TIMEF = 3, TIMET = 4, DURATION = 5;
    @BindViews({R.id.content_title, R.id.content_dec, R.id.content_date,
            R.id.content_time_from, R.id.content_time_to, R.id.content_duration})
    List<TextView> mTexts;

    private static final long delay = 80;
    private int animTrackId = 0;
    public final int[] cardsIds = new int[] {R.id.title_card, R.id.description_card,
            R.id.date_card, R.id.time_card, R.id.duration_card};
    private List<Integer> cards = new ArrayList<>();
    private CountDownTimer timer;

    public static ExtraInfoFragment getInstance(ExtraDataHolder data) {
        ExtraInfoFragment eif = new ExtraInfoFragment();
        eif.data = data;
        return eif;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.blur_fragment, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataIntoFields();
        OpenData.infoShown = true;

        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.key_anim), true))
            startAnim();
    }

    private void startAnim() {
        for (int i = 0; i < cards.size(); i++)
            mView.findViewById(cards.get(i)).setVisibility(View.INVISIBLE);

        timer = new CountDownTimer(cards.size() * delay + 250, delay) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (animTrackId < cards.size()) {
                    CardView view = mView.findViewById(cards.get(animTrackId));
                    Animation anim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right);
                    view.startAnimation(anim);
                    view.setVisibility(View.VISIBLE);
                    animTrackId++;
                }
            }
            @Override
            public void onFinish() {
                animTrackId = 0;
                cards = new ArrayList<>();
                timer = null;
            }
        }.start();
    }

    private void loadDataIntoFields() {
        mTexts.get(TITLE).setText(data.getTitle()); // title
        cards.add(cardsIds[TITLE]);

        if (!data.getDec().isEmpty()) { // dec
            mTexts.get(DEC).setText(data.getDec());
            mView.findViewById(cardsIds[DEC]).setVisibility(View.VISIBLE);
            cards.add(cardsIds[DEC]);
        } else
            mView.findViewById(cardsIds[DEC]).setVisibility(View.GONE);



        switch (data.getFragmentID()) { // date
            case R.string.plans: {
                mView.findViewById(cardsIds[2]).setVisibility(View.VISIBLE);
                mTexts.get(DATE).setText(data.getDate());
                cards.add(cardsIds[DATE]);
                loadTimesAndDur();
                break;
            }
            case R.string.todo: {
                mTexts.get(DATE).setVisibility(View.VISIBLE);
                mView.findViewById(cardsIds[3]).setVisibility(View.GONE);
                mView.findViewById(cardsIds[4]).setVisibility(View.GONE);

                mTexts.get(DATE).setText(data.getDate());
                cards.add(cardsIds[DATE]);
                break;
            }
            case R.string.specialEvents: {
                if (data.getDates().size() == 0)
                    mView.findViewById(cardsIds[2]).setVisibility(View.GONE);
                else {
                    String dateStr = data.datesToString();
                    mView.findViewById(cardsIds[2]).setVisibility(View.VISIBLE);
                    mTexts.get(DATE).setText(dateStr);
                    cards.add(cardsIds[DATE]);
                }
                loadTimesAndDur();
                break;
            }
        }
    }

    private void loadTimesAndDur() { // only plans and special
        cards.add(cardsIds[TIMEF]);

        if (data.isUseTimeTo()) {
            mTexts.get(TIMET).setVisibility(View.VISIBLE);
            mTexts.get(TIMEF).setText(String.format("%s %s", getString(R.string.from), data.getTimeFrom()));
            mTexts.get(TIMET).setText(String.format("%s %s", getString(R.string.to), data.getTimeTo()));

            mTexts.get(DURATION).setText(data.getDuration().toString(getContext()));
            mView.findViewById(R.id.duration_card).setVisibility(View.VISIBLE);
            cards.add(cardsIds[4]);
        } else {
            mTexts.get(TIMET).setVisibility(View.GONE);
            mTexts.get(TIMEF).setText(data.getTimeFrom());
            mView.findViewById(R.id.duration_card).setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isActionBarBlurred() {
        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        OpenData.infoShown = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDismiss(dialog);
    }

    @Override
    protected boolean isDimmingEnable() {
        return true;
    }

}
