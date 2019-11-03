package com.markone.reminder.ui.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.markone.reminder.R;

public class TutorialFragment extends Fragment {

    private TutorialViewModel tutorialViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tutorialViewModel =
                ViewModelProviders.of(this).get(TutorialViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tutorial, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        tutorialViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}