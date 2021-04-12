package ca.nait.ccharlton2.flashcardapplication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;

public class CustomFlashcardRecyclerViewAdapter extends RecyclerView.Adapter<FlashcardViewHolder> {
    private Context context;
    private ArrayList<Flashcard> listFlashcards;
    private ArrayList<Flashcard> mArrayList;
    private String groupId;
    static SQLiteDatabase database;
    static DBManager manager;

    CustomFlashcardRecyclerViewAdapter(Context context, ArrayList<Flashcard> listFlashcards, String groupId) {
        this.groupId = groupId;
        this.context = context;
        this.listFlashcards = listFlashcards;
        this.mArrayList = listFlashcards;
        manager = new DBManager(context);
        database = manager.getReadableDatabase();
    }

    @Override
    public FlashcardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_flashcard_flip_view, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashcardViewHolder holder, int position) {
        final Flashcard flashcards = listFlashcards.get(position);
        holder.id.setText(String.valueOf(flashcards.getId()));
        holder.question.setText("Q: " + flashcards.getQuestion());
        holder.answer.setText("A: " + flashcards.getAnswer());
        holder.hint.setText(flashcards.getHint());

        EasyFlipView flipView = holder.itemView.findViewById(R.id.flashcard_flip_view);

        if (FlashcardActivity.autoFlip != null)
        {
            flipView.setAutoFlipBack(FlashcardActivity.autoFlip);
        }

        if (FlashcardActivity.flipTime != null)
        {
            flipView.setAutoFlipBackTime(Integer.parseInt((FlashcardActivity.flipTime)) * 1000);
        }

        MaterialCardView questionCard = holder.itemView.findViewById(R.id.flipview_question);
        ImageView hintImageView = questionCard.findViewById(R.id.flipview_hint_image_view);
        ImageView deleteImageView = questionCard.findViewById(R.id.flipview_delete_image_view);
        ImageView editImageView = questionCard.findViewById(R.id.flipview_edit_image_view);
        String hint = ((TextView)questionCard.findViewById(R.id.flipview_hint_text_view)).getText().toString();
        String flashcardId = ((TextView)questionCard.findViewById(R.id.flipview_flashcard_id_text_view)).getText().toString();

        if(hint != null && !hint.trim().isEmpty()) {
            hintImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(view.getContext(), hint, Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            hintImageView.setVisibility(View.INVISIBLE);
        }

        editImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Edit Flashcard");
                View mainView = view.findViewById(R.id.flashcard_activity_main_view);

                View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.flashcard_add_flashcard_alert_dialog, (ViewGroup) mainView, false);
                final EditText question = (EditText) viewInflated.findViewById(R.id.flashcard_question_input);
                final EditText answer = (EditText) viewInflated.findViewById(R.id.flashcard_answer_input);
                final EditText hint = (EditText) viewInflated.findViewById(R.id.flashcard_hint_input);

                question.setText(flashcards.getQuestion());
                answer.setText(flashcards.getAnswer());
                hint.setText(flashcards.getHint());

                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ContentValues values = new ContentValues();
                        values.put(DBManager.C_CHILD_FLASHCARD_GROUP_ID, groupId);
                        values.put(DBManager.C_FLASHCARD_QUESTION, question.getText().toString());
                        values.put(DBManager.C_FLASHCARD_ANSWER, answer.getText().toString());
                        values.put(DBManager.C_FLASHCARD_HINT, hint.getText().toString());

                        try {
                            database = manager.getWritableDatabase();
                            database.update(DBManager.TABLE_FLASHCARDS, values, "_id=" + flashcards.getId(), null);
                            ((FlashcardActivity)view.getContext()).refreshFlashcardView();
                            Toast.makeText(view.getContext(), "Flashcard updated!", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Toast.makeText(view.getContext(), "Error: " + ex, Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        deleteImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try {
                    database = manager.getWritableDatabase();
                    database.delete(DBManager.TABLE_FLASHCARDS, "flashcard_group_id" + "=" + groupId + " AND _id = " + flashcardId, null);
                    Toast.makeText(view.getContext(), "Flashcard deleted", Toast.LENGTH_LONG).show();
                    ((FlashcardActivity)view.getContext()).refreshFlashcardView();
                } catch (Exception ex) {
                    Toast.makeText(view.getContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFlashcards.size();
    }
}
