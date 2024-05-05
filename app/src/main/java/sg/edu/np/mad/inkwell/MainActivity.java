package sg.edu.np.mad.inkwell;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
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

    private DocumentReference noteDocRef;

    private LinearLayout selectedNoteLayout;

    private LinearLayout selectedFolderLayout;

    private int selectedIdentationLevel;

    private void openFolderBottomSheet(EditText noteTitle) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.folder_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        Button bottomSheetNewFileButton = view.findViewById(R.id.bottomSheetNewNoteButton);
        bottomSheetNewFileButton.setText(R.string.bottom_sheet_new_note_button);

        bottomSheetNewFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFile(noteDocRef.collection("files"), selectedNoteLayout, currentNoteId, findViewById(R.id.noteTitle), findViewById(R.id.noteBody), selectedIdentationLevel + 1);
            }
        });

        Button bottomSheetNewFolderButton = view.findViewById(R.id.bottomSheetNewFolderButton);
        bottomSheetNewFolderButton.setText(R.string.bottom_sheet_new_folder_button);

        bottomSheetNewFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFolder(noteDocRef.collection("files"), selectedFolderLayout, currentNoteId, findViewById(R.id.noteTitle), findViewById(R.id.noteBody), selectedIdentationLevel + 1);
            }
        });

        Button bottomSheetRenameButton = view.findViewById(R.id.bottomSheetRenameButton);
        bottomSheetRenameButton.setText(R.string.bottom_sheet_rename_button);

        bottomSheetRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rename(noteTitle);
            }
        });

        Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
        bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

        bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> deleteFolder = new HashMap<>();
                deleteFolder.put("type", "deleted");

                noteDocRef.update(deleteFolder);

                findViewById(selectedNoteId).setVisibility(View.GONE);
                selectedNoteLayout.setVisibility(View.GONE);
                selectedFolderLayout.setVisibility(View.GONE);
            }
        });
    }

    private void openNoteBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.note_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
        bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

        bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> deleteNote = new HashMap<>();
                deleteNote.put("type", "deleted");

                noteDocRef.update(deleteNote);

                findViewById(selectedNoteId).setVisibility(View.GONE);
            }
        });
    }

    private void createNewFile(CollectionReference colRef, LinearLayout linearLayout, int id, EditText noteTitle, EditText noteBody, int indentationLevel) {
        Button noteButton = new Button(getApplicationContext());
        noteButton.setGravity(Gravity.START);
        noteButton.setBackgroundColor(Color.TRANSPARENT);
        noteButton.setId(id);
        noteButton.setText(R.string.new_note_title);

        RelativeLayout.LayoutParams noteButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        noteButtonParams.setMargins(50 * indentationLevel, 0, 0, 0);

        noteButton.setLayoutParams(noteButtonParams);

        Map<String, Object> noteData = new HashMap<>();
        noteData.put("type", "file");
        noteData.put("title", "Title");
        noteData.put("body", "Enter your text");

        colRef.document(String.valueOf(id)).set(noteData);

        noteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DocumentReference docRef = colRef.document(String.valueOf(id));
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        selectedNoteId = v.getId();
                        noteDocRef = docRef;

                        DocumentSnapshot document = task.getResult();
                        String docNoteTitle = document.getData().get("title").toString();
                        String docNoteBody = document.getData().get("body").toString();
                        noteTitle.setText(docNoteTitle);
                        noteBody.setText(docNoteBody);
                    }
                });
            }
        });

        noteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                noteDocRef = colRef.document(String.valueOf(id));

                openNoteBottomSheet();
                return true;
            }
        });
        linearLayout.addView(noteButton);
    }

    private void createNewFolder(CollectionReference colRef, LinearLayout linearLayout, int id, EditText noteTitle, EditText noteBody, int indentationLevel) {
        Button folderButton = new Button(getApplicationContext());
        folderButton.setGravity(Gravity.START);
        folderButton.setBackgroundColor(Color.TRANSPARENT);
        folderButton.setId(id);
        folderButton.setText(R.string.new_folder_title);

        RelativeLayout.LayoutParams noteButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        noteButtonParams.setMargins(50 * indentationLevel, 0, 0, 0);

        folderButton.setLayoutParams(noteButtonParams);

        Map<String, Object> folderData = new HashMap<>();
        folderData.put("type", "folder");
        folderData.put("title", "Folder");
        folderData.put("body", "");

        colRef.document(String.valueOf(id)).set(folderData);

        linearLayout.addView(folderButton);

        LinearLayout noteLayout = new LinearLayout(getApplicationContext());
        noteLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout folderLayout = new LinearLayout(getApplicationContext());
        folderLayout.setOrientation(LinearLayout.VERTICAL);

        folderButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                selectedNoteLayout = noteLayout;
                selectedFolderLayout = folderLayout;
                noteDocRef = colRef.document(String.valueOf(id));
                selectedIdentationLevel = indentationLevel;

                openFolderBottomSheet(noteTitle);
                return true;
            }
        });

        linearLayout.addView(noteLayout);
        linearLayout.addView(folderLayout);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderLayout.getVisibility() == View.VISIBLE) {
                    noteLayout.setVisibility(View.GONE);
                    folderLayout.setVisibility(View.GONE);
                } else {
                    noteLayout.setVisibility(View.VISIBLE);
                    folderLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void createFileButton(CollectionReference colRef, LinearLayout linearLayout, int id, String title, EditText noteTitle, EditText noteBody, int indentationLevel) {
        if (id > currentNoteId) {
            currentNoteId = id;
        }

        Button noteButton = new Button(getApplicationContext());
        noteButton.setGravity(Gravity.START);
        noteButton.setBackgroundColor(Color.TRANSPARENT);
        noteButton.setId(id);
        noteButton.setText(title);

        RelativeLayout.LayoutParams noteButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        noteButtonParams.setMargins(50 * indentationLevel, 0, 0, 0);

        noteButton.setLayoutParams(noteButtonParams);

        noteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DocumentReference docRef = colRef.document(String.valueOf(id));
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        selectedNoteId = v.getId();

                        noteDocRef = docRef;
                        selectedIdentationLevel = indentationLevel;

                        DocumentSnapshot document = task.getResult();
                        String docNoteTitle = document.getData().get("title").toString();
                        String docNoteBody = document.getData().get("body").toString();
                        noteTitle.setText(docNoteTitle);
                        noteBody.setText(docNoteBody);
                    }
                });
            }
        });

        noteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                noteDocRef = colRef.document(String.valueOf(id));

                openNoteBottomSheet();
                return true;
            }
        });
        linearLayout.addView(noteButton);
    }

    private void createFolderButton(CollectionReference colRef, LinearLayout linearLayout, int id, String title, EditText noteTitle, EditText noteBody, int indentationLevel) {
        if (id > currentNoteId) {
            currentNoteId = id;
        }

        Button folderButton = new Button(getApplicationContext());
        folderButton.setGravity(Gravity.START);
        folderButton.setBackgroundColor(Color.TRANSPARENT);
        folderButton.setId(id);
        folderButton.setText(title);

        RelativeLayout.LayoutParams noteButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        noteButtonParams.setMargins(50 * indentationLevel, 0, 0, 0);

        folderButton.setLayoutParams(noteButtonParams);

        linearLayout.addView(folderButton);

        LinearLayout noteLayout = new LinearLayout(getApplicationContext());
        noteLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout folderLayout = new LinearLayout(getApplicationContext());
        folderLayout.setOrientation(LinearLayout.VERTICAL);

        folderButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                selectedNoteLayout = noteLayout;
                selectedFolderLayout = folderLayout;
                noteDocRef = colRef.document(String.valueOf(id));
                selectedIdentationLevel = indentationLevel;

                openFolderBottomSheet(noteTitle);
                return true;
            }
        });

        colRef.document(String.valueOf(id)).collection("files").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String docNoteType = document.getData().get("type").toString();
                        if (docNoteType.equals("file")) {
                            createFileButton(colRef.document(String.valueOf(id)).collection("files"), noteLayout, Integer.parseInt(document.getId()), document.getData().get("title").toString(), noteTitle, noteBody, indentationLevel + 1);
                        } else if (docNoteType.equals("folder")) {
                            createFolderButton(colRef.document(String.valueOf(id)).collection("files"), folderLayout, Integer.parseInt(document.getId()), document.getData().get("title").toString(), noteTitle, noteBody, indentationLevel + 1);
                        }
                    }
                }
            }
        });

        linearLayout.addView(noteLayout);
        linearLayout.addView(folderLayout);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderLayout.getVisibility() == View.VISIBLE) {
                    noteLayout.setVisibility(View.GONE);
                    folderLayout.setVisibility(View.GONE);
                } else {
                    noteLayout.setVisibility(View.VISIBLE);
                    folderLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void createViewAnimator(EditText noteTitle, EditText noteBody) {
        Button viewAnimatorButton = findViewById(R.id.viewAnimatorButton);

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.setDisplayedChild(2);
            }
        });

        LinearLayout viewOne = findViewById(R.id.noteList);
        LinearLayout viewTwo = findViewById(R.id.menuList);

        Button searchMenuButton = new Button(getApplicationContext());
        searchMenuButton.setBackgroundColor(Color.TRANSPARENT);
        searchMenuButton.setGravity(Gravity.START);
        searchMenuButton.setText("Search");
        viewTwo.addView(searchMenuButton);

        searchMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.setDisplayedChild(2);
            }
        });

        Button tagsButton = new Button(getApplicationContext());
        tagsButton.setBackgroundColor(Color.TRANSPARENT);
        tagsButton.setGravity(Gravity.START);
        tagsButton.setText("Tags");
        viewTwo.addView(tagsButton);

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

        LinearLayout searchList = findViewById(R.id.searchList);

        TextInputEditText searchBarEditText = findViewById(R.id.searchBarEditText);

        searchBarEditText.addTextChangedListener(new TextChangedListener<TextInputEditText>(searchBarEditText) {
            @Override
            public void onTextChanged(TextInputEditText searchBarEditText, Editable s) {
                searchList.removeAllViews();

                search(db.collection("notes"), findViewById(R.id.searchList), searchBarEditText.getText(), noteTitle, noteBody);
            }
        });
    }

    private void search(CollectionReference colRef, LinearLayout linearLayout, Editable searchString, EditText noteTitle, EditText noteBody) {
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

    private void rename(EditText noteTitle) {
        View renamePopupView = LayoutInflater.from(MainActivity.this).inflate(R.layout.rename_popup, null);

        PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

        Button renameButton = renamePopupView.findViewById(R.id.renameButton);

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = renameEditText.getText().toString();

                Map<String, Object> newFolder = new HashMap<>();
                newFolder.put("title", newTitle);

                noteDocRef.update(newFolder);

                Button buttonTitle = findViewById(selectedNoteId);
                buttonTitle.setText(newTitle);

                renamePopupWindow.dismiss();
            }
        });

        renamePopupWindow.showAtLocation(renamePopupView, Gravity.CENTER, 0, 0);
    }

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

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

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
                            createViewAnimator(noteTitle, noteBody);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docNoteType = document.getData().get("type").toString();
                                if (docNoteType.equals("file")) {
                                    createFileButton(db.collection("notes"), findViewById(R.id.noteList), Integer.parseInt(document.getId()), document.getData().get("title").toString(), noteTitle, noteBody, 0);
                                } else if (docNoteType.equals("folder")) {
                                    createFolderButton(db.collection("notes"), findViewById(R.id.noteList), Integer.parseInt(document.getId()), document.getData().get("title").toString(), noteTitle, noteBody, 0);
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                        TextView inkwellDetails = findViewById(R.id.inkwellDetails);
                        String inkwellDetailsText = String.format(getResources().getString(R.string.inkwell_details_text), currentNoteId);
                        inkwellDetails.setText(inkwellDetailsText);
                    }
                });

        noteTitle.addTextChangedListener(new TextChangedListener<EditText>(noteTitle) {
            @Override
            public void onTextChanged(EditText noteTitle, Editable s) {
                note.updateTitle(noteTitle.getText().toString());
                Log.i("testing", noteTitle.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("title", note.title);

                noteDocRef.update(newNote);

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

                noteDocRef.update(newNote);
            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFile(db.collection("notes"), findViewById(R.id.noteList), currentNoteId, noteTitle, noteBody, 0);

                TextView inkwellDetails = findViewById(R.id.inkwellDetails);
                String inkwellDetailsText = String.format(getResources().getString(R.string.inkwell_details_text), currentNoteId);
                inkwellDetails.setText(inkwellDetailsText);
            }
        });

        Button addFolderButton = findViewById(R.id.addFolderButton);

        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFolder(db.collection("notes"), findViewById(R.id.noteList), currentNoteId, noteTitle, noteBody, 0);
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