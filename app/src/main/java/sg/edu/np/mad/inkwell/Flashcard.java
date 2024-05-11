package sg.edu.np.mad.inkwell;

public class Flashcard {
    public String question;

    public String answer;

    public int id;

    public String getQuestion() { return this.question; }

    public String getAnswer() { return this.answer; }

    public int getId() { return this.id; }

    public void setFlashcardQuestion(String flashcardQuestion) {
        this.question = flashcardQuestion;
    }

    public void setFlashcardAnswer(String flashcardAnswer) {
        this.answer = flashcardAnswer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Flashcard(String flashcardQuestion, String flashcardAnswer, int id) {
        this.question = flashcardQuestion;
        this.answer = flashcardAnswer;
        this.id = id;
    }
}
