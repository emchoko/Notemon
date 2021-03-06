package com.notemon;

import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.notemon.fragments.NoteRecyclerFragment;
import com.notemon.fragments.ProfileFragment;
import com.notemon.fragments.SettingsFragment;
import com.notemon.helpers.ColorValues;
import com.notemon.helpers.Constants;
import com.notemon.models.FirebaseToken;
import com.notemon.models.Project;
import com.notemon.rest.RestMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FILE = 0;
    private static final int EDIT = 1;
    private static final int NEW_MENU_ITEM = Menu.FIRST;
    private static final int SAVE_MENU_ITEM = NEW_MENU_ITEM + 1;
    private static final int UNDO_MENU_ITEM = SAVE_MENU_ITEM + 1;
    private static final int REDO_MENU_ITEM = UNDO_MENU_ITEM + 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private SubMenu projectSubMenu;
    private Menu navMenu;
    private Map<Integer, Project> projectMap;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private Project baseProject;
    private List<Project> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navMenu = navigationView.getMenu();
        projectSubMenu = navMenu.addSubMenu(R.string.projects);
        projectSubMenu.add(0, Menu.FIRST, Menu.FIRST, R.string.add_project)
                .setIcon(R.drawable.ic_add_black_24dp);

        getProjectsForUser();
        refreshToken();
    }

    private void refreshToken() {
        FirebaseToken firebaseToken = new FirebaseToken();
        String token = FirebaseInstanceId.getInstance().getToken();
        firebaseToken.setDeviceToken(token);
        Log.d(TAG, "Firebase Token: " + token);
        RestMethods.addTokenToUser(this, firebaseToken);
    }

    private void getProjectsForUser() {
        Call<List<Project>> call = RestMethods.getProjects(this);
        call.enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                switch (response.code()) {
                    case 200:
                        for (Project p : response.body()) {
                            Log.d(TAG, p.getName());
                        }

                        setUpProjectMenu(response.body());
                        break;
                    case 401:
                        Log.d(TAG, 401 + "");
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(MainActivity.this, "Error getting projects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpProjectMenu(List<Project> projects) {
        projectMap = new HashMap<>();
        int i = 0;
        for (Project p : projects) {
            if (!("Base Project").equals(p.getName())) {
                i++;
                projectMap.put(i, p);
            } else {
                baseProject = p;
            }
        }
        setupFrontFragment(true, baseProject);
        addProjectsToNav(projectMap);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager manager = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == projectSubMenu.getItem(0).getItemId()) {
            Toast.makeText(this, "Adding Project", Toast.LENGTH_SHORT).show();
            createProject();
        }
        if (projectMap != null)
            for (Map.Entry<Integer, Project> entry : projectMap.entrySet()) {
                if (id == projectSubMenu.getItem(entry.getKey()).getItemId()) {
                    Toast.makeText(this, "Project" + entry.getKey(), Toast.LENGTH_SHORT).show();
                    setupFrontFragment(false, entry.getValue());
                }
            }

        switch (id) {
            case R.id.navHome:
                setupFrontFragment(true, baseProject);
                break;
            case R.id.navSettings:
                setUpSettingsFragment();
                toolbar.setTitle("Settings");
                break;

            case R.id.navProfile:
                setUpProfileFragment();
                toolbar.setTitle("Profile");
                break;
//            case R.id.navProject:
//                setupFrontFragment(false, 1, getResources().getColor(R.color.project_red), getResources().getColor(R.color.project_red_dark));
//                break;
//            case projectSubMenu.getItem(Menu.FIRST).getItemId():
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpProfileFragment() {
        FragmentManager manager = getFragmentManager();
        ProfileFragment fragment = new ProfileFragment();

        manager.beginTransaction()
                .replace(R.id.mainFrameLayout, fragment)
                .commit();
    }

    private void setUpSettingsFragment() {
        FragmentManager manager = getFragmentManager();

        SettingsFragment fragment = new SettingsFragment();
        manager.beginTransaction()
                .replace(R.id.mainFrameLayout, fragment)
                .commit();
    }

    private void setupFrontFragment(boolean isHome, Project project) {
        FragmentManager manager = getFragmentManager();

        NoteRecyclerFragment fragment = new NoteRecyclerFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.HOME_FRAGMENT, isHome);
        args.putLong(Constants.PROJECT_ID, project.getId());
        args.putSerializable(Constants.PROJECT_ALL, project);
        fragment.setArguments(args);
        manager.beginTransaction()
                .replace(R.id.mainFrameLayout, fragment)
                .commit();

        if (isHome) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setTitle(R.string.notemon_title);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        } else {
            toolbar.setBackgroundColor(project.getColor());
            toolbar.setTitle(project.getName());
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toast.makeText(this, "Project Color: " + project.getColorDark(), Toast.LENGTH_SHORT).show();
                setToolbarColor(project.getColorDark());
            }

        }

    }

    private void setToolbarColor(int resColor) {
        Window window = this.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(resColor);
        }
    }

    private String projectNameAdd = "";
    private String projectColorAdd = "";

    private void createProject() {

        new MaterialDialog.Builder(this)
                .title(getString(R.string.add_project))
                .input("Project Name", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        projectNameAdd = input.toString();
                    }
                })
                .alwaysCallInputCallback()
                .content("Choose Project Name")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        colorDialog(projectNameAdd);
                    }
                })
                .show();
    }

    private void colorDialog(String projectName) {
        final List<String> colorList = new ArrayList<>();

        colorList.add("red");
        colorList.add("blue");
        colorList.add("green");
        colorList.add("yellow");
        colorList.add("pink");
        colorList.add("purple");
        colorList.add("teal");
        colorList.add("orange");
        colorList.add("lime");

        new MaterialDialog.Builder(this)
                .title(getString(R.string.add_project))
                .items(colorList).itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                Toast.makeText(MainActivity.this, colorList.get(which), Toast.LENGTH_SHORT).show();
                projectColorAdd = colorList.get(which);
                Project project = new Project(ColorValues.getColorId(projectColorAdd, MainActivity.this), ColorValues.getDarkColorId(projectColorAdd, MainActivity.this), projectNameAdd);

                RestMethods.createProject(project, MainActivity.this, false);

                getProjectsForUser();
                return true;
            }
        })
                .show();

    }

    private void addProjectsToNav(Map<Integer, Project> projects) {
        projectSubMenu.removeGroup(1);
        for (Map.Entry<Integer, Project> entry : projects.entrySet()) {
            projectSubMenu.add(1, entry.getKey() + 1, entry.getKey(), entry.getValue().getName())
                    .setIcon(R.drawable.ic_flag_black_24dp);
        }
    }

}
