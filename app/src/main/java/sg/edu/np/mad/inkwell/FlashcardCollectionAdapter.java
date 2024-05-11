package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FlashcardCollectionAdapter extends RecyclerView.Adapter<FlashcardCollectionViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<FlashcardCollection> allFlashcardCollections;

    private ArrayList<FlashcardCollection> flashcardCollectionList;

    private FlashcardActivity flashcardActivity;

    public FlashcardCollectionAdapter(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollectionList, FlashcardActivity flashcardActivity) {
        this.allFlashcardCollections = allFlashcardCollections;
        this.flashcardCollectionList = flashcardCollectionList;
        this.flashcardActivity = flashcardActivity;
    }

    public FlashcardCollectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flashcard_collection, viewGroup, false);
        FlashcardCollectionViewHolder holder = new FlashcardCollectionViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(FlashcardCollectionViewHolder holder, int position) {
        FlashcardCollection flashcardCollection = flashcardCollectionList.get(position);
        holder.title.setText(flashcardCollection.getTitle());
        holder.flashcardCount.setText(String.valueOf(flashcardCollection.getFlashcardCount()));

        RecyclerView recyclerView = flashcardActivity.findViewById(R.id.recyclerView);
    }

    public int getItemCount() { return flashcardCollectionList.size(); }
}
