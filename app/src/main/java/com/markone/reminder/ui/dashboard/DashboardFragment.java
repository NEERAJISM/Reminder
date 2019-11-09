package com.markone.reminder.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;
import com.markone.reminder.databinding.FragmentDashboardBinding;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Neeraj on 02-Nov-19
 */
public class DashboardFragment extends Fragment {
    private final CollectionReference reminderCollectionReference = FirebaseFirestore.getInstance()
            .collection(Common.REMINDER_DB)
            .document(Common.USER_ID)
            .collection(Common.REMINDER_COLLECTION);

    private RecyclerView recyclerView;
    private FragmentDashboardBinding fragmentDashboardBinding;
    private RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton floatingActionButton;
    private RecyclerViewAdapter mAdapter;
    private List<Reminder> reminders = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext());
        mAdapter = new RecyclerViewAdapter(getActivity(), reminders);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (fragmentDashboardBinding == null) {
            fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false);
            floatingActionButton = fragmentDashboardBinding.fab;
            setFloatingButtonAction();

            recyclerView = fragmentDashboardBinding.rvReminders;
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);
        }
        getReminders();
        return fragmentDashboardBinding.getRoot();
    }

    private void setFloatingButtonAction() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) Objects.requireNonNull(getActivity())).getNavController().navigate(R.id.nav_reminder);
            }
        });
    }

    private String getUserId() {
        return Common.USER_ID;
    }

    private void getReminders() {
        reminderCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    reminders.clear();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        reminders.add(documentSnapshot.toObject(Reminder.class));
                    }
                    mAdapter.updateReminders(reminders);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Common.viewToast(getContext(), "Error while getting reminders");
                }
            }
        });
    }
}
