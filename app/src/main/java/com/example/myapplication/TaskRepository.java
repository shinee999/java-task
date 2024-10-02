package com.example.myapplication;

import android.content.Context;

import com.example.myapplication.model.AppDatabase;
import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.Task;
import com.example.myapplication.model.TaskDao;
import com.example.myapplication.services.ApiService;
import com.example.myapplication.services.RetrofitClient;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {
    private ApiService apiService;
    private TaskDao taskDao;
    private AppDatabase database;

    private static final String AUTH_HEADER = "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz";
    private static final String CONTENT_TYPE = "application/json";

    public TaskRepository(Context context) {
        apiService = RetrofitClient.getApiService();
        database = AppDatabase.getInstance(context);
        taskDao = database.taskDao();
    }

    public void refreshTasks(String token, final TaskCallback callback) {
        apiService.getTasks(token).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body();

                    System.out.println("tasks " + tasks.get(0).getTask());
                    insertTasks(tasks);
                    callback.onTasksLoaded(tasks);
                } else {
                    callback.onDataNotAvailable();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                callback.onDataNotAvailable();
            }
        });
    }


    public void login(final LoginCallBack callback) {

        LoginRequest loginRequest = new LoginRequest("365","1");
        Call<LoginResponse> call = apiService.login(AUTH_HEADER, CONTENT_TYPE, loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Handle successful login
                    String token = loginResponse.oauth.access_token;
                } else {
                    // Handle unsuccessful login
                    System.out.println("Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Handle network failure
                System.out.println("Network error: " + t.getMessage());
            }
        });
    }

    private void insertTasks(final List<Task> tasks) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (Task task : tasks) {
                    if (task.getId() == null || task.getId().isEmpty()) {
                        task.setId(UUID.randomUUID().toString());
                    }
                }
                taskDao.insertAll(tasks);
            }
        });
    }

    public void getAllTasks(final TaskCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Task> tasks = taskDao.getAllTasks();
                callback.onTasksLoaded(tasks);
            }
        });
    }

    public void searchTasks(final String query, final TaskCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Task> tasks = taskDao.searchTasks("%" + query + "%");
                callback.onTasksLoaded(tasks);
            }
        });
    }

    public interface TaskCallback {
        void onTasksLoaded(List<Task> tasks);
        void onDataNotAvailable();
    }

    public interface LoginCallBack {
        void onLogin(String token);


        void onDataNotAvailable();
    }
}