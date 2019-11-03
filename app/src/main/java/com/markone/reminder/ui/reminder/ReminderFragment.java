package com.markone.reminder.ui.reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentReminderBinding;

import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class ReminderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater, container, false);
        binding.btCreateReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (Objects.requireNonNull(getActivity())).onBackPressed();
            }
        });

        return binding.getRoot();
    }
}
