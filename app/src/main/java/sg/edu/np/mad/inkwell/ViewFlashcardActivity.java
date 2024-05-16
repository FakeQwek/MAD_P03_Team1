package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintProperties;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewFlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int currentFlashcardId;

    private void recyclerView(ArrayList<Flashcard> allFlashcards, ArrayList<Flashcard> flashcards) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FlashcardAdapter adapter = new FlashcardAdapter(allFlashcards, flashcards, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<Flashcard> flashcards, String query) {
        ArrayList<Flashcard> filterList = new ArrayList<>();
        for (Flashcard flashcard : flashcards){
            if(flashcard.getQuestion().toLowerCase().contains(query)) {
                filterList.add(flashcard);
            }
        }
        recyclerView(flashcards, filterList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flashcard);

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

        ArrayList<Flashcard> allFlashcards = new ArrayList<>();

        db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards")
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
                                if (Integer.parseInt(dc.getDocument().getId()) > currentFlashcardId) {
                                    currentFlashcardId = Integer.parseInt(dc.getDocument().getId());
                                }
                                Flashcard flashcard = new Flashcard(dc.getDocument().getData().get("question").toString(), dc.getDocument().getData().get("answer").toString(), Integer.parseInt(dc.getDocument().getId()));
                                allFlashcards.add(flashcard);
                                filter(allFlashcards, "");
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                Log.d("tester", String.valueOf(allFlashcards.size()));
                            }
                        }
                    }
                });

        ImageButton addFlashcardButton = findViewById(R.id.addFlashcardButton);

        addFlashcardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> flashcardData = new HashMap<>();
                flashcardData.put("question", "New flashcard");
                flashcardData.put("answer", "answer");
                db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(currentFlashcardId + 1)).set(flashcardData);

                db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("flashcardCount", FieldValue.increment(1));
            }
        });

        Button quizButton = findViewById(R.id.quizButton);

        quizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allFlashcards.isEmpty()) {
                    Intent quiz = new Intent(ViewFlashcardActivity.this, QuizFlashcardActivity.class);
                    startActivity(quiz);
                } else {

                }
            }
        });

    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_notes) {
            Intent notesActivity = new Intent(ViewFlashcardActivity.this, NotesActivity.class);
            startActivity(notesActivity);
            Log.d( "Message", "Opening notes");
        }
        else if (menuItem.getItemId() == R.id.nav_todo) {
            Intent todoActivity = new Intent(ViewFlashcardActivity.this, TodoActivity.class);
            startActivity(todoActivity);
            Log.d("Message", "Opening home");
            return true;
        }
        else if (menuItem.getItemId() == R.id.nav_flashcards) {
            Intent todoActivity = new Intent(ViewFlashcardActivity.this, FlashcardActivity.class);
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