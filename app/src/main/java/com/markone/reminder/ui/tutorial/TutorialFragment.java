package com.markone.reminder.ui.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentTutorialBinding;

public class TutorialFragment extends Fragment {

    private FragmentTutorialBinding fragmentTutorialBinding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentTutorialBinding == null) {
            fragmentTutorialBinding = FragmentTutorialBinding.inflate(inflater, container, false);
        }
        return fragmentTutorialBinding.getRoot();
    }
}