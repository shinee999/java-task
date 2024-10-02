package com.example.myapplication.model;

import androidx.room.*;
import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Task> tasks);

    @Query("SELECT * FROM tasks WHERE task LIKE :query OR title LIKE :query OR description LIKE :query")
    List<Task> searchTasks(String query);
}