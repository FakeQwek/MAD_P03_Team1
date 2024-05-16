package sg.edu.np.mad.inkwell;

import com.google.firebase.firestore.DocumentReference;

public class File {
    public String title;

    public String body;

    public int id;

    public String type;

    public DocumentReference docRef;

    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public int getId() { return this.id; }

    public String getType() { return this.type; }

    public DocumentReference getDocRef() { return this.docRef; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public File(String title, String body, int id, String type, DocumentReference docRef) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.type = type;
        this.docRef = docRef;
    }
}
