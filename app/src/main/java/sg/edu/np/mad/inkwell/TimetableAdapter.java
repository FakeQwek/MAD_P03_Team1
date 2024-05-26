package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.HashMap;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableData> dataList;
    private HashMap<String, Integer> categoryColors;
    private Context context;

    // Constructor
    public TimetableAdapter(Context context, List<TimetableData> dataList, HashMap<String, Integer> categoryColors) {
        this.context = context;
        this.dataList = dataList;
        this.categoryColors = categoryColors;
    }

    public TimetableAdapter(List<TimetableData> dataList) {
        this.dataList = dataList;
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
        holder.tvTitle.setText(data.getTitle());
        holder.tvDescription.setText(data.getDescription());
        holder.tvStartTime.setText(data.getStartTime());
        holder.tvEndTime.setText(data.getEndTime());
        int categoryColor = getColorForCategory(data.getCategory());

        holder.colorIndicator.setBackgroundColor(categoryColor);
    }

    public void updateCategoryColor(String category, int color) {
        if (categoryColors.containsKey(category)) {
            categoryColors.put(category, color);
            notifyDataSetChanged(); // Notify RecyclerView of data change
        }
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
