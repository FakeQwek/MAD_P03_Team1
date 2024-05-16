package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TodoViewHolder extends RecyclerView.ViewHolder {
    TextView todoTitle;

    TextView description;

    TextView todoDateTime;

    CardView cardView1;

    CardView cardView2;

    TextView status;

    CardView cardView3;

    public TodoViewHolder(View view) {
        super(view);

        todoTitle = view.findViewById(R.id.todoTitle);

        description = view.findViewById(R.id.todoDescription);

        todoDateTime = view.findViewById(R.id.todoDateTime);

        cardView1 = view.findViewById(R.id.cardView1);

        cardView2 = view.findViewById(R.id.cardView2);

        status = view.findViewById(R.id.status);

        cardView3 = view.findViewById(R.id.cardView3);
    }
}
