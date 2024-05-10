package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FlashcardCollectionViewHolder extends RecyclerView.ViewHolder {
    TextView title;

    TextView flashcardCount;

    CardView cardView;

    public FlashcardCollectionViewHolder(View view) {
        super(view);

        title = view.findViewById(R.id.title);

        flashcardCount = view.findViewById(R.id.flashcardCount);

        cardView = view.findViewById(R.id.cardView);
    }
}
