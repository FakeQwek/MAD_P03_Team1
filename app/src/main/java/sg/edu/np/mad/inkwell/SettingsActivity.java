package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        Switch switch1 = findViewById(R.id.switch1);

        // Checks switch1 if night mode is on and vice versa
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            switch1.setChecked(true);
        } else {
            switch1.setChecked(false);
        }

        // Toggle night mode
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch1.isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_main) {
            Intent notesActivity = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(notesActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_notes) {
            Intent todoActivity = new Intent(SettingsActivity.this, NotesActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_todos) {
            Intent todoActivity = new Intent(SettingsActivity.this, TodoActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_flashcards) {
            Intent todoActivity = new Intent(SettingsActivity.this, FlashcardActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_calendar) {
            Intent todoActivity = new Intent(SettingsActivity.this, TimetableActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_timetable) {
            Intent todoActivity = new Intent(SettingsActivity.this, TimetableActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent todoActivity = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_logout) {
            Log.d("Message", "Logout");
        }
        else {
            Log.d("Message", "Unknown page!");
        }
        return true;
    }
}