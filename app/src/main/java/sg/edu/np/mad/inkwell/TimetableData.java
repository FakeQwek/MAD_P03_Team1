package sg.edu.np.mad.inkwell;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class TimetableData {
    private String name;
    private String location;
    public String startTime;
    public String endTime;
    private String category;

    public TimetableData(String name, String location, String startTime, String endTime, String category) {
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
    }

    public String getName() { return name; }
    public String getLocation() {
        return location;
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