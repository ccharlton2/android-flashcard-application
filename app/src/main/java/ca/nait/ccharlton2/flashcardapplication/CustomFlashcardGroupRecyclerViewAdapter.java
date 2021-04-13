package ca.nait.ccharlton2.flashcardapplication;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CustomFlashcardGroupRecyclerViewAdapter extends RecyclerView.Adapter<FlashcardGroupViewHolder> implements Filterable
{
    private Context context;
    private ArrayList<FlashcardGroup> listGroups;
    private ArrayList<FlashcardGroup> mArrayList;
    static SQLiteDatabase database;
    static DBManager manager;
    MaterialCardView flashcardGroupCardView;

    CustomFlashcardGroupRecyclerViewAdapter(Context context, ArrayList<FlashcardGroup> listGroups)
    {
        this.context = context;
        this.listGroups = listGroups;
        this.mArrayList = listGroups;
        manager = new DBManager(context);
        database = manager.getReadableDatabase();
    }

    @Override
    public FlashcardGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_group_card, parent, false);
        return new FlashcardGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashcardGroupViewHolder holder, int position)
    {
        final FlashcardGroup groups = listGroups.get(position);
        holder.groupName.setText(groups.getGroupName());
        holder.groupCategoryName.setText(groups.getCategoryName());
        holder.groupGroupId.setText(String.valueOf(groups.getId()));
        holder.groupFlashcardCount.setText(String.valueOf(groups.getFlashcardCount()).equals("1") ? String.valueOf(groups.getFlashcardCount()) + " Card" : String.valueOf(groups.getFlashcardCount()) + " Cards");

        flashcardGroupCardView = holder.itemView.findViewById(R.id.flashcard);
        FloatingActionButton fab = (FloatingActionButton) ((Activity) context).findViewById(R.id.floating_action_button);
        FloatingActionButton fabDelete = (FloatingActionButton) ((Activity) context).findViewById(R.id.floating_action_button_delete);
        FloatingActionButton fabDeselect = (FloatingActionButton) ((Activity) context).findViewById(R.id.floating_action_button_deselect_all);

        ImageView editGroupImageView = flashcardGroupCardView.findViewById(R.id.flashcard_group_edit_group_image_view);

        // Edit group start
        editGroupImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<String> categories = new ArrayList<String>();

                database = manager.getReadableDatabase();
                Cursor cursor = database.query(DBManager.TABLE_CATEGORIES, null, null, null, null, null, null);

                while (cursor.moveToNext()) {
                    String tempCategory = cursor.getString(cursor.getColumnIndex(DBManager.C_CATEGORY_NAME));
                    categories.add(tempCategory);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Edit Group");

                View mainView = view.findViewById(R.id.main_view);
                View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.flashcard_add_flashcard_group_alert_dialog, (ViewGroup) mainView, false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.group_name_input);
                final AutoCompleteTextView spinner = (AutoCompleteTextView) viewInflated.findViewById(R.id.group_category_text_view);

                RelativeLayout parentLayout = (RelativeLayout) view.getParent();
                String groupName = ((TextView) parentLayout.findViewById(R.id.flashcard_group_group_name)).getText().toString();
                String categoryName = ((TextView) parentLayout.findViewById(R.id.flashcard_group_category_name)).getText().toString();
                String groupId = ((TextView) parentLayout.findViewById(R.id.flashcard_group_group_id)).getText().toString();
                input.setText(groupName);
                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ArrayList<String> categoryIds = new ArrayList<String>();
                        String groupName = input.getText().toString();
                        String category = spinner.getText().toString();
                        boolean isValid = false;

                        if (input.getText().toString() != null && !input.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(view.getContext(), "Group name cannot be blank", Toast.LENGTH_LONG).show();
                            input.setError("Group name cannot be blank");
                        }

                        if (spinner.getText().toString() != null && !spinner.getText().toString().trim().isEmpty()) {
                        } else {
                            isValid = false;
                            spinner.setError("Category cannot be blank");
                        }

                        if (isValid) {
                            dialog.dismiss();
                            Cursor cursor = database.query(DBManager.TABLE_CATEGORIES, new String[]{"_id"},
                                    "category_name like " + "'%" + category + "%'", null, null, null, null);

                            while (cursor.moveToNext()) {
                                String tempCategory = cursor.getString(cursor.getColumnIndex(DBManager.C_ID));
                                categoryIds.add(tempCategory);
                            }

                            ContentValues values = new ContentValues();
                            values.put(DBManager.C_CHILD_CATEGORY_ID, categoryIds.get(0));
                            values.put(DBManager.C_CARD_TITLE, groupName);

                            try {
                                database.update(DBManager.TABLE_FLASHCARD_GROUPS, values, "_id=" + groupId, null);
                                ((MainActivity) view.getContext()).refreshActivityView();
                            } catch (Exception ex) {
                                Toast.makeText(view.getContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(
                                builder.getContext(),
                                R.layout.dropdown_menu_popup_item,
                                categories);

                spinner.setAdapter(adapter);
                spinner.setText(categoryName, false);

                builder.show();
            }
        });
        // Edit group end

        // Group flashcardGroupCardView start
        flashcardGroupCardView.setOnLongClickListener(
                new View.OnLongClickListener()
                {
                    public boolean onLongClick(View view)
                    {
                        if (((MaterialCardView) view).isChecked()) {
                            // uncheck
                            ((MaterialCardView) view).setChecked(!((MaterialCardView) view).isChecked());
                            view.findViewById(R.id.flashcard_group_edit_group_image_view).setVisibility(View.INVISIBLE);

                            MainActivity.deleteGroupIds.remove(((TextView) view.findViewById(R.id.flashcard_group_group_id)).getText().toString());

                            if (MainActivity.deleteGroupIds.size() > 0) {
                                if (MainActivity.deleteItemCount < 0) {
                                    MainActivity.deleteItemCount = 0;
                                } else {
                                    MainActivity.deleteItemCount--;
                                }
                            } else {
                                fab.setVisibility(View.VISIBLE);
                                fabDelete.setVisibility(View.INVISIBLE);
                                fabDeselect.setVisibility(View.INVISIBLE);
                            }

                            return true;
                        } else {
                            // check
                            ((MaterialCardView) view).setChecked(!((MaterialCardView) view).isChecked());
                            view.findViewById(R.id.flashcard_group_edit_group_image_view).setVisibility(View.VISIBLE);
                            MainActivity.deleteGroupIds.add(((TextView) view.findViewById(R.id.flashcard_group_group_id)).getText().toString());
                            MainActivity.deleteItemCount++;
                            fab.setVisibility(View.INVISIBLE);
                            fabDelete.setVisibility((View.VISIBLE));
                            fabDeselect.setVisibility((View.VISIBLE));
                            return false;
                        }
                    }
                }
        );

        flashcardGroupCardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!((MaterialCardView) view).isChecked()) {
                    String flashcardGroupId = ((TextView) view.findViewById(R.id.flashcard_group_group_id)).getText().toString();
                    Intent intent = new Intent(view.getContext(), FlashcardActivity.class);
                    intent.putExtra("FLASHCARD_GROUP_ID", flashcardGroupId);
                    view.getContext().startActivity(intent);
                }
            }
        });
        // Group flashcardGroupCardView end
    }

    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    listGroups = mArrayList;
                } else {
                    ArrayList<FlashcardGroup> filteredList = new ArrayList<>();
                    for (FlashcardGroup groups : mArrayList) {
                        if (groups.getCategoryName().contains(charString)) {
                            filteredList.add(groups);
                        }
                    }
                    listGroups = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listGroups;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                listGroups = (ArrayList<FlashcardGroup>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount()
    {
        return listGroups.size();
    }
}
