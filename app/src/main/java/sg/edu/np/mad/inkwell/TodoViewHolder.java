package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TodoViewHolder extends RecyclerView.ViewHolder {
    TextView todoTitle;

    TextView todoDateTime;

    CardView cardView;

    public TodoViewHolder(View view) {
        super(view);

        todoTitle = view.findViewById(R.id.todoTitle);

        todoDateTime = view.findViewById(R.id.todoDateTime);

        cardView = view.findViewById(R.id.cardView);
    }
}
