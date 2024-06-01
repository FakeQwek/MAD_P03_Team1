package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

    public static ArrayList<File> fileOrder = new ArrayList<>();

    public static int fileOrderIndex;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Object> allNotes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        NotesAdapter adapter = new NotesAdapter(allNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
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

        if (searchView != null) {
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
    }

    // Method to notify recycler view a new item has been inserted
    private void notifyInsert() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(0);
    }

    private void navigationBar() {
        SearchView searchView = findViewById(R.id.searchView);

        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_main).setVisible(false);
        menu.findItem(R.id.nav_notes).setVisible(false);
        menu.findItem(R.id.nav_todos).setVisible(false);
        menu.findItem(R.id.nav_flashcards).setVisible(false);
        menu.findItem(R.id.nav_calendar).setVisible(false);
        menu.findItem(R.id.nav_timetable).setVisible(false);
        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);

        ImageButton swapButton = findViewById(R.id.swapButton);

        if (swapButton != null) {
            swapButton.setVisibility(View.VISIBLE);

            swapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menu.hasVisibleItems()) {
                        menu.findItem(R.id.nav_main).setVisible(false);
                        menu.findItem(R.id.nav_notes).setVisible(false);
                        menu.findItem(R.id.nav_todos).setVisible(false);
                        menu.findItem(R.id.nav_flashcards).setVisible(false);
                        menu.findItem(R.id.nav_calendar).setVisible(false);
                        menu.findItem(R.id.nav_timetable).setVisible(false);
                        menu.findItem(R.id.nav_settings).setVisible(false);
                        menu.findItem(R.id.nav_logout).setVisible(false);
                        searchView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        menu.findItem(R.id.nav_main).setVisible(true);
                        menu.findItem(R.id.nav_notes).setVisible(true);
                        menu.findItem(R.id.nav_todos).setVisible(true);
                        menu.findItem(R.id.nav_flashcards).setVisible(true);
                        menu.findItem(R.id.nav_calendar).setVisible(true);
                        menu.findItem(R.id.nav_timetable).setVisible(true);
                        menu.findItem(R.id.nav_settings).setVisible(true);
                        menu.findItem(R.id.nav_logout).setVisible(true);
                        searchView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            });
        }
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

        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteBody = findViewById(R.id.noteBody);

        fileOrder = new ArrayList<>();

        fileOrderIndex = -1;

        // Read from firebase and create files and folders on create
        db.collection("users").document(currentFirebaseUserUid).collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                search(files, notes);
                                navigationBar();
                                String docNoteType = document.getData().get("type").toString();
                                String docNoteUid = document.getData().get("uid").toString();
                                if (Integer.parseInt(document.getId()) > currentNoteId) {
                                    currentNoteId = Integer.parseInt(document.getId());
                                }
                                if (docNoteType.equals("file") && docNoteUid.equals(currentFirebaseUserUid)) {
                                    File file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference());
                                    notes.add(file);
                                    filter(files, notes, "");
                                } else if (docNoteType.equals("folder") && docNoteUid.equals(currentFirebaseUserUid)) {
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

                Toast toast = new Toast(NotesActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_added, null);
                toast.setView(view);
                toast.show();
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

                Toast toast = new Toast(NotesActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_added, null);
                toast.setView(view);
                toast.show();
            }
        });

        ImageButton leftButton = findViewById(R.id.leftButton);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileOrderIndex > 0) {
                    fileOrderIndex--;
                    selectedNoteId = fileOrder.get(fileOrderIndex).id;

                    noteTitle.setText(fileOrder.get(fileOrderIndex).title);
                    noteBody.setText(fileOrder.get(fileOrderIndex).body);
                }
            }
        });

        ImageButton rightButton = findViewById(R.id.rightButton);

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileOrderIndex < fileOrder.size() - 1) {
                    fileOrderIndex++;
                    selectedNoteId = fileOrder.get(fileOrderIndex).id;

                    noteTitle.setText(fileOrder.get(fileOrderIndex).title);
                    noteBody.setText(fileOrder.get(fileOrderIndex).body);
                }
            }
        });

        ImageButton readOnlyButton = findViewById(R.id.readOnlyButton);

        readOnlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteTitle.isEnabled()) {
                    noteTitle.setEnabled(false);
                    noteBody.setEnabled(false);
                    readOnlyButton.setImageResource(R.drawable.pencil_outline);
                } else {
                    noteTitle.setEnabled(true);
                    noteBody.setEnabled(true);
                    readOnlyButton.setImageResource(R.drawable.book_open_blank_variant_outline);
                }
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_home) {
            Intent notesActivity = new Intent(NotesActivity.this, MainActivity.class);
            startActivity(notesActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_notes) {
            Intent todoActivity = new Intent(NotesActivity.this, NotesActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_todos) {
            Intent todoActivity = new Intent(NotesActivity.this, TodoActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_flashcards) {
            Intent todoActivity = new Intent(NotesActivity.this, FlashcardActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_calendar) {
            Intent todoActivity = new Intent(NotesActivity.this, TimetableActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_timetable) {
            Intent todoActivity = new Intent(NotesActivity.this, TimetableActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent todoActivity = new Intent(NotesActivity.this, SettingsActivity.class);
            startActivity(todoActivity);
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_logout) {
            Log.d("Message", "Logout");
        }
        else {
            Log.d("Message", "Unknown page!");
        }

        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);

        return true;
    }
}
