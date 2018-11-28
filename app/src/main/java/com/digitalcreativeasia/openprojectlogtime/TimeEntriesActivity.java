package com.digitalcreativeasia.openprojectlogtime;

import android.os.Bundle;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.digitalcreativeasia.openprojectlogtime.adapters.TimeEntriesAdapter;
import com.digitalcreativeasia.openprojectlogtime.interfaces.CustomSnackBarListener;
import com.digitalcreativeasia.openprojectlogtime.pojos.TimeEntryType;
import com.digitalcreativeasia.openprojectlogtime.pojos.task.TaskModel;
import com.digitalcreativeasia.openprojectlogtime.pojos.task.TimeEntries;
import com.digitalcreativeasia.openprojectlogtime.pojos.timeentry.TimeEntry;
import com.digitalcreativeasia.openprojectlogtime.ui.LightStatusBar;
import com.digitalcreativeasia.openprojectlogtime.utils.ErrorResponseInspector;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Credentials;
import timber.log.Timber;


public class TimeEntriesActivity extends AppCompatActivity implements TimeEntriesAdapter.SelectListener {

    public static final String INTENT_TASK_MODEL = "intent.task.model";

    List<TimeEntryType> timeEntryTypes;
    List<TimeEntry> timeEntriesList;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.text_project)
    TextView mProjectText;
    @BindView(R.id.text_work_packages)
    TextView mWorkPackagesText;

    TaskModel mTaskModel;
    Snackbar mSnackBar;
    TimeEntriesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_entries);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        LightStatusBar.inspect(this, toolbar);
        setSupportActionBar(toolbar);

        mTaskModel = new Gson().fromJson(getIntent().getStringExtra(INTENT_TASK_MODEL), TaskModel.class);
        timeEntryTypes = new ArrayList<>();
        timeEntriesList = new ArrayList<>();

        getTimeEntryTypes();
        initViews();
    }

    void showSnackBar(String message, String customAction, CustomSnackBarListener listener) {
        mSnackBar.setText(message);
        mSnackBar.setAction(customAction, listener::onActionClicked);
        mSnackBar.show();
    }

    void showSnackBar(String message) {
        mSnackBar.setText(message);
        mSnackBar.show();
    }

    private void initViews() {
        mRefreshLayout.setOnRefreshListener(() ->
                getTimeEntries(false, String.valueOf(mTaskModel.getId())));
        mSnackBar = Snackbar.make(findViewById(R.id.toolbar), "", Snackbar.LENGTH_INDEFINITE);
        mSnackBar.setAction("OK", view -> mSnackBar.dismiss());

        mProjectText.setText(mTaskModel.getLinks().getProject().getTitle());
        mWorkPackagesText.setText(mTaskModel.getSubject());

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }


    void getTimeEntryTypes() {
        mRefreshLayout.setRefreshing(true);
        String apiKey = App.getTinyDB().getString(App.KEY.API, "");
        AndroidNetworking.get(App.getApplication().getResources().getString(R.string.time_entries_api)
                + App.PATH.ENUM_LIST)
                .addHeaders("Authorization", Credentials.basic("apikey", apiKey))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                TimeEntryType entryType = new Gson().fromJson(response.getJSONObject(i).toString(), TimeEntryType.class);
                                timeEntryTypes.add(entryType);
                            }
                            getTimeEntries(true, String.valueOf(mTaskModel.getId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError err) {
                        mRefreshLayout.setRefreshing(false);
                        Timber.e("err %s", err.getErrorDetail());
                        String msg = ErrorResponseInspector.inspect(err);
                        showSnackBar(msg, "Exit", view -> finish());
                    }
                });
    }


    void getTimeEntries(boolean initRefresh, String workPackageId) {
        if (!initRefresh)
            mRefreshLayout.setRefreshing(true);
        String url = String.format(App.PATH.TIME_ENTRIES_LIST, workPackageId);
        AndroidNetworking.get(App.getApplication().getResources().getString(R.string.baseUrl)
                + url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mRefreshLayout.setRefreshing(false);
                        Timber.i("resp: %s", response.toString());
                        try {
                            JSONArray array = response.getJSONObject("_embedded").getJSONArray("elements");
                            timeEntriesList.clear();
                            for (int i = 0; i < array.length(); i++) {
                                TimeEntry model = new Gson().fromJson(array.getJSONObject(i).toString(), TimeEntry.class);
                                timeEntriesList.add(model);
                            }
                            updateList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError err) {
                        mRefreshLayout.setRefreshing(false);
                        Timber.e("err %s", err.getErrorDetail());
                        String msg = ErrorResponseInspector.inspect(err);
                        showSnackBar(msg, "Exit", view -> finish());
                    }
                });
    }

    private void updateList() {
        if (mAdapter == null) {
            mAdapter = new TimeEntriesAdapter(this, timeEntriesList, this);
            mRecyclerView.setAdapter(mAdapter);
        } else mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelect(TimeEntry model) {

    }
}
