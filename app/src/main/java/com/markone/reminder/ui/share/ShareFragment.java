package com.markone.reminder.ui.share;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentShareBinding;

public class ShareFragment extends Fragment {

    private FragmentShareBinding fragmentShareBinding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentShareBinding == null) {
            fragmentShareBinding = FragmentShareBinding.inflate(inflater, container, false);
        }
        return fragmentShareBinding.getRoot();
    }
}