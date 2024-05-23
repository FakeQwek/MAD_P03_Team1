package sg.edu.np.mad.inkwell;

import com.google.firebase.firestore.DocumentReference;

public class File {
    // File class attributes
    public String title;

    public String body;

    public int id;

    public String type;

    public DocumentReference docRef;

    // File class get methods
    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public int getId() { return this.id; }

    public String getType() { return this.type; }

    public DocumentReference getDocRef() { return this.docRef; }

    // File class set methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // File class constructor
    public File(String title, String body, int id, String type, DocumentReference docRef) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.type = type;
        this.docRef = docRef;
    }
}
