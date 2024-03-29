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
    public static final String HEADER_CONSTANT_ID = String.valueOf(Integer.MAX_VALUE);
    public static final int HEADER_TYPE = 0;
    public static final int REMINDER_TYPE = 1;

    private Activity activity;
    private List<Reminder> reminderList = new ArrayList<>();

    RecyclerViewAdapter(FragmentActivity activity, List<Reminder> reminders) {
        this.activity = activity;
        if (reminders != null) {
            this.reminderList.addAll(reminders);
        }
    }

    void clearReminders() {
        reminderList.clear();
    }

    void updateReminders(List<Reminder> reminders, String header) {
        if (reminders != null && reminders.size() > 0) {
            reminderList.add(getHeader(header));
            this.reminderList.addAll(reminders);
        }
    }

    private Reminder getHeader(String header) {
        Reminder reminder = new Reminder();
        reminder.setId(HEADER_CONSTANT_ID);
        reminder.setName(header);
        return reminder;
    }

    @Override
    public int getItemViewType(int position) {
        return (HEADER_CONSTANT_ID.equals(reminderList.get(position).getId())) ? HEADER_TYPE : REMINDER_TYPE;
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (viewType == HEADER_TYPE) ? new MyRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_header, parent, false))
                : new MyRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reminder, parent, false));
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
        TextView tvHeader;

        ImageView ivStatus;
        TextView tvDate;
        TextView tvTime;
        TextView tvFrequency;
        TextView tvName;
        ImageView ivFrequency;
        MaterialCardView cardView;

        private int mCurrentPosition;

        MyRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView.getId() == R.id.ll_header) {
                tvHeader = itemView.findViewById(R.id.tv_header);
            } else {
                cardView = itemView.findViewById(R.id.card);
                ivFrequency = itemView.findViewById(R.id.iv_frequency);
                ivStatus = itemView.findViewById(R.id.iv_status);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvFrequency = itemView.findViewById(R.id.tv_frequency);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }

        void onBind(int position) {
            mCurrentPosition = position;

            final Reminder reminder = reminderList.get(position);
            if (HEADER_CONSTANT_ID.equals(reminder.getId())) {
                tvHeader.setText(reminder.getName());
                return;
            }

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
                ivFrequency.setVisibility(View.VISIBLE);
                tvFrequency.setText(reminder.getFrequency().toString());
                tvFrequency.setVisibility(View.VISIBLE);
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
