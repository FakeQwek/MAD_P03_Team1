package sg.edu.np.mad.inkwell;

public class Flashcard {
    public String flashcardQuestion;

    public String flashcardAnswer;

    public String getFlashcardQuestion() { return this.flashcardQuestion; }

    public String getFlashcardAnswer() { return this.flashcardAnswer; }

    public void setFlashcardQuestion(String flashcardQuestion) {
        this.flashcardQuestion = flashcardQuestion;
    }

    public void setFlashcardAnswer(String flashcardAnswer) {
        this.flashcardAnswer = flashcardAnswer;
    }

    public Flashcard(String flashcardQuestion, String flashcardAnswer) {
        this.flashcardQuestion = flashcardQuestion;
        this.flashcardAnswer = flashcardAnswer;
    }
}
