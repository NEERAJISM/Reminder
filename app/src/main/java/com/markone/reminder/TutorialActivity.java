package com.markone.reminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.markone.reminder.databinding.ActivityTutorialBinding;

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
}
