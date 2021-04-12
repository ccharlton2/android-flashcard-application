package ca.nait.ccharlton2.flashcardapplication;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FlashcardGroupViewHolder extends RecyclerView.ViewHolder
{
    public TextView groupName;
    public TextView groupCategoryName;
    public TextView groupGroupId;
    public TextView groupFlashcardCount;

    public FlashcardGroupViewHolder(@NonNull View layoutView)
    {
        super(layoutView);
        groupName = layoutView.findViewById(R.id.flashcard_group_group_name);
        groupCategoryName = layoutView.findViewById(R.id.flashcard_group_category_name);
        groupGroupId = layoutView.findViewById(R.id.flashcard_group_group_id);
        groupFlashcardCount = layoutView.findViewById(R.id.flashcard_group_flashcard_count);
    }
}
