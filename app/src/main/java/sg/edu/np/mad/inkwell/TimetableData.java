package sg.edu.np.mad.inkwell;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class TimetableData {
    private String title;
    private String description;
    public String startTime;
    public String endTime;
    private String category;

    public TimetableData(String title, String description, String startTime, String endTime, String category) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
    }

    public String getTitle() {

        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public String getCategory() { return category; }

    // Nested Category class
    public static class Category {
        private String name;
        private int color;

        public Category() {}

        public Category(String name, int color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }
    }
}