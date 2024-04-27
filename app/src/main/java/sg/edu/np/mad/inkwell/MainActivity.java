package sg.edu.np.mad.inkwell;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.inkwell.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public abstract class TextChangedListener<T> implements TextWatcher {
        private T target;

        public TextChangedListener(T target) {
            this.target = target;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            this.onTextChanged(target, s);
        }

        public abstract void onTextChanged(T target, Editable s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Button addNoteButton = findViewById(R.id.addNoteButton);

        Note note = new Note("Title", "Enter your text");

        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteBody = findViewById(R.id.noteBody);

        noteTitle.setText(note.title);
        noteBody.setText(note.body);

        db.collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Button noteButton = new Button(getApplicationContext());
                                noteButton.setBackgroundColor(Color.WHITE);
                                noteButton.setGravity(Gravity.LEFT);

                                int noteId = Integer.parseInt(document.getId());

                                noteButton.setId(noteId);
                                noteButton.setText(document.getData().get("title").toString());

                                noteButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        DocumentReference docRef = db.collection("notes").document(String.valueOf(noteId));
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                String docNoteTitle = document.getData().get("title").toString();
                                                noteTitle.setText(docNoteTitle);
                                            }
                                        });
                                    }
                                });

                                LinearLayout noteList = findViewById(R.id.noteList);
                                noteList.addView(noteButton);
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });
        noteTitle.addTextChangedListener(new TextChangedListener<EditText>(noteTitle) {
            @Override
            public void onTextChanged(EditText noteTitle, Editable s) {
                note.updateTitle(noteTitle.getText().toString());
                Log.i("testing", noteTitle.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("title", note.title);

                db.collection("notes").document("one")
                        .update(newNote)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("testing", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("testing", "Error writing document", e);
                            }
                        });
            }
        });

        noteBody.addTextChangedListener(new TextChangedListener<EditText>(noteBody) {
            @Override
            public void onTextChanged(EditText noteBody, Editable s) {
                note.updateBody(noteBody.getText().toString());
                Log.i("testing", noteBody.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("body", note.body);


                db.collection("notes").document("one")
                        .update(newNote)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("testing", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("testing", "Error writing document", e);
                            }
                        });
            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button noteButton = new Button(getApplicationContext());
                noteButton.setBackgroundColor(Color.WHITE);
                noteButton.setGravity(Gravity.LEFT);

                int noteId = noteButton.generateViewId();

                Map<String, Object> noteData = new HashMap<>();
                noteData.put("title", "Title");
                noteData.put("body", "Enter your text");

                db.collection("notes").document(String.valueOf(noteId)).set(noteData);

                noteButton.setId(noteId);

                noteButton.setText("new note");

                noteButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DocumentReference docRef = db.collection("notes").document(String.valueOf(noteId));
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                String docNoteTitle = document.getData().get("title").toString();
                                noteTitle.setText(docNoteTitle);
                            }
                        });
                    }
                });

                LinearLayout noteList = findViewById(R.id.noteList);
                noteList.addView(noteButton);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}