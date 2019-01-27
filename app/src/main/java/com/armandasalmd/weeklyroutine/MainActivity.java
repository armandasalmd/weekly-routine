package com.armandasalmd.weeklyroutine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.armandasalmd.weeklyroutine.classes.BusStation;
import com.armandasalmd.weeklyroutine.classes.Day;
import com.armandasalmd.weeklyroutine.classes.Event;
import com.armandasalmd.weeklyroutine.classes.OpenData;
import com.armandasalmd.weeklyroutine.classes.SortHelper;
import com.armandasalmd.weeklyroutine.classes.UserGuide;
import com.armandasalmd.weeklyroutine.fragments.FragmentPlans;
import com.armandasalmd.weeklyroutine.fragments.FragmentSpecial;
import com.armandasalmd.weeklyroutine.fragments.FragmentTodo;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.armandasalmd.weeklyroutine.classes.OpenData.askForTuto;
import static com.armandasalmd.weeklyroutine.classes.OpenData.combineSpecialEvents;
import static com.armandasalmd.weeklyroutine.classes.OpenData.combined;
import static com.armandasalmd.weeklyroutine.classes.OpenData.deleteSpecialsFromEvents;
import static com.armandasalmd.weeklyroutine.classes.OpenData.removePastDates;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentPlans fragPlans;
    private FragmentTodo fragTodo;
    private FragmentSpecial fragSpecial;
    private int currentFrag = R.string.plans;
    private Menu mMenu;
    private MaterialMenuDrawable materialMenu;

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (OpenData.mDays == null) {
            OpenData.load(this);
            if (OpenData.currentMaxNotifId < 1)
                startActivity(new Intent(getApplicationContext(), TutorialActivity.class));
        }

        toolbar.setTitle(getString(R.string.plans));
        toolbar.setSubtitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        setDrawerListerer();
        initBurgerIcon();

        View view = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);
        TextView tv = view.findViewById(R.id.text_today);
        tv.setText(OpenData.prevDate(OpenData.getCurrentDate(this)));

        navigationView.setNavigationItemSelectedListener(this);
        if (findViewById(R.id.main_container) != null) {
            fragPlans = new FragmentPlans();
            removePastDates(PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.key_delete_special), false));
            combineSpecialEvents();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragPlans).commit();
            if (savedInstanceState != null) {
                navigationView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.setCheckedItem(R.id.nav_main);
                    }
                }, 500);
            }
        }
    }

    public void initBurgerIcon() {
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        toolbar.setNavigationIcon(materialMenu);
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OpenData.createPlansIsShown)
                    createPlanStop();
                else if (!drawerLayout.isDrawerOpen(Gravity.LEFT))
                    drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    private void setDrawerListerer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (currentFrag == R.string.specialEvents) {
                    fragSpecial.removeSecondTabFocus();
                    fragSpecial.closeMenus();
                }
                else if (currentFrag == R.string.plans)
                    fragPlans.closeMenus();
                else if (currentFrag == R.string.todo)
                    fragTodo.drawerOpened();
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void lockDrawer(boolean lock) {
        if (lock)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void startFragment(int string_id) {
        if (currentFrag != string_id) {
            currentFrag = string_id;
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null : "null at startFrag";
            assert getCurrentFocus() != null : "null at startFrag";
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


            switch (string_id) {
                case R.string.plans:
                    combineSpecialEvents();
                    fragPlans = new FragmentPlans();
                    transaction.replace(R.id.main_container, fragPlans);

                    mMenu.findItem(R.id.action_lock).setVisible(true);
                    mMenu.findItem(R.id.action_circle_add).setVisible(false);
                    mMenu.findItem(R.id.action_delete_no_dates).setVisible(false);
                    //lockMode(true);
                    mMenu.findItem(R.id.action_lock).setIcon(R.drawable.ic_locked);
                    mMenu.findItem(R.id.action_lock).setTitle("Užrakinta");
                    // o isdisable vyksta onNavItemClick
                    mMenu.findItem(R.id.action_done).setVisible(true);
                    mMenu.findItem(R.id.action_sort).setVisible(false);
                    break;
                case R.string.specialEvents:
                    deleteSpecialsFromEvents();
                    fragSpecial = new FragmentSpecial();
                    transaction.replace(R.id.main_container, fragSpecial);

                    mMenu.findItem(R.id.action_lock).setVisible(false);
                    mMenu.findItem(R.id.action_circle_add).setVisible(false);
                    mMenu.findItem(R.id.action_delete_no_dates).setVisible(true);
                    mMenu.findItem(R.id.action_done).setVisible(false);
                    mMenu.findItem(R.id.action_sort).setVisible(true);
                    break;
                case R.string.todo:
                    deleteSpecialsFromEvents();
                    fragTodo = new FragmentTodo();
                    transaction.replace(R.id.main_container, fragTodo);

                    mMenu.findItem(R.id.action_lock).setVisible(false);
                    mMenu.findItem(R.id.action_circle_add).setVisible(true).setIcon(R.drawable.ic_plus_circle);
                    mMenu.findItem(R.id.action_delete_no_dates).setVisible(false);
                    mMenu.findItem(R.id.action_done).setVisible(false);
                    mMenu.findItem(R.id.action_sort).setVisible(true);
                    break;
            }
            transaction.commit();
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(string_id));

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (OpenData.createPlansIsShown)
                createPlanStop();
            else
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    private void lockMode(boolean locked) {
        MenuItem item = mMenu.findItem(R.id.action_lock);
        if (locked) {
            item.setIcon(R.drawable.ic_locked);
            item.setTitle(R.string.locked);

        }else {
            item.setIcon(R.drawable.ic_unlocked);
            item.setTitle(R.string.unlocked);
        }
        fragPlans.setLocked(locked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startSettings();
                return true;
            case R.id.action_lock:
                lockMode(!OpenData.lockMode);
                return true;
            case R.id.action_circle_add:
                if (OpenData.createPlansIsShown)
                    try {
                        fragPlans.toolbarAddClick();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                else
                    fragTodo.addEvent();
                return true;
            case R.id.action_cancel:
                fragTodo.cancelEditing();
                break;
            case R.id.action_delete_no_dates:
                if (currentFrag == R.string.specialEvents)
                    fragSpecial.removeEventsWithoutDate();
                break;
            case R.id.sort_az:
                if (currentFrag == R.string.specialEvents)
                    fragSpecial.requestToSort(SortHelper.AZ);
                else
                    fragTodo.requestToSort(SortHelper.AZ);
                break;
            case R.id.sort_za:
                if (currentFrag == R.string.specialEvents)
                    fragSpecial.requestToSort(SortHelper.ZA);
                else
                    fragTodo.requestToSort(SortHelper.ZA);
                break;
            case R.id.sort_time:
                if (currentFrag == R.string.specialEvents)
                    fragSpecial.requestToSort(SortHelper.TIME);
                else
                    fragTodo.requestToSort(SortHelper.TIME);
                break;
            case R.id.action_alldone:
            case R.id.action_allnotdone:
                if (currentFrag == R.string.plans)
                    fragPlans.makeAllDone(id == R.id.action_alldone);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSettings() {
        Intent settings = new Intent(getApplicationContext(), MySettingsActivity.class);
        startActivityForResult(settings, MySettingsActivity.code_key); // stt (ngs)
        //Toasty.info(this, getString(R.string.progress)).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MySettingsActivity.code_key && resultCode == RESULT_OK) { // wipe info
            boolean[] values = data.getBooleanArrayExtra(Integer.toString(MySettingsActivity.code_key));
            //do stuff
            if (values[0])
                for (Day day : OpenData.mDays)
                    day.setEvents(new ArrayList<Event>());
            if (values[1]) {
                OpenData.mSpecials = new ArrayList<>();
            }
            if (values[2])
                OpenData.mTodo = new ArrayList<>();

            if (currentFrag == R.string.todo && values[2])
                fragTodo.afterWipe();
            else if (currentFrag == R.string.specialEvents && values[1])
                fragSpecial.firstTab.afterWipe();
            else if (currentFrag == R.string.plans && values[0]) {
                if (!values[1])
                    OpenData.combineSpecialEvents();
                fragPlans.afterWipe();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (currentFrag == R.string.todo) {
            busStopPlansAndTodo(false);
        }

        if (id == R.id.nav_main) {
            startFragment(R.string.plans);
        } else if (id == R.id.nav_special) {
            startFragment(R.string.specialEvents);
        } else if (id == R.id.nav_todo) {
            startFragment(R.string.todo);
        } else if (id == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START, false);
            startSettings();
            return true;
        } else if (id == R.id.nav_guide) {
            OpenData.tutoMode = OpenData.getWeekDayInt();
            ready = new boolean[2];
            if (currentFrag != R.string.plans) {
                navigationView.getMenu().getItem(0).setChecked(true);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
            } else {
                fragPlans.selectTodaysTab();
                startTuto1();
            }
        } else if (id == R.id.nav_about) {
            OpenData.askForTuto = 2;
            startActivity(new Intent(getApplicationContext(), TutorialActivity.class));
            return true;
        } else if (id == R.id.nav_exit) {
            finishAndRemoveTask();
            return true;
        }

        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 50);

        return true;
    }

    private boolean[] ready = new boolean[2];
    public void sayReady(int id) {
        // id = 0 - fragPlans, id = 1 - listViewFrag
        ready[id] = true;
        if (OpenData.tutoMode != -1 && ready[0] && ready[1]) startTuto1();
    }

    private void startTuto1() {
        fragPlans.adapter.addSampleItems(OpenData.tutoMode);

        OpenData.tutoViews = new ArrayList<>();
        OpenData.tutoViews.add(fragPlans.getFab());
        fragPlans.adapter.get2Views(OpenData.getWeekDayInt());
    }

    public void rowsReady(View[] views) {
        OpenData.tutoViews.add(views[0]);
        OpenData.tutoViews.add(views[1]);

        UserGuide.firstGuide(this, toolbar);
    }

    public void endTuto1() {
        fragPlans.adapter.tutoEnd(OpenData.getWeekDayInt());
        OpenData.tutoMode = -1;
        OpenData.tutoViews = null;
        OpenData.tutoSave = null;
        lockMode(true);
    }

    public void showMoreEventInfo() { // guide metu Extra info parodomas
        fragPlans.showTutoExtraInfoAndOpenMenu();
    }

    public void muteOptions(boolean mute) { // special create rezime neturi matytis
        mMenu.findItem(R.id.action_sort).setVisible(!mute);
        mMenu.findItem(R.id.action_delete_no_dates).setVisible(!mute);
    }

    @Override
    protected void onPause() {
        if (combined)
            deleteSpecialsFromEvents();

        OpenData.save(this);
        super.onPause();
        BusStation.getBus(0).unregister(this);
    }

    @Override
    protected void onResume() {
        if (currentFrag == R.string.plans) {
            if (!combined) {
                combineSpecialEvents();
                if (fragPlans != null) {
                    fragPlans.updateAfterClean();
                    lockMode(true);
                }
            }
        }
        if (askForTuto == 1) {
            askForTuto = 0;
            dialogForTuto();
        }

        super.onResume();
        BusStation.getBus(0).register(this); // todo edit mode toogle
    }

    @SuppressLint("RestrictedApi")
    private void dialogForTuto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
        builder.setTitle(R.string.start_tutorial);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OpenData.tutoMode = OpenData.getWeekDayInt();
                ready = new boolean[2];
                startTuto1();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @Subscribe
    public void busStopPlansAndTodo(Boolean value) {
        if (currentFrag == R.string.plans) { // TODO: isjungia toolbar mygtukus
            createPlanStart(value);
            mMenu.findItem(R.id.action_lock).setVisible(false);
            mMenu.findItem(R.id.action_settings).setVisible(false);
            mMenu.findItem(R.id.action_done).setVisible(false);
        } else if (currentFrag == R.string.todo) {
            if (value)
                mMenu.findItem(R.id.action_circle_add).setIcon(R.drawable.ic_save);
            else
                mMenu.findItem(R.id.action_circle_add).setIcon(R.drawable.ic_plus_circle);
            mMenu.findItem(R.id.action_cancel).setVisible(value);
        }
    }

    private void createPlanStart(boolean editMode) {
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);
        if (!editMode) {
            mMenu.findItem(R.id.action_circle_add).setVisible(true).setIcon(R.drawable.ic_check);
            toolbar.setTitle(getString(R.string.add));
        }
        else {
            mMenu.findItem(R.id.action_circle_add).setVisible(true).setIcon(R.drawable.ic_save);
            toolbar.setTitle(getString(R.string.edit));
        }
        lockDrawer(true);
    }

    public void createPlanStop() {
        fragPlans.dismissFrag();
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
        lockDrawer(false);
        mMenu.findItem(R.id.action_circle_add).setVisible(false).setIcon(R.drawable.ic_save);
        mMenu.findItem(R.id.action_lock).setVisible(true);
        mMenu.findItem(R.id.action_settings).setVisible(true);
        mMenu.findItem(R.id.action_done).setVisible(true);
        toolbar.setTitle(getString(currentFrag));
    }

}