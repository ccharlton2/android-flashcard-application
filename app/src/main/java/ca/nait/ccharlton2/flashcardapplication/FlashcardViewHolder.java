package ca.nait.ccharlton2.flashcardapplication;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FlashcardViewHolder extends RecyclerView.ViewHolder
{
    public TextView id;
    public TextView question;
    public TextView answer;
    public TextView hint;

    public FlashcardViewHolder(@NonNull View layoutView)
    {
        super(layoutView);
        id = layoutView.findViewById(R.id.flipview_flashcard_id_text_view);
        question = layoutView.findViewById(R.id.flipview_question_text_view);
        answer = layoutView.findViewById(R.id.flipview_answer_text_view);
        hint = layoutView.findViewById(R.id.flipview_hint_text_view);
    }
}
