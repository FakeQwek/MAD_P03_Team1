package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Declaration of variables
    private ArrayList<Object> allNotes;

    private NotesActivity notesActivity;

    // NotesAdapter constructor
    public NotesAdapter(ArrayList<Object> allNotes, NotesActivity notesActivity) {
        this.allNotes = allNotes;
        this.notesActivity = notesActivity;
    }

    // Returns an int based on whether the item is a file or not
    @Override
    public int getItemViewType(int position) {
        if (allNotes.get(position).getClass().getName().equals("sg.edu.np.mad.inkwell.File")) {
            return 0;
        } else {
            return 2;
        }
    }

    // NotesAdapter onCreateViewHolder
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file, viewGroup, false);
            RecyclerView.ViewHolder holder = new FileViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder, viewGroup, false);
            RecyclerView.ViewHolder holder = new FolderViewHolder(view);
            return holder;
        }
    }

    // NotesAdapter onBindViewHolder
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // If item is a file
        if (holder.getItemViewType() == 0) {
            FileViewHolder fileViewHolder = (FileViewHolder) holder;
            File file = (File) allNotes.get(position);
            fileViewHolder.fileButton.setText(file.getTitle());

            if (!NotesActivity.fileIds.contains(file.id)) {
                NotesActivity.fileIds.add(file.id);
                NotesActivity.files.add(file);
            }

            EditText noteTitle = notesActivity.findViewById(R.id.noteTitle);

            EditText noteBody = notesActivity.findViewById(R.id.noteBody);

            fileViewHolder.fileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotesActivity.selectedNoteId = file.getId();
                    NotesActivity.fileOrderIndex++;

                    noteTitle.setText(file.getTitle());
                    noteBody.setText(file.getBody());

                    if (NotesActivity.fileOrder.isEmpty()) {
                        NotesActivity.fileOrder.add(file);
                    } else if (NotesActivity.fileOrder.get(NotesActivity.fileOrderIndex - 1) != file) {
                        Log.d("tester", String.valueOf(NotesActivity.fileOrderIndex));
                        Log.d("tester", String.valueOf(NotesActivity.fileOrder.size()));
                        if (NotesActivity.fileOrderIndex != NotesActivity.fileOrder.size()) {
                            int count = NotesActivity.fileOrder.size() - NotesActivity.fileOrderIndex;
                            Log.d("tester1", String.valueOf(count));
                            for (int i = 0; i < count; i++) {
                                Log.d("tester", "for LOOP");
                                NotesActivity.fileOrder.remove(NotesActivity.fileOrder.size() - 1);
                            }
                        }
                        NotesActivity.fileOrder.add(file);
                    }
                }
            });

            fileViewHolder.fileButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Create new BottomSheetDialog to show note_bottom_sheet.xml
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                    View view = LayoutInflater.from(notesActivity).inflate(R.layout.note_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    // Get bottomSheetDeleteButton Button and set text
                    Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
                    bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

                    // Set on click listener to bottomSheetDeleteButton to delete note
                    bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.docRef.update("type", "deleted");

                            fileViewHolder.fileButton.setVisibility(View.GONE);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });
                    return false;
                }
            });

            // Set text change listener on noteTitle
            noteTitle.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(noteTitle) {
                @Override
                public void onTextChanged(EditText noteTitle, Editable s) {
                    // Update note title data in firebase
                    if (file.id == NotesActivity.selectedNoteId) {
                        file.docRef.update("title", noteTitle.getText().toString());
                        file.setTitle(noteTitle.getText().toString());
                        fileViewHolder.fileButton.setText(noteTitle.getText().toString());
                    }
                }
            });

            // Set text change listener on noteBody
            noteBody.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(noteBody) {
                @Override
                public void onTextChanged(EditText noteBody, Editable s) {
                    // Update note body data in firebase
                    if (file.id == NotesActivity.selectedNoteId) {
                        file.docRef.update("body", noteBody.getText().toString());
                        file.setBody(noteBody.getText().toString());
                    }
                }
            });
            // If item is a folder
        } else {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            Folder folder = (Folder) allNotes.get(position);
            folderViewHolder.folderButton.setText(folder.getTitle());
            ArrayList<Object> folderAllNotes = new ArrayList<>();

            recyclerView(folderAllNotes, folderViewHolder.recyclerView);

            folder.colRef.document(String.valueOf(folder.id)).collection("files")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String docNoteType = document.getData().get("type").toString();
                                    if (docNoteType.equals("file")) {
                                        if (Integer.parseInt(document.getId()) > NotesActivity.currentNoteId) {
                                            NotesActivity.currentNoteId = Integer.parseInt(document.getId());
                                        }

                                        File file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference());
                                        folderAllNotes.add(file);
                                        folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());
                                    } else if (docNoteType.equals("folder")) {
                                        if (Integer.parseInt(document.getId()) > NotesActivity.currentNoteId) {
                                            NotesActivity.currentNoteId = Integer.parseInt(document.getId());
                                        }

                                        Folder folder2 = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, folder.colRef.document(String.valueOf(folder.id)).collection("files"));
                                        folderAllNotes.add(folder2);
                                        folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());
                                    }
                                }
                            } else {
                                Log.d("testing", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            // Toggles visibility of the children elements of a folder
            folderViewHolder.folderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (folderViewHolder.recyclerView.getVisibility() == View.VISIBLE) {
                        folderViewHolder.recyclerView.setVisibility(View.GONE);
                        folderViewHolder.chevron.setImageResource(R.drawable.chevron_up);
                    } else {
                        folderViewHolder.recyclerView.setVisibility(View.VISIBLE);
                        folderViewHolder.chevron.setImageResource(R.drawable.chevron_down);
                    }
                }
            });

            // On long clicking a folder bring up a menu
            folderViewHolder.folderButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Create new BottomSheetDialog to show folder_bottom_sheet.xml
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                    View view = LayoutInflater.from(notesActivity).inflate(R.layout.folder_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    // Get bottomSheetNewFileButton Button and set text
                    Button bottomSheetNewFileButton = view.findViewById(R.id.bottomSheetNewNoteButton);
                    bottomSheetNewFileButton.setText(R.string.bottom_sheet_new_note_button);

                    // Set on click listener to bottomSheetNewFileButton to create new file
                    bottomSheetNewFileButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotesActivity.currentNoteId++;

                            Map<String, Object> fileData = new HashMap<>();
                            fileData.put("title", "Title");
                            fileData.put("body", "Enter your text");
                            fileData.put("type", "file");

                            folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)).set(fileData);

                            File file = new File("Title", "Enter your text", NotesActivity.currentNoteId, "file", folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)));
                            NotesActivity.fileIds.add(file.id);
                            NotesActivity.files.add(file);
                            folderAllNotes.add(0, file);
                            folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_added, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    // Get bottomSheetNewFolderButton Button and set text
                    Button bottomSheetNewFolderButton = view.findViewById(R.id.bottomSheetNewFolderButton);
                    bottomSheetNewFolderButton.setText(R.string.bottom_sheet_new_folder_button);

                    // Set on click listener to bottomSheetNewFolderButton to create new folder
                    bottomSheetNewFolderButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotesActivity.currentNoteId++;

                            Map<String, Object> folderData = new HashMap<>();
                            folderData.put("title", "Folder");
                            folderData.put("body", "");
                            folderData.put("type", "folder");

                            folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)).set(folderData);

                            Folder folder2 = new Folder("Folder", "", NotesActivity.currentNoteId, "folder", folder.colRef.document(String.valueOf(folder.id)).collection("files"));
                            folderAllNotes.add(0, folder2);
                            folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_added, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    // Get bottomSheetRenameButton Button and set text
                    Button bottomSheetRenameButton = view.findViewById(R.id.bottomSheetRenameButton);
                    bottomSheetRenameButton.setText(R.string.bottom_sheet_rename_button);

                    // Set on click listener to bottomSheetRenameButton to rename folder
                    bottomSheetRenameButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View renamePopupView = LayoutInflater.from(notesActivity).inflate(R.layout.rename_popup, null);

                            PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                            TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

                            // Get renameButton Button
                            Button renameButton = renamePopupView.findViewById(R.id.renameButton);

                            // Set on click listener to renameButton to open rename_popup.xml
                            renameButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folder.colRef.document(String.valueOf(folder.id)).update("title", renameEditText.getText().toString());

                                    folderViewHolder.folderButton.setText(renameEditText.getText().toString());

                                    renamePopupWindow.dismiss();
                                }
                            });

                            renamePopupWindow.showAtLocation(renamePopupView, Gravity.CENTER, 0, 0);

                            bottomSheetDialog.dismiss();
                        }
                    });

                    // Get bottomSheetDeleteButton Button and set text
                    Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
                    bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

                    // Set on click listener to bottomSheetDeleteButton to delete folder
                    bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            folder.colRef.document(String.valueOf(folder.id)).update("type", "deleted");

                            folderViewHolder.constraintLayout.setVisibility(View.GONE);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });
                    return false;
                }
            });

        }
    }

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Object> allNotes, RecyclerView recyclerView) {
        NotesAdapter adapter = new NotesAdapter(allNotes, notesActivity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(notesActivity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Returns the size of allNotes
    public int getItemCount() { return allNotes.size(); }
}
