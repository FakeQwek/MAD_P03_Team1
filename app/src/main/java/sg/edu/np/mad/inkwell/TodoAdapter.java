package sg.edu.np.mad.inkwell;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TodoAdapter extends RecyclerView.Adapter<TodoViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Todo> todoList;

    private TodoActivity todoActivity;

    public TodoAdapter(ArrayList<Todo> todoList, TodoActivity todoActivity) {
        this.todoList = todoList;
        this.todoActivity = todoActivity;
    }

    public TodoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.todo, viewGroup, false);
        TodoViewHolder holder = new TodoViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.todoTitle.setText(todo.getTodoTitle());
        holder.todoDateTime.setText(todo.getTodoDateTime());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(todoActivity);
                View view = LayoutInflater.from(todoActivity).inflate(R.layout.todo_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                Button todoRenameButton = view.findViewById(R.id.todoRenameButton);
                todoRenameButton.setText("Rename");

                todoRenameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View renamePopupView = LayoutInflater.from(todoActivity).inflate(R.layout.rename_popup, null);

                        PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                        TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

                        Button renameButton = renamePopupView.findViewById(R.id.renameButton);

                        renameButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newTitle = renameEditText.getText().toString();

                                Map<String, Object> newFolder = new HashMap<>();
                                newFolder.put("title", newTitle);

                                db.collection("todos").document(String.valueOf(todo.getTodoId())).update(newFolder);

                                holder.todoTitle.setText(newTitle);

                                renamePopupWindow.dismiss();
                            }
                        });

                        renamePopupWindow.showAtLocation(renamePopupView, Gravity.CENTER, 0, 0);
                    }
                });

                Button todoMoveButton1 = view.findViewById(R.id.todoMoveButton1);
                todoMoveButton1.setText("Move to In Progress");

                Button todoMoveButton2 = view.findViewById(R.id.todoMoveButton2);
                todoMoveButton2.setText("Move to Done");

                Button todoDeleteButton = view.findViewById(R.id.todoDeleteButton);
                todoDeleteButton.setText("Delete");
            }
        });
    }

    public int getItemCount() { return todoList.size(); }
}
