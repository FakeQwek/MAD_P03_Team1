package sg.edu.np.mad.inkwell;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class TimetableData {
    private String name;
    private String location;
    public String startTime;
    public String endTime;
    private String category;
    private String startDate;
    private String endDate;

    public TimetableData(String name, String location, String startTime, String startDate, String endTime, String endDate, String category) {
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
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
    public  String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

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