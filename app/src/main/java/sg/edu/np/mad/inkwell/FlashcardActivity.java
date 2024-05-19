package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentFlashcardCollectionId keeps track of the ids that have already been assigned
    public static int currentFlashcardCollectionId;

    // selectedFlashcardCollectionId keeps track of the flashcard collection that has been selected
    public static int selectedFlashcardCollectionId;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollections) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FlashcardCollectionAdapter adapter = new FlashcardCollectionAdapter(allFlashcardCollections, flashcardCollections, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<FlashcardCollection> flashcardCollections, String query) {
        ArrayList<FlashcardCollection> filterList = new ArrayList<>();
        for (FlashcardCollection flashcardCollection : flashcardCollections){
            if(flashcardCollection.getTitle().toLowerCase().contains(query)) {
                filterList.add(flashcardCollection);
            }
        }
        recyclerView(flashcardCollections, filterList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

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

        ArrayList<FlashcardCollection> allFlashcardCollections = new ArrayList<>();

        // Read from firebase and create flashcard collections on create
        db.collection("flashcardCollections")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        // Adds items to recycler view on create and everytime new data is added to firebase
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            String docFlashcardCollectionUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docFlashcardCollectionUid.equals(currentFirebaseUserUid)) {
                                if (Integer.parseInt(dc.getDocument().getId()) > currentFlashcardCollectionId) {
                                    currentFlashcardCollectionId = Integer.parseInt(dc.getDocument().getId());
                                }
                                FlashcardCollection flashcardCollection = new FlashcardCollection(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), Integer.parseInt(dc.getDocument().getData().get("flashcardCount").toString()), Integer.parseInt(dc.getDocument().getData().get("correct").toString()));
                                allFlashcardCollections.add(flashcardCollection);
                                filter(allFlashcardCollections, "");
                            }
                        }
                    }
                });

        Button addFlashcardCollectionButton = findViewById(R.id.addFlashcardCollectionButton);

        // Adds a flashcard collection to firebase
        addFlashcardCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFlashcardCollectionId++;

                Map<String, Object> flashcardCollectionData = new HashMap<>();
                flashcardCollectionData.put("title", "New collection");
                flashcardCollectionData.put("flashcardCount", 0);
                flashcardCollectionData.put("correct", 0);
                flashcardCollectionData.put("uid", currentFirebaseUserUid);
                db.collection("flashcardCollections").document(String.valueOf(currentFlashcardCollectionId)).set(flashcardCollectionData);
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_notes) {
            Intent notesActivity = new Intent(FlashcardActivity.this, NotesActivity.class);
            startActivity(notesActivity);
            Log.d( "Message", "Opening notes");
        }
        else if (menuItem.getItemId() == R.id.nav_todo) {
            Intent todoActivity = new Intent(FlashcardActivity.this, TodoActivity.class);
            startActivity(todoActivity);
            Log.d("Message", "Opening home");
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_flashcards) {
            Intent todoActivity = new Intent(FlashcardActivity.this, FlashcardActivity.class);
            startActivity(todoActivity);
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