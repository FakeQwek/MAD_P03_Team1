package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<Flashcard> allFlashcards;

    private ArrayList<Flashcard> flashcardList;

    private ViewFlashcardActivity viewFlashcardActivity;

    private boolean deleteMode = false;

    public FlashcardAdapter(ArrayList<Flashcard> allFlashcards, ArrayList<Flashcard> flashcardList, ViewFlashcardActivity viewFlashcardActivity) {
        this.allFlashcards = allFlashcards;
        this.flashcardList = flashcardList;
        this.viewFlashcardActivity = viewFlashcardActivity;
    }

    public FlashcardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flashcard, viewGroup, false);
        FlashcardViewHolder holder = new FlashcardViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcardList.get(position);
        holder.question.setText(flashcard.getQuestion());
        holder.answer.setText(flashcard.getAnswer());

        if (deleteMode) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = viewFlashcardActivity.findViewById(R.id.recyclerView);

        ImageButton deleteFlashcardButton = viewFlashcardActivity.findViewById(R.id.deleteFlashcardButton);

        deleteFlashcardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteMode) {
                    deleteMode = false;
                } else {
                    deleteMode = true;
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allFlashcards.remove(flashcard);
                flashcardList.remove(flashcard);
                recyclerView.getAdapter().notifyDataSetChanged();

                db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(flashcard.getId())).delete();

                db.collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("flashcardCount", FieldValue.increment(-1));
            }
        });
    }

    public int getItemCount() { return flashcardList.size(); }
}
