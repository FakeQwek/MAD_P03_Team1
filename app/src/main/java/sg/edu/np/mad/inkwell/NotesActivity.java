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
import android.widget.TextView;
import android.widget.ViewAnimator;

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

    // DocumentReference of most recently selected note
    private DocumentReference noteDocRef;

    // noteLayout of most recently selected note
    private LinearLayout selectedNoteLayout;

    // folderLayout of most recently selected note
    private LinearLayout selectedFolderLayout;

    // Function to add elements to viewAnimator on app load
    private void createViewAnimator(EditText noteTitle, EditText noteBody) {
        // Get viewAnimatorButton Button
        Button viewAnimatorButton = findViewById(R.id.viewAnimatorButton);

        // Get viewAnimator ViewAnimator
        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        // Get searchButton Button
        Button searchButton = findViewById(R.id.searchButton);

        // Set on click listener to searchButton to open searchMenu
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.setDisplayedChild(2);
            }
        });

        LinearLayout viewOne = findViewById(R.id.noteList);
        LinearLayout viewTwo = findViewById(R.id.menuList);

        // Create new Button and set text for searchMenuButton
        Button searchMenuButton = new Button(getApplicationContext());
        searchMenuButton.setBackgroundColor(Color.TRANSPARENT);
        searchMenuButton.setGravity(Gravity.START);
        searchMenuButton.setText("Search");
        viewTwo.addView(searchMenuButton);

        // Set on click listener to searchMenuButton to open searchMenu
        searchMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.setDisplayedChild(2);
            }
        });

        // Create new Button and set text for tagsButton
        Button tagsButton = new Button(getApplicationContext());
        tagsButton.setBackgroundColor(Color.TRANSPARENT);
        tagsButton.setGravity(Gravity.START);
        tagsButton.setText("Tags");
        viewTwo.addView(tagsButton);

        // Set on click listener to viewAnimatorButton to change displayed child
        viewAnimatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewAnimator.getDisplayedChild() == 0) {
                    viewAnimator.setDisplayedChild(1);
                } else {
                    viewAnimator.setDisplayedChild(0);
                }
            }
        });

        // Get searchList LinearLayout
        LinearLayout searchList = findViewById(R.id.searchList);

        // Get searchBarEditText TextInputEditText
        TextInputEditText searchBarEditText = findViewById(R.id.searchBarEditText);

        // Set text change listener to searchBarEditText
        searchBarEditText.addTextChangedListener(new MainActivity.TextChangedListener<TextInputEditText>(searchBarEditText) {
            @Override
            public void onTextChanged(TextInputEditText searchBarEditText, Editable s) {
                searchList.removeAllViews();

                search(db.collection("notes"), findViewById(R.id.searchList), searchBarEditText.getText(), noteTitle, noteBody);
            }
        });
    }

    // Function to search for existing files
    private void search(CollectionReference colRef, LinearLayout linearLayout, Editable searchString, EditText noteTitle, EditText noteBody) {
        // Read from firebase and create files on search
        colRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docNoteType = document.getData().get("type").toString();
                                int docNoteId = Integer.parseInt(document.getId());
                                if (docNoteType.equals("file")) {
                                    String docNoteTitle = document.getData().get("title").toString();
                                    if (docNoteTitle.contains(searchString)) {
                                        Button noteButton = new Button(getApplicationContext());
                                        noteButton.setGravity(Gravity.START);
                                        noteButton.setBackgroundColor(Color.TRANSPARENT);

                                        String docNoteBody = document.getData().get("body").toString();

                                        noteButton.setId(docNoteId);
                                        noteButton.setText(docNoteTitle);

                                        noteButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                selectedNoteId = docNoteId;

                                                noteDocRef = document.getReference();

                                                noteTitle.setText(docNoteTitle);
                                                noteBody.setText(docNoteBody);
                                            }
                                        });
                                        linearLayout.addView(noteButton);
                                    }
                                } else if (docNoteType.equals("folder")) {
                                    TextInputEditText searchBarEditText = findViewById(R.id.searchBarEditText);

                                    search(colRef.document(String.valueOf(docNoteId)).collection("files"), findViewById(R.id.searchList), searchBarEditText.getText(), noteTitle, noteBody);
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void recyclerView(ArrayList<Object> allNotes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        NotesAdapter adapter = new NotesAdapter(allNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
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

        ArrayList<Object> allNotes = new ArrayList<>();

        // Read from firebase and create files and folders on app load
        db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            createViewAnimator(noteTitle, noteBody);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docNoteType = document.getData().get("type").toString();
                                if (docNoteType.equals("file")) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    File file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference());
                                    allNotes.add(file);
                                    recyclerView(allNotes);
                                } else if (docNoteType.equals("folder")) {
                                    if (Integer.parseInt(document.getId()) > currentNoteId) {
                                        currentNoteId = Integer.parseInt(document.getId());
                                    }

                                    Folder folder = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, db.collection("notes"));
                                    allNotes.add(folder);
                                    recyclerView(allNotes);
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
