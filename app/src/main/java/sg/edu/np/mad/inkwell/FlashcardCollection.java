package sg.edu.np.mad.inkwell;

public class FlashcardCollection {
    public String title;

    public int id;

    public int flashcardCount;

    public int correct;

    public String getTitle() { return this.title; }

    public int getId() { return this.id; }

    public int getFlashcardCount() {return this.flashcardCount; }

    public int getCorrect() { return this.correct; }

    public void setTitle(String title) {
        this.title = title;
    }

    public FlashcardCollection(String title, int id, int flashcardCount, int correct) {
        this.title = title;
        this.id = id;
        this.flashcardCount = flashcardCount;
        this.correct = correct;
    }
}
