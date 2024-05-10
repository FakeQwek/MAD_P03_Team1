package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int currentFlashcardCollectionId;

    private void recyclerView(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollections) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FlashcardCollectionAdapter adapter = new FlashcardCollectionAdapter(allFlashcardCollections, flashcardCollections, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        ArrayList<FlashcardCollection> allFlashcardCollections = new ArrayList<>();

        db.collection("flashcardCollections")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                if (Integer.parseInt(dc.getDocument().getId()) > currentFlashcardCollectionId) {
                                    currentFlashcardCollectionId = Integer.parseInt(dc.getDocument().getId());
                                }
                                FlashcardCollection flashcardCollection = new FlashcardCollection(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), Integer.parseInt(dc.getDocument().getData().get("flashcardCount").toString()));
                                allFlashcardCollections.add(flashcardCollection);
                                filter(allFlashcardCollections, "");
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                Log.d("tester", String.valueOf(allFlashcardCollections.size()));
                            }
                        }
                    }
                });

        Button addFlashcardCollectionButton = findViewById(R.id.addFlashcardCollectionButton);

        addFlashcardCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> flashcardCollectionData = new HashMap<>();
                flashcardCollectionData.put("title", "New collection");
                flashcardCollectionData.put("flashcardCount", 0);
                db.collection("flashcardCollections").document(String.valueOf(currentFlashcardCollectionId + 1)).set(flashcardCollectionData);
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