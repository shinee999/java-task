package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.Task;
import com.example.myapplication.services.ApiService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TaskRepository taskRepository;
    private TaskAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;



    private String accessToken = "";

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshTasks);

        taskRepository = new TaskRepository(this);

        refreshTasks();
//        setupPeriodicWork();
    }

    private void refreshTasks() {
        swipeRefreshLayout.setRefreshing(true);


        // First, create your API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.baubuddy.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        // Login parameters
        String AUTH_HEADER = "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz";
        String CONTENT_TYPE = "application/json";
        LoginRequest loginRequest = new LoginRequest("365", "1");

        // Perform login
        apiService.login(AUTH_HEADER, CONTENT_TYPE, loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.oauth.access_token;

                    // Now that we have the token, refresh tasks
                    refreshTasksWithToken("Bearer " + token);
                } else {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        // Handle login error
                        Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    // Handle network error
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshTasksWithToken(String bearerToken) {



        taskRepository.refreshTasks(bearerToken, new TaskRepository.TaskCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                runOnUiThread(() -> {
                    adapter.setTasks(tasks);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onDataNotAvailable() {
                runOnUiThread(() -> {
                    // Handle error
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

//    private String getToken() {
//        taskRepository.login(new TaskRepository.LoginCallBack() {
//            @Override
//            public void onLogin(String token) {
//                runOnUiThread(() -> {
//                    setAccessToken(token);
//                });
//            }
//
//            @Override
//            public void onDataNotAvailable() {
//                runOnUiThread(() -> {
//                    // Handle error
//                    setAccessToken(null);
//                });
//            }
//        });
//
//        return accessToken;
//    }

    private void setupPeriodicWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                RefreshTasksWorker.class, 60, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
        return true;
    }

    private void performSearch(String query) {
        taskRepository.searchTasks(query, new TaskRepository.TaskCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                runOnUiThread(() -> adapter.setTasks(tasks));
            }

            @Override
            public void onDataNotAvailable() {
                runOnUiThread(() -> {
                    // Handle error
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_qr) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR Code");
            integrator.initiateScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                performSearch(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}