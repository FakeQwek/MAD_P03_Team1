package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import sg.edu.np.mad.inkwell.R;
import sg.edu.np.mad.inkwell.TimetableActivity;
import sg.edu.np.mad.inkwell.TimetableData;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableData> dataList;
    private ArrayList<TimetableData> events;
    private TimetableActivity timetableActivity;
    private HashMap<String, Integer> categoryColors;
    private TimetableActivity context;
    private FirebaseFirestore db;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TimetableData item);
    }

    // Constructor
    public TimetableAdapter(ArrayList<TimetableData> dataList, ArrayList<TimetableData> events, TimetableActivity timetableActivity) {
        this.dataList = dataList;
        this.events = events;
        this.timetableActivity = timetableActivity;
        this.context = timetableActivity;  // Initialize context here
        this.categoryColors = new HashMap<>();
        this.db = FirebaseFirestore.getInstance();

        // Fetch category colors initially
        fetchCategoryColors();
    }

    // Fetch category colors from Firestore
    private void fetchCategoryColors() {

        db.collection("users").document(userId).collection("categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String category = document.getId();
                                String categoryColor = document.getString("color");
                                if (categoryColor != null && !categoryColor.isEmpty()) {
                                    int color = Color.parseColor(categoryColor); // Convert color string to int
                                    setCategoryColor(category, color);
                                }
                            }
                            notifyDataSetChanged(); // Notify RecyclerView of data change after fetching colors
                        }
                    }
                });
    }

    // Set category color in HashMap
    public void setCategoryColor(String category, int color) {
        categoryColors.put(category, color);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timetable_card, parent, false);
        return new ViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableData data = dataList.get(position);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String date = dateFormat2.format(calendar.getTime());

        String documentId = data.getDocumentId();
        holder.tvTitle.setText(data.getName());
        holder.tvDescription.setText(data.getLocation());
        holder.tvStartTime.setText(data.getStartTime());
        holder.tvEndTime.setText(data.getEndTime());
        holder.category.setText(data.getCategory());
        int categoryColor = getColorForCategory(data.getCategory());

        holder.catCard.setBackgroundColor(categoryColor);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TimetableActivity) v.getContext()).onItemClick(data, userId, documentId, db, events, date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvDescription, tvStartTime, tvEndTime, category;
        public View catCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            category = itemView.findViewById(R.id.category);
            catCard = itemView.findViewById(R.id.catCard);
        }
    }

    private int getColorForCategory(String category) {
        Integer color = categoryColors.get(category);
        if (color != null) {
            return color;
        } else {
            return ContextCompat.getColor(context, R.color.white); // Default color
        }
    }
}
