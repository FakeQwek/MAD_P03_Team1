package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentNoteId keeps track of the ids that have already been assigned
    public static int currentNoteId;

    // selectedNoteId keeps track of the note that has been selected
    public static int selectedNoteId = 1;

    public static ArrayList<File> files = new ArrayList<>();

    public static ArrayList<Integer> fileIds = new ArrayList<>();

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Object> allNotes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        NotesAdapter adapter = new NotesAdapter(allNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
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

    // Method to search the items in recycler view
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

    // Method to notify recycler view a new item has been inserted
    private void notifyInsert() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(0);
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

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        ArrayList<Object> notes = new ArrayList<>();

        // Read from firebase and create files and folders on create
        db.collection("users").document(currentFirebaseUserUid).collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                search(files, notes);
                                String docNoteType = document.getData().get("type").toString();
                                String docNoteUid = document.getData().get("uid").toString();
                                if (docNoteType.equals("file") && docNoteUid.equals(currentFirebaseUserUid)) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    File file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference());
                                    notes.add(file);
                                    filter(files, notes, "");
                                } else if (docNoteType.equals("folder") && docNoteUid.equals(currentFirebaseUserUid)) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    Folder folder = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, db.collection("users").document(currentFirebaseUserUid).collection("notes"));
                                    notes.add(folder);
                                    filter(files, notes, "");
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });

        ImageButton addFileButton = findViewById(R.id.addFileButton);

        // Adds a file to firebase and updates the recycler view
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                Map<String, Object> fileData = new HashMap<>();
                fileData.put("title", "Title");
                fileData.put("body", "Enter your text");
                fileData.put("type", "file");
                fileData.put("uid", currentFirebaseUserUid);

                db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)).set(fileData);

                File file = new File("Title", "Enter your text", currentNoteId, "file", db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)));
                fileIds.add(file.id);
                files.add(file);
                notes.add(0, file);

                if (currentNoteId == 1) {
                    recyclerView(notes);
                } else {
                    notifyInsert();
                }
            }
        });

        ImageButton addFolderButton = findViewById(R.id.addFolderButton);

        // Adds a folder to firebase and updates the recycler view
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                Map<String, Object> folderData = new HashMap<>();
                folderData.put("title", "Folder");
                folderData.put("body", "");
                folderData.put("type", "folder");
                folderData.put("uid", currentFirebaseUserUid);

                db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)).set(folderData);

                Folder folder = new Folder("Folder", "", NotesActivity.currentNoteId, "folder", db.collection("users").document(currentFirebaseUserUid).collection("notes"));
                notes.add(0, folder);

                if (currentNoteId == 1) {
                    recyclerView(notes);
                } else {
                    notifyInsert();
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
