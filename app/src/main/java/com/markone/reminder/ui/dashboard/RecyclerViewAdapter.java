package com.markone.reminder.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.markone.reminder.Common;
import com.markone.reminder.R;
import com.markone.reminder.ui.reminder.Reminder;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyRecyclerViewHolder> {
    private List<Reminder> reminderList = new ArrayList<>();

    RecyclerViewAdapter(List<Reminder> reminders) {
        if (reminders != null) {
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
        TextView tvStatus;
        TextView tvDate;
        TextView tvTime;
        TextView tvFrequency;
        TextView tvName;
        TextView tvDetail;
        ImageView ivFrequency;
        private int mCurrentPosition;

        MyRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFrequency = itemView.findViewById(R.id.iv_frequency);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvName = itemView.findViewById(R.id.tv_name);
            //TODO add detail tv here
        }

        void onBind(int position) {
            mCurrentPosition = position;

            final Reminder reminder = reminderList.get(position);

            tvStatus.setText(reminder.getStatus().toString());
            tvDate.setText(reminder.getStartDate_Day() + " " + reminder.getStartDate_Month() + " " + reminder.getStartDate_Year());
            tvTime.setText(reminder.getStartDate_Hour() + " : " + reminder.getStartDate_Minute());

            if (reminder.getFrequency() != Common.Frequency.None) {
                tvFrequency.setText(reminder.getFrequency().toString());
            } else {
                ivFrequency.setVisibility(View.INVISIBLE);
                tvFrequency.setVisibility(View.INVISIBLE);
            }
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }
    }
}
