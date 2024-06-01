package sg.edu.np.mad.inkwell;

public class TimetableData {
    private String name;
    private String location;
    public String startTime;
    public String endTime;
    private String category;
    private String startDate;
    private String endDate;
    private String documentId;

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
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}