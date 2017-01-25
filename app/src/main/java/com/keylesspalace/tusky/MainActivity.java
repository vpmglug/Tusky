/* Copyright 2017 Andrew Dawson
 *
 * This file is part of Tusky.
 *
 * Tusky is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky. If not, see
 * <http://www.gnu.org/licenses/>. */

package com.keylesspalace.tusky;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private AlarmManager alarmManager;
    private PendingIntent serviceAlarmIntent;
    private boolean notificationServiceEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup the tabs and timeline pager.
        TimelinePagerAdapter adapter = new TimelinePagerAdapter(getSupportFragmentManager());
        String[] pageTitles = {
            getString(R.string.title_home),
            getString(R.string.title_notifications),
            getString(R.string.title_public)
        };
        adapter.setPageTitles(pageTitles);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Retrieve notification update preference.
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        notificationServiceEnabled = preferences.getBoolean("notificationService", true);
        long notificationCheckInterval =
                preferences.getLong("notificationCheckInterval", 5 * 60 * 1000);
        // Start up the NotificationsService.
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationService.class);
        final int SERVICE_REQUEST_CODE = 8574603; // This number is arbitrary.
        serviceAlarmIntent = PendingIntent.getService(this, SERVICE_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (notificationServiceEnabled) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), notificationCheckInterval, serviceAlarmIntent);
        } else {
            alarmManager.cancel(serviceAlarmIntent);
        }
    }

    private void compose() {
        Intent intent = new Intent(this, ComposeActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        if (notificationServiceEnabled) {
            alarmManager.cancel(serviceAlarmIntent);
        }
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("domain");
        editor.remove("accessToken");
        editor.apply();
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compose: {
                compose();
                return true;
            }
            case R.id.action_logout: {
                logOut();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
