package sg.edu.np.mad.inkwell;

public class FlashcardCollection {
    public String title;

    public int id;

    public int flashcardCount;

    public String getTitle() { return this.title; }

    public int getFlashcardCount() {return this.flashcardCount; }

    public FlashcardCollection(String title, int id, int flashcardCount) {
        this.title = title;
        this.id = id;
        this.flashcardCount = flashcardCount;
    }
}
