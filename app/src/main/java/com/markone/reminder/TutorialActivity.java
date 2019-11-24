package com.markone.reminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.markone.reminder.databinding.ActivityTutorialBinding;
import com.markone.reminder.ui.tutorial.TutorialDashboard;
import com.markone.reminder.ui.tutorial.TutorialMain;
import com.markone.reminder.ui.tutorial.TutorialPopup;
import com.markone.reminder.ui.tutorial.TutorialReminder;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final boolean isNotStartup = Common.ACTION_NOT_STARTUP.equals(getIntent().getAction());

        if (!isNotStartup) {
            SharedPreferences sharedPreferences = getSharedPreferences(Common.SETTING_FILE, MODE_PRIVATE);
            if (!sharedPreferences.getBoolean(Common.IS_FIRST_STARTUP, true)) {
                skipToLogon();
            }
            sharedPreferences.edit().putBoolean(Common.IS_FIRST_STARTUP, false).apply();
        }

        super.onCreate(savedInstanceState);
        ActivityTutorialBinding binding = ActivityTutorialBinding.inflate(getLayoutInflater());

        PagerAdapter adapter = new CustomPagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
        binding.tabDots.setupWithViewPager(binding.viewPager);

        binding.btSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotStartup) {
                    onBackPressed();
                } else {
                    skipToLogon();
                }
            }
        });
        setContentView(binding.getRoot());
    }

    private void skipToLogon() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> list = new ArrayList<>();

        CustomPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            list.clear();
            list.add(new TutorialMain());
            list.add(new TutorialDashboard());
            list.add(new TutorialReminder());
            list.add(new TutorialPopup());
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
