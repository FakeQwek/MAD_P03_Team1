package sg.edu.np.mad.inkwell;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ViewAnimator;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TodoAdapter extends RecyclerView.Adapter<TodoViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<Todo> allTodos;

    private ArrayList<Todo> todoList;

    private TodoActivity todoActivity;

    public TodoAdapter(ArrayList<Todo> allTodos, ArrayList<Todo> todoList, TodoActivity todoActivity) {
        this.allTodos = allTodos;
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
        holder.description.setText(todo.getDescription());
        holder.todoDateTime.setText(todo.getTodoDateTime());

        if (todo.todoStatus.equals("inProgress")) {
            holder.cardView2.setCardBackgroundColor(Color.parseColor("#ADD2E8"));
            holder.status.setText("IN PROGRESS");
            holder.status.setTextColor(Color.parseColor("#0029BA"));
            holder.cardView3.setCardBackgroundColor(Color.parseColor("#0029BA"));
        } else if (todo.todoStatus.equals("done")) {
            holder.cardView2.setCardBackgroundColor(Color.parseColor("#ADE8C1"));
            holder.status.setText("DONE");
            holder.status.setTextColor(Color.parseColor("#009C2C"));
            holder.cardView3.setCardBackgroundColor(Color.parseColor("#009C2C"));
        }

        RecyclerView todoRecyclerView = todoActivity.findViewById(R.id.todoRecyclerView);

        Animation slideInLeft = AnimationUtils.loadAnimation(todoActivity, R.anim.slide_in_left);

        holder.cardView1.startAnimation(slideInLeft);

        Animation popup = AnimationUtils.loadAnimation(todoActivity, R.anim.popup);

        holder.cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(todoActivity);
                View view = LayoutInflater.from(todoActivity).inflate(R.layout.todo_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                ViewAnimator viewAnimator = view.findViewById(R.id.viewAnimator);
                TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
                TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);

                if (todo.todoStatus.equals("todo")) {
                    viewAnimator.setDisplayedChild(0);
                } else if (todo.todoStatus.equals("inProgress")) {
                    viewAnimator.setDisplayedChild(1);
                } else {
                    viewAnimator.setDisplayedChild(2);
                }

                titleEditText.setText(todo.todoTitle);
                descriptionEditText.setText(todo.description);

                ImageButton statusLeftButton = view.findViewById(R.id.statusLeftButton);

                statusLeftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewAnimator.showPrevious();
                    }
                });

                ImageButton statusRightButton = view.findViewById(R.id.statusRightButton);

                statusRightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewAnimator.showNext();
                    }
                });

                Button cancelButton = view.findViewById(R.id.cancelButton);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


                Button doneButton = view.findViewById(R.id.doneButton);

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> newTodo = new HashMap<>();
                        newTodo.put("title", titleEditText.getText().toString());
                        newTodo.put("description", descriptionEditText.getText().toString());

                        int displayedChild = viewAnimator.getDisplayedChild();

                        if (displayedChild == 0) {
                            newTodo.put("status", "todo");
                            todo.setTodoStatus("todo");
                        } else if (displayedChild == 1) {
                            newTodo.put("status", "inProgress");
                            todo.setTodoStatus("inProgress");
                        } else {
                            newTodo.put("status", "done");
                            todo.setTodoStatus("done");
                        }

                        db.collection("todos").document(String.valueOf(todo.todoId)).update(newTodo);

                        todo.setTodoTitle(titleEditText.getText().toString());
                        todo.setDescription(descriptionEditText.getText().toString());

                        if (!todo.todoStatus.equals(TodoActivity.currentStatus)) {
                            todoList.remove(todo);
                        }

                        todoRecyclerView.getAdapter().notifyDataSetChanged();

                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }

    public int getItemCount() { return todoList.size(); }
}
