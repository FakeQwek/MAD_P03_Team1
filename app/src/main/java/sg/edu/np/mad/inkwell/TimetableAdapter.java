package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableData> dataList;
    private ArrayList<TimetableData> events;
    private TimetableActivity timetableActivity;
    private HashMap<String, Integer> categoryColors;
    private Context context;

    // Constructor
    public TimetableAdapter(ArrayList<TimetableData> dataList, ArrayList<TimetableData> events, TimetableActivity timetableActivity) {
        this.dataList = dataList;
        this.events = events;
        this.timetableActivity = timetableActivity;
        this.context = timetableActivity;  // Initialize context here
        this.categoryColors = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timetable_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableData data = dataList.get(position);
        holder.tvTitle.setText(data.getName());
        holder.tvDescription.setText(data.getLocation());
        holder.tvStartTime.setText(data.getStartTime());
        holder.tvEndTime.setText(data.getEndTime());
        int categoryColor = getColorForCategory(data.getCategory());

        holder.colorIndicator.setBackgroundColor(categoryColor);
    }

    public void setCategoryColor(String category, int color) {
        categoryColors.put(category, color);
        notifyDataSetChanged(); // Notify RecyclerView of data change
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvDescription, tvStartTime, tvEndTime;
        public View colorIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }
    }

    private int getColorForCategory(String category) {
        Integer color = categoryColors.get(category);
        if (color != null) {
            return color;
        } else {
            return ContextCompat.getColor(context, R.color.white);
        }
    }
}
