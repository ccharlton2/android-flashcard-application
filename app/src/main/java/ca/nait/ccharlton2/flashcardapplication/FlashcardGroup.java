package ca.nait.ccharlton2.flashcardapplication;

public class FlashcardGroup
{
    private int id;
    private String groupName;
    private String categoryName;
    private String flashcardCount;

    public FlashcardGroup(int id, String groupName, String categoryName, String flashcardCount)
    {
        this.id = id;
        this.groupName = groupName;
        this.categoryName = categoryName;
        this.flashcardCount = flashcardCount;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getFlashcardCount()
    {
        return flashcardCount;
    }

    public void setFlashcardCount(String flashcardCount)
    {
        this.flashcardCount = flashcardCount;
    }
}
