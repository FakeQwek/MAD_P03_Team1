package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Get id of current user

    //String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Class to add text change listener
    public abstract static class TextChangedListener<T> implements TextWatcher {
        private T target;

        public TextChangedListener(T target) {
            this.target = target;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            this.onTextChanged(target, s);
        }

        public abstract void onTextChanged(T target, Editable s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Alert", "MainActivity class created");
        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds nav bar drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Sets listener to allows for closing and opening of the navbar
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Sets help button functionality to bring you to introduction
        ImageView helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                startActivity(new Intent(MainActivity.this, Intro1.class));
            }
        });

        String name = getIntent().getStringExtra("Username");
        if (name != null) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            Log.d("Alert", "name is" + name);
            TextView nameText = findViewById(R.id.accountName);
            nameText.setText(name);
        }


        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", "");
        userData.put("type", "");
        /*
        db.collection("users").document(currentFirebaseUserUid).set(userData);
        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document("0").set(userData);
        db.collection("users").document(currentFirebaseUserUid).collection("notes").document("0").set(userData);
        db.collection("users").document(currentFirebaseUserUid).collection("todos").document("0").set(userData);
        */

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //Allows movement between activities upon clicking from Navbar class
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        // If home button is pressed trigger toast from navbar class
        if (id == R.id.nav_home) {
            Navbar navbar = new Navbar(this);
            Intent newActivity = navbar.redirect(id, true);
            return true;
        }
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        Log.d("Alert", "Starting" + newActivity);
        return true;

    }


}