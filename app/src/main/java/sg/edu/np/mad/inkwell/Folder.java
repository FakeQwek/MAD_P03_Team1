package sg.edu.np.mad.inkwell;

import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;

public class Folder {
    public String title;

    public String body;

    public int id;

    public String type;

    public CollectionReference colRef;

    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public int getId() { return this.id; }

    public String getType() { return this.type; }

    public CollectionReference getColRef() { return this.colRef; }

    public Folder(String title, String body, int id, String type, CollectionReference colRef) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.type = type;
        this.colRef = colRef;
    }

}
