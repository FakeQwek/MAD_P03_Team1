package sg.edu.np.mad.inkwell;

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
    public String getCategory() {
        return category;
    }

}