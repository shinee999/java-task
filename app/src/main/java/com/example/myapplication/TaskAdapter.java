package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;

    public <E> TaskAdapter(ArrayList<E> es) {
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;
        TextView titleTextView;
        TextView descriptionTextView;
        View colorCodeView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            colorCodeView = itemView.findViewById(R.id.colorCodeView);
        }

        void bind(Task task) {
            taskTextView.setText(task.getTask());
            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getDescription());
            colorCodeView.setBackgroundColor(Color.parseColor(task.getColorCode().equals("") ? "#0095da" : task.getColorCode()));
        }
    }
}