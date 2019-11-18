package com.markone.reminder.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding fragmentSettingsBinding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentSettingsBinding == null) {
            fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);
        }
        return fragmentSettingsBinding.getRoot();
    }
}