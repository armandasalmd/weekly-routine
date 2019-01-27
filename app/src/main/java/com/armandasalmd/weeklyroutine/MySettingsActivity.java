package com.armandasalmd.weeklyroutine;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.armandasalmd.weeklyroutine.classes.OpenData;

public class MySettingsActivity extends AppCompatPreferenceActivity {

    private boolean[] wipe = new boolean[3];
    public static final int code_key = 533;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        SettingsFragment fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            endThis();
        return true;
    }

    @Override
    public void onBackPressed() {
        endThis();
    }

    private void endThis() {
        Intent intent = new Intent();
        intent.putExtra(Integer.toString(code_key), wipe);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    public void notAboutWipe(boolean[] values) {
        for (int i = 0; i < values.length; i++)
            if (values[i])
                wipe[i] = true;
        //((MainActivity)getParent()).notAboutWipe(values);
        //BusStation.getBus(0).post(values);
    }

    public static class SettingsFragment extends PreferenceFragment {
        //private InterstitialAd mInterstitialAd;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_settings, rootKey);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            PreferenceManager man = getPreferenceManager();
            Preference pRate = man.findPreference(getString(R.string.key_rate)),
                       //pAd = man.findPreference(getString(R.string.key_ad)),
                       pRestore = man.findPreference(getString(R.string.key_restore)),
                       pSep = man.findPreference(getString(R.string.key_separator)),
                       pWipe = man.findPreference(getString(R.string.key_wipe));


            pRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                    }
                    return false;
                }
            });

            pWipe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String str = newValue.toString();
                    if (str.equals("[]"))
                        return false;
                    else {
                        boolean[] match = new boolean[3];
                        if (str.contains("plans")) // plans
                            match[0] = true;
                        if (str.contains("Special")) //special
                            match[1] = true;
                        if (str.contains("list")) //todo
                            match[2] = true;
                        ((MySettingsActivity)getActivity()).notAboutWipe(match);
                    }
                    // [Week plans, To do list, Special events]
                    return false;
                }
            });

            pRestore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.restore_title)
                            .setMessage(R.string.sure)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear();
                                    editor.apply();
                                    ((MySettingsActivity)getActivity()).restartThis();
                                }
                            }).setNegativeButton(getString(R.string.no), null).show();

                    return false;
                }
            });

            pSep.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    OpenData.changeSep(((String)newValue).charAt(0));
                    return true;
                }
            });

            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    private void restartThis() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}
