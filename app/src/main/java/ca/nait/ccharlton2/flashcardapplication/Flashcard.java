package ca.nait.ccharlton2.flashcardapplication;

public class Flashcard
{
    private int id;
    private String question;
    private String answer;
    private String hint;

    public Flashcard(int id, String question, String answer, String hint)
    {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.hint = hint;
    }

    public Flashcard(String question, String answer, String hint)
    {
        this.question = question;
        this.answer = answer;
        this.hint = hint;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getHint()
    {
        return hint;
    }

    public void setHint(String hint)
    {
        this.hint = hint;
    }
}
