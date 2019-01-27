package com.armandasalmd.weeklyroutine;

import android.graphics.Typeface;
import android.os.Bundle;

import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AhoyOnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AhoyOnboarderCard card1 = new AhoyOnboarderCard(getString(R.string.plans), getString(R.string.plans_description), R.drawable.plans);
        AhoyOnboarderCard card2 = new AhoyOnboarderCard(getString(R.string.specialEvents), getString(R.string.special_description), R.drawable.specials);
        AhoyOnboarderCard card3 = new AhoyOnboarderCard(getString(R.string.todo), getString(R.string.todo_description), R.drawable.todo);

        List<AhoyOnboarderCard> pages = new ArrayList<>();
        pages.add(card1);
        pages.add(card2);
        pages.add(card3);

        for (AhoyOnboarderCard card : pages) {
            card.setBackgroundColor(R.color.black_transparent);
            card.setTitleColor(R.color.white);
            card.setDescriptionColor(R.color.grey_200);
        }

        showNavigationControls(true);
        setGradientBackground();
        setFinishButtonTitle("Baigti");
        Typeface face = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        setFont(face);
        setOnboardPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        switch (OpenData.askForTuto) {
            case 0: OpenData.askForTuto = 1;
                break;
            case 2: OpenData.askForTuto = 0;
                break;
        }
        finish();
    }
}
