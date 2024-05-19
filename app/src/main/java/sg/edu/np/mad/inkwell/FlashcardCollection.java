package sg.edu.np.mad.inkwell;

public class FlashcardCollection {
    // FlashcardCollection class attributes
    public String title;

    public int id;

    public int flashcardCount;

    public int correct;

    // FlashcardCollection class get methods
    public String getTitle() { return this.title; }

    public int getId() { return this.id; }

    public int getFlashcardCount() {return this.flashcardCount; }

    public int getCorrect() { return this.correct; }

    // FlashcardCollection class set methods
    public void setTitle(String title) {
        this.title = title;
    }

    // FlashcardCollection class constructor
    public FlashcardCollection(String title, int id, int flashcardCount, int correct) {
        this.title = title;
        this.id = id;
        this.flashcardCount = flashcardCount;
        this.correct = correct;
    }
}
