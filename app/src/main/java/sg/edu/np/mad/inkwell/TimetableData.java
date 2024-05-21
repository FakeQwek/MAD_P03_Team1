package sg.edu.np.mad.inkwell;

public class TimetableData {
    private String title;
    private String description;
    public String startTime;
    public String endTime;

    public TimetableData(String title, String description, String startTime, String endTime) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
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

}