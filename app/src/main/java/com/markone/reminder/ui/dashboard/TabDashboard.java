package com.markone.reminder.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.markone.reminder.databinding.FragmentTabDashboardBinding;

public class TabDashboard extends Fragment {

    private FragmentTabDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentTabDashboardBinding.inflate(inflater, container, false);
            TabAdapter adapter = new TabAdapter(getChildFragmentManager());
            DoneDashboardFragment doneDashboardFragment = new DoneDashboardFragment();
            DashboardFragment dashboardFragment = new DashboardFragment(doneDashboardFragment);
            adapter.addFragment(dashboardFragment, "Upcoming");
            adapter.addFragment(doneDashboardFragment, "Completed");
            binding.viewPager.setAdapter(adapter);
            binding.tabLayout.setupWithViewPager(binding.viewPager);
        }
        return binding.getRoot();
    }
}
