package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaration of variables

    // Latest noteId of most recently created note
    public static int currentNoteId;

    // noteId of most recently selected note
    public static int selectedNoteId = 1;

    public static ArrayList<File> files = new ArrayList<>();

    public static ArrayList<Integer> fileIds = new ArrayList<>();

    private void recyclerView(ArrayList<Object> allNotes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        NotesAdapter adapter = new NotesAdapter(allNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<File> files, ArrayList<Object> notes, String query) {
        ArrayList<Object> filterList = new ArrayList<>();
        if (query.isEmpty()) {
            recyclerView(notes);
        } else {
            for (File file : files){
                if (file.title.toLowerCase().contains(query)) {
                    filterList.add(file);
                }
            }
            recyclerView(filterList);
        }
    }

    private void search(ArrayList<File> files, ArrayList<Object> notes) {
        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(files, notes, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(files, notes, newText);
                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        // Get noteTitle and noteBody EditText and set text
        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteBody = findViewById(R.id.noteBody);

        ArrayList<Object> notes = new ArrayList<>();

        // Read from firebase and create files and folders on app load
        db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                search(files, notes);
                                String docNoteType = document.getData().get("type").toString();
                                if (docNoteType.equals("file")) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    File file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference());
                                    notes.add(file);
                                    filter(files, notes, "");
                                } else if (docNoteType.equals("folder")) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    Folder folder = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, db.collection("notes"));
                                    notes.add(folder);
                                    filter(files, notes, "");
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_notes) {
            /* Replace intent with other function for fragment
            Intent Login = new Intent(MainActivity.this, Login.class);
            startActivity(Login);
            */
            Log.d( "Message", "Opening notes");
        }
        else if (menuItem.getItemId() == R.id.nav_todo) {
            Intent todoActivity = new Intent(NotesActivity.this, TodoActivity.class);
            startActivity(todoActivity);
            Log.d("Message", "Opening home");
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_calendar) {
            Log.d("Message", "Opening calendar");
        }
        else if (menuItem.getItemId() == R.id.nav_timetable) {
            Log.d("Message", "Opening timetable");
        }
        else {
            Log.d("Message", "Unknown page!");
        }
        return true;
    }
}
