package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoViewHolder> {
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
    }

    public int getItemCount() { return todoList.size(); }
}
