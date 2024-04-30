package sg.edu.np.mad.inkwell;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import sg.edu.np.mad.inkwell.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public abstract static class TextChangedListener<T> implements TextWatcher {
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

    private int currentNoteId;

    private int selectedNoteId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding;

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

        Note note = new Note("", "");

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
                                String docNoteType = document.getData().get("type").toString();

                                if (docNoteType.equals("file")) {
                                    Button noteButton = new Button(getApplicationContext());
                                    noteButton.setBackgroundColor(Color.WHITE);
                                    noteButton.setGravity(Gravity.START);
                                    noteButton.setBackgroundColor(Color.TRANSPARENT);

                                    int noteId = Integer.parseInt(document.getId());

                                    currentNoteId++;

                                    noteButton.setId(noteId);
                                    noteButton.setText(document.getData().get("title").toString());

                                    noteButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            DocumentReference docRef = db.collection("notes").document(String.valueOf(noteId));
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    selectedNoteId = v.getId();
                                                    Log.d("testing", String.valueOf(selectedNoteId));

                                                    DocumentSnapshot document = task.getResult();
                                                    String docNoteTitle = document.getData().get("title").toString();
                                                    String docNoteBody = document.getData().get("body").toString();
                                                    noteTitle.setText(docNoteTitle);
                                                    noteBody.setText(docNoteBody);
                                                }
                                            });
                                        }
                                    });

                                    LinearLayout noteList = findViewById(R.id.noteList);
                                    noteList.addView(noteButton);
                                } else if (docNoteType.equals("folder")) {
                                    Button folderButton = new Button(getApplicationContext());
                                    folderButton.setBackgroundColor(Color.WHITE);
                                    folderButton.setGravity(Gravity.START);
                                    folderButton.setBackgroundColor(Color.TRANSPARENT);

                                    int folderId = Integer.parseInt(document.getId());

                                    currentNoteId++;

                                    folderButton.setId(folderId);
                                    folderButton.setText(document.getData().get("title").toString());

                                    LinearLayout noteList = findViewById(R.id.noteList);
                                    noteList.addView(folderButton);

                                    LinearLayout folderLayout = new LinearLayout(getApplicationContext());
                                    folderLayout.setOrientation(LinearLayout.VERTICAL);

                                    folderButton.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            Button noteButton = new Button(getApplicationContext());
                                            noteButton.setBackgroundColor(Color.WHITE);
                                            noteButton.setGravity(Gravity.START);
                                            noteButton.setBackgroundColor(Color.TRANSPARENT);

                                            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                            );

                                            buttonParams.setMargins(50, 0, 0, 0);

                                            noteButton.setLayoutParams(buttonParams);

                                            currentNoteId++;
                                            int noteId = currentNoteId;
                                            Log.d("testing", String.valueOf(currentNoteId));

                                            Map<String, Object> noteData = new HashMap<>();
                                            noteData.put("type", "file");
                                            noteData.put("title", "Title");
                                            noteData.put("body", "Enter your text");
                                            noteData.put("parentId", folderId);

                                            db.collection("notes").document(String.valueOf(folderId)).collection("files").document(String.valueOf(noteId)).set(noteData);

                                            noteButton.setId(noteId);

                                            noteButton.setText(R.string.new_note_title);

                                            noteButton.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    DocumentReference docRef = db.collection("notes").document(String.valueOf(folderId)).collection("files").document(String.valueOf(noteId));
                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            selectedNoteId = v.getId();

                                                            DocumentSnapshot document = task.getResult();
                                                            String docNoteTitle = document.getData().get("title").toString();
                                                            String docNoteBody = document.getData().get("body").toString();
                                                            noteTitle.setText(docNoteTitle);
                                                            noteBody.setText(docNoteBody);
                                                        }
                                                    });
                                                }
                                            });

                                            folderLayout.addView(noteButton);
                                            return true;
                                        }
                                    });

                                    db.collection("notes")
                                            .document(String.valueOf(folderId))
                                            .collection("files")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Button noteButton = new Button(getApplicationContext());
                                                            noteButton.setBackgroundColor(Color.WHITE);
                                                            noteButton.setGravity(Gravity.START);
                                                            noteButton.setBackgroundColor(Color.TRANSPARENT);

                                                            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                                            );

                                                            buttonParams.setMargins(50, 0, 0, 0);

                                                            noteButton.setLayoutParams(buttonParams);

                                                            int noteId = Integer.parseInt(document.getId());

                                                            currentNoteId++;

                                                            noteButton.setId(noteId);
                                                            noteButton.setText(document.getData().get("title").toString());

                                                            noteButton.setOnClickListener(new View.OnClickListener() {
                                                                public void onClick(View v) {
                                                                    DocumentReference docRef = db.collection("notes").document(String.valueOf(folderId)).collection("files").document(String.valueOf(noteId));
                                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            selectedNoteId = v.getId();
                                                                            Log.d("testing", String.valueOf(selectedNoteId));

                                                                            DocumentSnapshot document = task.getResult();
                                                                            String docNoteTitle = document.getData().get("title").toString();
                                                                            String docNoteBody = document.getData().get("body").toString();
                                                                            noteTitle.setText(docNoteTitle);
                                                                            noteBody.setText(docNoteBody);
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            folderLayout.addView(noteButton);
                                                        }
                                                    } else {
                                                        Log.d("testing", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                    noteList.addView(folderLayout);

                                    folderButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (folderLayout.getVisibility() == View.VISIBLE) {
                                                folderLayout.setVisibility(View.GONE);
                                            } else {
                                                folderLayout.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            }

                            TextView inkwellDetails = findViewById(R.id.inkwellDetails);
                            String inkwellDetailsText = String.format(getResources().getString(R.string.inkwell_details_text), currentNoteId);
                            inkwellDetails.setText(inkwellDetailsText);

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

                db.collection("notes").document(String.valueOf(selectedNoteId))
                        .update(newNote);

                db.collection("notes")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("testing", document.getId() + " => " + document.getData());
                                    document.getReference().collection("files").document(String.valueOf(selectedNoteId)).update(newNote);
                                }
                            }
                        });

                Button buttonTitle = findViewById(selectedNoteId);
                buttonTitle.setText(note.title);
            }
        });

        noteBody.addTextChangedListener(new TextChangedListener<EditText>(noteBody) {
            @Override
            public void onTextChanged(EditText noteBody, Editable s) {
                note.updateBody(noteBody.getText().toString());
                Log.i("testing", noteBody.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("body", note.body);


                db.collection("notes").document(String.valueOf(selectedNoteId))
                        .update(newNote);

                db.collection("notes")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("testing", document.getId() + " => " + document.getData());
                                    document.getReference().collection("files").document(String.valueOf(selectedNoteId)).update(newNote);
                                }
                            }
                        });
            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button noteButton = new Button(getApplicationContext());
                noteButton.setBackgroundColor(Color.WHITE);
                noteButton.setGravity(Gravity.START);
                noteButton.setBackgroundColor(Color.TRANSPARENT);

                currentNoteId++;
                int noteId = currentNoteId;

                Map<String, Object> noteData = new HashMap<>();
                noteData.put("type", "file");
                noteData.put("title", "Title");
                noteData.put("body", "Enter your text");

                db.collection("notes").document(String.valueOf(noteId)).set(noteData);

                noteButton.setId(noteId);

                noteButton.setText(R.string.new_note_title);

                noteButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DocumentReference docRef = db.collection("notes").document(String.valueOf(noteId));
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                selectedNoteId = v.getId();

                                DocumentSnapshot document = task.getResult();
                                String docNoteTitle = document.getData().get("title").toString();
                                String docNoteBody = document.getData().get("body").toString();
                                noteTitle.setText(docNoteTitle);
                                noteBody.setText(docNoteBody);
                            }
                        });
                    }
                });

                LinearLayout noteList = findViewById(R.id.noteList);
                noteList.addView(noteButton);

                TextView inkwellDetails = findViewById(R.id.inkwellDetails);
                String inkwellDetailsText = String.format(getResources().getString(R.string.inkwell_details_text), currentNoteId);
                inkwellDetails.setText(inkwellDetailsText);
            }
        });

        addNoteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("testing", "apple");
                return true;
            }
        });

        Button addFolderButton = findViewById(R.id.addFolderButton);

        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button folderButton = new Button(getApplicationContext());
                folderButton.setBackgroundColor(Color.WHITE);
                folderButton.setGravity(Gravity.START);
                folderButton.setBackgroundColor(Color.TRANSPARENT);

                currentNoteId++;
                int folderId = currentNoteId;

                Map<String, Object> folderData = new HashMap<>();
                folderData.put("type", "folder");
                folderData.put("title", "Folder");
                folderData.put("body", "");

                db.collection("notes").document(String.valueOf(folderId)).set(folderData);

                folderButton.setId(folderId);

                folderButton.setText(R.string.new_folder_title);

                LinearLayout noteList = findViewById(R.id.noteList);
                noteList.addView(folderButton);

                LinearLayout folderLayout = new LinearLayout(getApplicationContext());
                folderLayout.setOrientation(LinearLayout.VERTICAL);

                folderButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Button noteButton = new Button(getApplicationContext());
                        noteButton.setBackgroundColor(Color.WHITE);
                        noteButton.setGravity(Gravity.START);
                        noteButton.setBackgroundColor(Color.TRANSPARENT);

                        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );

                        buttonParams.setMargins(50, 0, 0, 0);

                        noteButton.setLayoutParams(buttonParams);

                        currentNoteId++;
                        int noteId = currentNoteId;

                        Map<String, Object> noteData = new HashMap<>();
                        noteData.put("type", "file");
                        noteData.put("title", "Title");
                        noteData.put("body", "Enter your text");
                        noteData.put("parentId", folderId);

                        db.collection("notes").document(String.valueOf(folderId)).collection("files").document(String.valueOf(noteId)).set(noteData);

                        noteButton.setId(noteId);

                        noteButton.setText(R.string.new_note_title);

                        noteButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                DocumentReference docRef = db.collection("notes").document(String.valueOf(folderId)).collection("files").document(String.valueOf(noteId));
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        selectedNoteId = v.getId();

                                        DocumentSnapshot document = task.getResult();
                                        String docNoteTitle = document.getData().get("title").toString();
                                        String docNoteBody = document.getData().get("body").toString();
                                        noteTitle.setText(docNoteTitle);
                                        noteBody.setText(docNoteBody);
                                    }
                                });
                            }
                        });

                        folderLayout.addView(noteButton);
                        return true;
                    }
                });

                noteList.addView(folderLayout);

                folderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (folderLayout.getVisibility() == View.VISIBLE) {
                            folderLayout.setVisibility(View.GONE);
                        } else {
                            folderLayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
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