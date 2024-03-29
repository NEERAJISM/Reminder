package com.markone.reminder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.markone.reminder.ui.dashboard.TabDashboard;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public NavController navController;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private boolean isProUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        setProfile();
    }

    private void setProfile() {
        View header = navigationView.getHeaderView(0);
        ImageView imageView = header.findViewById(R.id.iv_profile);
        TextView textView = header.findViewById(R.id.tv_name);

        String name = getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_NAME, "User");
        String uri = getSharedPreferences(Common.USER_FILE, MODE_PRIVATE).getString(Common.USER_URI, "");

        textView.setText("Hi " + name + "!");
        if (!Common.isBlank(uri)) {
            new DownloadImageTask(imageView, uri).execute();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment), drawer);
    }

    public NavController getNavController() {
        return navController;
    }

    @Override
    public void onBackPressed() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        int id = menuItem.getItemId();
        switch (id) {
            case R.id.menu_new_reminder:
                int currentReminderSize = getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE).getInt(Common.CURRENT_REMINDER_SIZE, 0);
                if ((isProUser ? Common.MAX_REMINDERS_PRO : Common.MAX_REMINDERS) <= currentReminderSize) {
                    Common.viewToast(this, "Max Reminder Limit Reached");
                } else {
                    navController.navigate(R.id.nav_reminder);
                }
                break;
            case R.id.menu_completed:
                Fragment fragment = getSupportFragmentManager().getFragments().get(0).getChildFragmentManager().getFragments().get(0);
                if (fragment instanceof TabDashboard) {
                    ((TabDashboard) fragment).ShowCompleted();
                }
                break;
            case R.id.menu_settings:
                navController.navigate(R.id.nav_settings);
                break;
            case R.id.menu_rate_us:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                break;
            case R.id.menu_tutorial:
                Intent intent = new Intent(this, TutorialActivity.class);
                intent.setAction(Common.ACTION_NOT_STARTUP);
                startActivity(intent);
                break;
        }
        return true;
    }

    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        ImageView bmImage;
        String url;

        public DownloadImageTask(ImageView bmImage, String url) {
            this.bmImage = bmImage;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
