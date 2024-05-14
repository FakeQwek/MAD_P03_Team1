package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FlashcardViewHolder extends RecyclerView.ViewHolder {
    TextView question;

    TextView answer;

    CardView cardView;

    ImageButton deleteButton;

    public FlashcardViewHolder(View view) {
        super(view);

        question = view.findViewById(R.id.question);

        answer = view.findViewById(R.id.answer);

        cardView = view.findViewById(R.id.cardView);

        deleteButton = view.findViewById(R.id.deleteButton);
    }
}
