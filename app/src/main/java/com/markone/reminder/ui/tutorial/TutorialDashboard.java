package com.markone.reminder.ui.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentTutorialDashboardBinding;

public class TutorialDashboard extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTutorialDashboardBinding binding = FragmentTutorialDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}