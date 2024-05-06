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

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Class to add text change listener
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

    // Declaration of variables

    // Latest noteId of most recently created note
    private int currentNoteId;

    // noteId of most recently selected note
    private int selectedNoteId = 1;

    // DocumentReference of most recently selected note
    private DocumentReference noteDocRef;

    // noteLayout of most recently selected note
    private LinearLayout selectedNoteLayout;

    // folderLayout of most recently selected note
    private LinearLayout selectedFolderLayout;

    // indentationLevel of most recently selected note
    private int selectedIndentationLevel;

    // Function to inflate folder_bottom_sheet.xml
    private void openFolderBottomSheet(EditText noteTitle) {
        // Create new BottomSheetDialog to show folder_bottom_sheet.xml
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.folder_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        // Get bottomSheetNewFileButton Button and set text
        Button bottomSheetNewFileButton = view.findViewById(R.id.bottomSheetNewNoteButton);
        bottomSheetNewFileButton.setText(R.string.bottom_sheet_new_note_button);

        // Set on click listener to bottomSheetNewFileButton to create new file
        bottomSheetNewFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFile(noteDocRef.collection("files"), selectedNoteLayout, currentNoteId, findViewById(R.id.noteTitle), findViewById(R.id.noteBody), selectedIndentationLevel + 1);
            }
        });

        // Get bottomSheetNewFolderButton Button and set text
        Button bottomSheetNewFolderButton = view.findViewById(R.id.bottomSheetNewFolderButton);
        bottomSheetNewFolderButton.setText(R.string.bottom_sheet_new_folder_button);

        // Set on click listener to bottomSheetNewFolderButton to create new folder
        bottomSheetNewFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                createNewFolder(noteDocRef.collection("files"), selectedFolderLayout, currentNoteId, findViewById(R.id.noteTitle), findViewById(R.id.noteBody), selectedIndentationLevel + 1);
            }
        });

        // Get bottomSheetRenameButton Button and set text
        Button bottomSheetRenameButton = view.findViewById(R.id.bottomSheetRenameButton);
        bottomSheetRenameButton.setText(R.string.bottom_sheet_rename_button);

        // Set on click listener to bottomSheetRenameButton to rename folder
        bottomSheetRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rename(noteTitle);
            }
        });

        // Get bottomSheetDeleteButton Button and set text
        Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
        bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

        // Set on click listener to bottomSheetDeleteButton to delete folder
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

    // Function to inflate note_bottom_sheet.xml
    private void openNoteBottomSheet() {
        // Create new BottomSheetDialog to show note_bottom_sheet.xml
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.note_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        // Get bottomSheetDeleteButton Button and set text
        Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
        bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

        // Set on click listener to bottomSheetDeleteButton to delete note
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

    // Function to create a new file
    private void createNewFile(CollectionReference colRef, LinearLayout linearLayout, int id, EditText noteTitle, EditText noteBody, int indentationLevel) {
        // Create new Button and set text for noteButton
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

        // Create new file data and sends it to firebase
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("type", "file");
        noteData.put("title", "Title");
        noteData.put("body", "Enter your text");

        colRef.document(String.valueOf(id)).set(noteData);

        // Set on click listener to noteButton to display note details
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

        // Set on long click listener to noteButton to open note_bottom_sheet.xml
        noteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                noteDocRef = colRef.document(String.valueOf(id));

                openNoteBottomSheet();
                return true;
            }
        });

        // Add noteButton to linearLayout
        linearLayout.addView(noteButton);
    }

    // Function to create a new folder
    private void createNewFolder(CollectionReference colRef, LinearLayout linearLayout, int id, EditText noteTitle, EditText noteBody, int indentationLevel) {
        // Create new Button and set text for folderButton
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

        // Create new folder data and sends it to firebase
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("type", "folder");
        folderData.put("title", "Folder");
        folderData.put("body", "");

        colRef.document(String.valueOf(id)).set(folderData);

        // Add noteButton to linearLayout
        linearLayout.addView(folderButton);

        // Create new LinearLayout noteLayout and folderLayout
        LinearLayout noteLayout = new LinearLayout(getApplicationContext());
        noteLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout folderLayout = new LinearLayout(getApplicationContext());
        folderLayout.setOrientation(LinearLayout.VERTICAL);

        // Set on click listener to folderButton to open folder_bottom_sheet.xml
        folderButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                selectedNoteLayout = noteLayout;
                selectedFolderLayout = folderLayout;
                noteDocRef = colRef.document(String.valueOf(id));
                selectedIndentationLevel = indentationLevel;

                openFolderBottomSheet(noteTitle);
                return true;
            }
        });

        // Add noteLayout and folderLayout to linearLayout
        linearLayout.addView(noteLayout);
        linearLayout.addView(folderLayout);

        // Set on click listener to folderButton to toggle view of its children elements
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

    // Function to create file on app load
    private void createFileButton(CollectionReference colRef, LinearLayout linearLayout, int id, String title, EditText noteTitle, EditText noteBody, int indentationLevel) {
        if (id > currentNoteId) {
            currentNoteId = id;
        }

        // Create new Button and set text for noteButton
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

        // Set on click listener to noteButton to display note details
        noteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DocumentReference docRef = colRef.document(String.valueOf(id));
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        selectedNoteId = v.getId();

                        noteDocRef = docRef;
                        selectedIndentationLevel = indentationLevel;

                        DocumentSnapshot document = task.getResult();
                        String docNoteTitle = document.getData().get("title").toString();
                        String docNoteBody = document.getData().get("body").toString();
                        noteTitle.setText(docNoteTitle);
                        noteBody.setText(docNoteBody);
                    }
                });
            }
        });

        // Set on long click listener to noteButton to open note_bottom_sheet.xml
        noteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                noteDocRef = colRef.document(String.valueOf(id));

                openNoteBottomSheet();
                return true;
            }
        });

        // Add noteButton to linearLayout
        linearLayout.addView(noteButton);
    }

    // Function to create folder on app load
    private void createFolderButton(CollectionReference colRef, LinearLayout linearLayout, int id, String title, EditText noteTitle, EditText noteBody, int indentationLevel) {
        if (id > currentNoteId) {
            currentNoteId = id;
        }

        // Create new Button and set text for folderButton
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

        // Add folder button to linearLayout
        linearLayout.addView(folderButton);

        // Create new LinearLayout noteLayout and folderLayout
        LinearLayout noteLayout = new LinearLayout(getApplicationContext());
        noteLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout folderLayout = new LinearLayout(getApplicationContext());
        folderLayout.setOrientation(LinearLayout.VERTICAL);

        // Set on long click listener to folderButton to open folder_bottom_sheet.xml
        folderButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedNoteId = id;
                selectedNoteLayout = noteLayout;
                selectedFolderLayout = folderLayout;
                noteDocRef = colRef.document(String.valueOf(id));
                selectedIndentationLevel = indentationLevel;

                openFolderBottomSheet(noteTitle);
                return true;
            }
        });

        // Call recursive function to create folder children elements on load
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

        // Add noteLayout and folderLayout to linearLayout
        linearLayout.addView(noteLayout);
        linearLayout.addView(folderLayout);

        // Set on click listener to folderButton to toggle view of its children elements
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
        searchBarEditText.addTextChangedListener(new TextChangedListener<TextInputEditText>(searchBarEditText) {
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

    // Function to rename files and folders
    private void rename(EditText noteTitle) {
        View renamePopupView = LayoutInflater.from(MainActivity.this).inflate(R.layout.rename_popup, null);

        PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

        // Get renameButton Button
        Button renameButton = renamePopupView.findViewById(R.id.renameButton);

        // Set on click listener to renameButton to open rename_popup.xml
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

        // Get addNoteButton Button
        Button addNoteButton = findViewById(R.id.addNoteButton);

        // Create new Note object
        Note note = new Note("", "");

        // Get noteTitle and noteBody EditText and set text
        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteBody = findViewById(R.id.noteBody);
        noteTitle.setText(note.title);
        noteBody.setText(note.body);

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

        // Set text change listener on noteTitle
        noteTitle.addTextChangedListener(new TextChangedListener<EditText>(noteTitle) {
            @Override
            public void onTextChanged(EditText noteTitle, Editable s) {
                // Update note title data in firebase
                note.updateTitle(noteTitle.getText().toString());
                Log.i("testing", noteTitle.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("title", note.title);

                noteDocRef.update(newNote);

                Button buttonTitle = findViewById(selectedNoteId);
                buttonTitle.setText(note.title);
            }
        });

        // Set text change listener on noteBody
        noteBody.addTextChangedListener(new TextChangedListener<EditText>(noteBody) {
            @Override
            public void onTextChanged(EditText noteBody, Editable s) {
                // Update note body data in firebase
                note.updateBody(noteBody.getText().toString());
                Log.i("testing", noteBody.getText().toString());

                Map<String, Object> newNote = new HashMap<>();
                newNote.put("body", note.body);

                noteDocRef.update(newNote);
            }
        });

        // Set on click listener to addNoteButton to create new file
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

        // Get addFolderButton Button
        Button addFolderButton = findViewById(R.id.addFolderButton);

        // Set on click listener to addFolderButton to create new folder
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