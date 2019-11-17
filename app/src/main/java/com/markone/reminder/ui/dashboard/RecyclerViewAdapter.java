package com.markone.reminder.ui.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.markone.reminder.Common;
import com.markone.reminder.MainActivity;
import com.markone.reminder.R;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyRecyclerViewHolder> {
    private Activity activity;
    private List<Reminder> reminderList = new ArrayList<>();

    RecyclerViewAdapter(FragmentActivity activity, List<Reminder> reminders) {
        this.activity = activity;
        if (reminders != null) {
            this.reminderList.addAll(reminders);
        }
    }

    public void updateReminders(List<Reminder> reminders) {
        if (reminders != null) {
            reminderList.clear();
            this.reminderList.addAll(reminders);
        }
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reminder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public class MyRecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatus;
        TextView tvDate;
        TextView tvTime;
        TextView tvFrequency;
        TextView tvName;
        TextView tvDetail;
        ImageView ivFrequency;
        MaterialCardView cardView;

        private int mCurrentPosition;

        MyRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            ivFrequency = itemView.findViewById(R.id.iv_frequency);
            ivStatus = itemView.findViewById(R.id.iv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvName = itemView.findViewById(R.id.tv_name);
            //TODO add detail tv here
        }

        void onBind(int position) {
            mCurrentPosition = position;

            final Reminder reminder = reminderList.get(position);
            tvName.setText(reminder.getName());

            Common.Status status = reminder.getStatus();
            if (status == Common.Status.Low) {
                ivStatus.setImageResource(R.drawable.ic_warning_low);
            } else if (status == Common.Status.Med) {
                ivStatus.setImageResource(R.drawable.ic_warning_med);
            } else if (status == Common.Status.High) {
                ivStatus.setImageResource(R.drawable.ic_warning_high);
            } else {
                ivStatus.setImageResource(R.drawable.ic_tick);
            }

            tvDate.setText(Common.getFormattedDate(reminder.getStartDate_Day(), reminder.getStartDate_Month(), reminder.getStartDate_Year()));
            tvTime.setText(Common.getFormattedTime(reminder.getStartDate_Hour(), reminder.getStartDate_Minute()));

            if (reminder.getFrequency() != Common.Frequency.Once) {
                tvFrequency.setText(reminder.getFrequency().toString());
            } else {
                ivFrequency.setVisibility(View.INVISIBLE);
                tvFrequency.setVisibility(View.INVISIBLE);
            }

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Reminder", reminder);
                    ((MainActivity) activity).getNavController().navigate(R.id.nav_reminder, bundle);
                }
            });
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }
    }
}
