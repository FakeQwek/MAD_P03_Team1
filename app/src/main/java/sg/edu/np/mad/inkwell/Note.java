package sg.edu.np.mad.inkwell;

public class Note {
    public String title;

    public String body;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateBody(String body) {
        this.body = body;
    }

    public Note(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
