package ca.nait.ccharlton2.flashcardapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    View mainView;
    CharSequence filter;

    static ArrayList<String> deleteGroupIds;
    static int deleteItemCount;
    static SQLiteDatabase database;
    static DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        mainView = findViewById(R.id.main_view);
        manager = new DBManager(this);
        filter = "";
        deleteGroupIds = new ArrayList<String>();
        deleteItemCount = 0;

        refreshActivityView();

        FloatingActionButton fabDelete = (FloatingActionButton) findViewById(R.id.floating_action_button_delete);
        FloatingActionButton fabDeselect = (FloatingActionButton) findViewById(R.id.floating_action_button_deselect_all);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button);
        Chip filterChip = findViewById(R.id.flashcard_group_filter_chip);

        filterChip.setOnCloseIconClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                filter = "";
                refreshActivityView();
            }
        });

        fabDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String[] deleteGroupIdsArray = new String[deleteGroupIds.size()];

                for (int i = 0; i < deleteGroupIds.size(); i++) {
                    deleteGroupIdsArray[i] = deleteGroupIds.get(i);
                }

                String args = TextUtils.join(", ", deleteGroupIds);

                String deleteGroupsSql = String.format("DELETE FROM " + DBManager.TABLE_FLASHCARD_GROUPS + " WHERE _id IN (%s);", args);
                String deleteGroupFlashcardsSql = String.format("DELETE FROM " + DBManager.TABLE_FLASHCARDS + " WHERE flashcard_group_id IN (%s);", args);

                database = manager.getWritableDatabase();
                database.execSQL(deleteGroupsSql);
                database.execSQL(deleteGroupFlashcardsSql);


                fabDelete.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);

                Snackbar.make(mainView, deleteItemCount == 1 ? String.valueOf(deleteItemCount) + " item deleted" : String.valueOf(deleteItemCount) + " items deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                refreshActivityView();
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<String> categories = new ArrayList<String>();

                database = manager.getReadableDatabase();
                Cursor cursor = database.query(DBManager.TABLE_CATEGORIES, null, null, null, null, null, null);

                startManagingCursor(cursor);

                while (cursor.moveToNext()) {
                    String tempCategory = cursor.getString(cursor.getColumnIndex(DBManager.C_CATEGORY_NAME));
                    categories.add(tempCategory);
                }

                stopManagingCursor(cursor);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Group");

                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.flashcard_add_flashcard_group_alert_dialog, (ViewGroup) mainView, false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.group_name_input);
                final AutoCompleteTextView spinner = (AutoCompleteTextView) viewInflated.findViewById(R.id.group_category_text_view);

                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        filter = "";

                        ArrayList<String> categoryIds = new ArrayList<String>();
                        String groupName = input.getText().toString();
                        String category = spinner.getText().toString();

                        boolean isValid = false;

                        if (input.getText().toString() != null && !input.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(mainView.getContext(), "Group name cannot be blank", Toast.LENGTH_LONG).show();
                            input.setError("Group name cannot be blank");
                        }

                        if (spinner.getText().toString() != null && !spinner.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(mainView.getContext(), "Category cannot be blank", Toast.LENGTH_LONG).show();
                            input.setError("Category cannot be blank");
                        }

                        if (isValid) {

                            Cursor cursor = database.query(DBManager.TABLE_CATEGORIES, new String[]{"_id", "category_name"},
                                    "category_name like " + "'%" + category + "%'", null, null, null, null);

                            startManagingCursor(cursor);

                            while (cursor.moveToNext()) {
                                String tempCategory = cursor.getString(cursor.getColumnIndex(DBManager.C_ID));
                                categoryIds.add(tempCategory);
                            }

                            if (categoryIds.size() > 0) {
                                ContentValues values = new ContentValues();
                                values.put(DBManager.C_CHILD_CATEGORY_ID, categoryIds.get(0));
                                values.put(DBManager.C_CARD_TITLE, input.getText().toString());

                                try {
                                    database = manager.getWritableDatabase();
                                    database.insertOrThrow(DBManager.TABLE_FLASHCARD_GROUPS, null, values);
                                    database.close();

                                } catch (Exception ex) {
                                    Toast.makeText(mainView.getContext(), "Error: " + ex, Toast.LENGTH_LONG).show();
                                }
                                refreshActivityView();

                                dialog.dismiss();
                            }

                            stopManagingCursor(cursor);

                            Snackbar.make(mainView, "Group saved!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
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

                AutoCompleteTextView editTextFilledExposedDropdown =
                        mainView.findViewById(R.id.text_1);
                spinner.setAdapter(adapter);

                builder.show();
            }
        });

        fabDeselect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refreshActivityView();
            }
        });
    }

    public void refreshActivityView()
    {
        FloatingActionButton fabDelete = (FloatingActionButton) findViewById(R.id.floating_action_button_delete);
        FloatingActionButton fabDeselect = (FloatingActionButton) findViewById(R.id.floating_action_button_deselect_all);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button);
        Chip filterChip = findViewById(R.id.flashcard_group_filter_chip);
        deleteGroupIds.clear();
        deleteItemCount = 0;

        if (!filter.equals("")) {
            filterChip.setText(filter);
            filterChip.setVisibility(View.VISIBLE);

        } else {
            filterChip.setVisibility(View.GONE);
        }

        RecyclerView contactView = findViewById(R.id.flashcard_group_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        CustomFlashcardGroupRecyclerViewAdapter mAdapter;
        contactView.setLayoutManager(linearLayoutManager);
        contactView.setHasFixedSize(true);
        contactView.setNestedScrollingEnabled(true);

        ArrayList<FlashcardGroup> allGroups = manager.listGroups();
        if (allGroups.size() > 0) {
            mAdapter = new CustomFlashcardGroupRecyclerViewAdapter(this, allGroups);
            mAdapter.getFilter().filter(filter);
            contactView.setVisibility(View.VISIBLE);
            contactView.setAdapter(mAdapter);
        } else {
            contactView.setVisibility(View.GONE);
            Toast.makeText(this, "Start by adding a category", Toast.LENGTH_LONG).show();
        }

        fab.setVisibility(View.VISIBLE);
        fabDelete.setVisibility(View.INVISIBLE);
        fabDeselect.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        deleteGroupIds.clear();
        deleteItemCount = 0;
        refreshActivityView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_add_category: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Category");

                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.flashcard_add_category_alert_dialog, (ViewGroup) mainView, false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.category_name_input);
                builder.setView(viewInflated);

                builder.setPositiveButton("save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ContentValues values = new ContentValues();
                        values.put(DBManager.C_CATEGORY_NAME, input.getText().toString());

                        boolean isValid = false;

                        if (input.getText().toString() != null && !input.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(mainView.getContext(), "Category name cannot be blank", Toast.LENGTH_LONG).show();
                            input.setError("Category name cannot be blank");
                        }

                        if (isValid) {
                            try {
                                database = manager.getWritableDatabase();
                                database.insertOrThrow(DBManager.TABLE_CATEGORIES, null, values);
                                database.close();

                            } catch (Exception ex) {

                            }

                            dialog.dismiss();

                            Snackbar.make(mainView, "Category saved!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
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

                builder.show();
                break;
            }
            case R.id.action_go_to_settings: {
                Intent intent = new Intent(this, PrefsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_filter_groups: {
                ArrayList<String> categories = new ArrayList<String>();

                database = manager.getReadableDatabase();
                Cursor cursor = database.query(DBManager.TABLE_CATEGORIES, new String[]{"category_name"}, "_id IN (SELECT category_id FROM " + DBManager.TABLE_FLASHCARD_GROUPS + ");", null, null, null, null);

                startManagingCursor(cursor);

                while (cursor.moveToNext()) {
                    String tempCategory = cursor.getString(cursor.getColumnIndex(DBManager.C_CATEGORY_NAME));
                    categories.add(tempCategory);
                }

                stopManagingCursor(cursor);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Filter Groups");

                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.flashcard_filter_alert_dialog, (ViewGroup) mainView, false);
                final AutoCompleteTextView spinner = (AutoCompleteTextView) viewInflated.findViewById(R.id.filter_category_auto_complete_view);
                builder.setView(viewInflated);

                builder.setPositiveButton("apply", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        filter = spinner.getText().toString();
                        refreshActivityView();
                        dialog.dismiss();
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

                builder.show();
            }
        }
        return true;
    }

    @Override
    public void onClick(View v)
    {

    }
}