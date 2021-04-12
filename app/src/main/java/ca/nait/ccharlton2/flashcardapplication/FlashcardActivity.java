package ca.nait.ccharlton2.flashcardapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class FlashcardActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    SharedPreferences prefs;
    View mainView;

    static Boolean autoFlip;
    static String flipTime;
    static SQLiteDatabase database;
    static DBManager manager;

    /*
    * OnCreate()
    *
    * What does it do?
    *
    * Sets values needed for activity functionality
    *
    * Instantiates activity views and listeners
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Bundle extras = getIntent().getExtras();
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // auto-flip
        String autoFlipKey = getResources().getString(R.string.preference_auto_flip_key);
        autoFlip = prefs.getBoolean(autoFlipKey, true);

        // flip time
        String flipTimeKey = getResources().getString(R.string.preference_flip_time_key);
        flipTime = prefs.getString(flipTimeKey, "3");

        setContentView(R.layout.activity_flashcard);
        mainView = findViewById(R.id.flashcard_activity_main_view);
        manager = new DBManager(this);

        // RecyclerView start
        RecyclerView flashcardView = findViewById(R.id.flashcard_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        CustomFlashcardRecyclerViewAdapter mAdapter;
        flashcardView.setLayoutManager(linearLayoutManager);
        flashcardView.setHasFixedSize(false);
        ImageView leftScrollImageView = mainView.findViewById(R.id.flashcard_recycler_view_move_left_image_view);
        ImageView rightScrollImageView = mainView.findViewById(R.id.flashcard_recycler_view_move_right_image_view);

        ArrayList<Flashcard> allFlashcards = manager.listFlashcards(extras.getString("FLASHCARD_GROUP_ID"));
        if (allFlashcards.size() > 0) {
            mAdapter = new CustomFlashcardRecyclerViewAdapter(this, allFlashcards, extras.getString("FLASHCARD_GROUP_ID"));
            flashcardView.setVisibility(View.VISIBLE);
            flashcardView.setAdapter(mAdapter);
            leftScrollImageView.setVisibility(View.VISIBLE);
            rightScrollImageView.setVisibility(View.VISIBLE);
        } else {
            flashcardView.setVisibility(View.GONE);
            leftScrollImageView.setVisibility(View.GONE);
            rightScrollImageView.setVisibility(View.GONE);
        }
        // RecyclerView end

        // Snap helper makes it so only one item appears at a time
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(flashcardView);

        // Scroll ImageView start
        leftScrollImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (linearLayoutManager.findFirstVisibleItemPosition() > 0) {
                    flashcardView.smoothScrollToPosition(linearLayoutManager.findFirstVisibleItemPosition() - 1);
                } else {
                    flashcardView.smoothScrollToPosition(0);
                }
            }
        });

        rightScrollImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flashcardView.smoothScrollToPosition(linearLayoutManager.findLastVisibleItemPosition() + 1);
            }
        });
        // Scroll ImageView end

        // FloatingActionButton start
        FloatingActionButton fabAddFlashcard = (FloatingActionButton) findViewById(R.id.activity_flashcard_floating_action_button_add_flashcard);
        FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.activity_flashcard_floating_action_button_refresh);
        FloatingActionButton fabShuffle = (FloatingActionButton) findViewById(R.id.activity_flashcard_floating_action_button_shuffle);

        fabAddFlashcard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<String> categories = new ArrayList<String>();

                AlertDialog.Builder builder = new AlertDialog.Builder(FlashcardActivity.this);
                builder.setTitle("New Flashcard");

                View viewInflated = LayoutInflater.from(FlashcardActivity.this).inflate(R.layout.flashcard_add_flashcard_alert_dialog, (ViewGroup) mainView, false);
                final EditText question = (EditText) viewInflated.findViewById(R.id.flashcard_question_input);
                final EditText answer = (EditText) viewInflated.findViewById(R.id.flashcard_answer_input);
                final EditText hint = (EditText) viewInflated.findViewById(R.id.flashcard_hint_input);

                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        boolean isValid = false;

                        if (question.getText().toString() != null && !question.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(mainView.getContext(), "Question cannot be blank", Toast.LENGTH_LONG).show();
                            question.setError("Question cannot be blank");
                        }

                        if (answer.getText().toString() != null && !answer.getText().toString().trim().isEmpty()) {
                            isValid = true;
                        } else {
                            isValid = false;
                            Toast.makeText(mainView.getContext(), "Answer cannot be blank", Toast.LENGTH_LONG).show();
                            answer.setError("Answer name cannot be blank");
                        }

                        if (isValid)
                        {
                            ContentValues values = new ContentValues();
                            values.put(DBManager.C_CHILD_FLASHCARD_GROUP_ID, extras.getString("FLASHCARD_GROUP_ID"));
                            values.put(DBManager.C_FLASHCARD_QUESTION, question.getText().toString());
                            values.put(DBManager.C_FLASHCARD_ANSWER, answer.getText().toString());
                            values.put(DBManager.C_FLASHCARD_HINT, hint.getText().toString());

                            try {
                                database = manager.getWritableDatabase();
                                database.insertOrThrow(DBManager.TABLE_FLASHCARDS, null, values);

                            } catch (Exception ex) {
                                Toast.makeText(mainView.getContext(), "Error: " + ex, Toast.LENGTH_LONG).show();
                            }
                            refreshFlashcardView();
                        }
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

                builder.show();
            }
        });

        if (allFlashcards.size() > 0)
        {
            fabRefresh.setVisibility(View.VISIBLE);
            fabRefresh.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    linearLayoutManager.scrollToPosition(0);
                }
            });
        }
        else
        {
            fabRefresh.setVisibility(View.INVISIBLE);
        }

        if (allFlashcards.size() > 1)
        {
            fabShuffle.setVisibility(View.VISIBLE);
            fabShuffle.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shuffleFlashcards(extras.getString("FLASHCARD_GROUP_ID"));
                }
            });
        }
        else
        {
            fabShuffle.setVisibility(View.INVISIBLE);
        }

        // FloatingActionButton end
    }

    /*
    * onCreateOptionsMenu()
    *
    * What does it do?
    *
    * Inflates activity menu
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_flashcard, menu);
        return true;
    }

    /*
    * onOptionsItemSelected()
    *
    * What does it do?
    *
    * When a menu item is selected this method is called
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_return_home: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    /*
    * shuffleFlashcards()
    *
    * What does it do?
    *
    * Rebinds the RecyclerView with a randomly shuffled ArrayList<Flashcards>
    *
    * Necessary listeners are bound again
    * */
    public void shuffleFlashcards(String groupId)
    {
        // RecyclerView start
        RecyclerView flashcardView = findViewById(R.id.flashcard_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        CustomFlashcardRecyclerViewAdapter mAdapter;
        flashcardView.setLayoutManager(linearLayoutManager);
        flashcardView.setHasFixedSize(false);
        ImageView leftScrollImageView = mainView.findViewById(R.id.flashcard_recycler_view_move_left_image_view);
        ImageView rightScrollImageView = mainView.findViewById(R.id.flashcard_recycler_view_move_right_image_view);

        ArrayList<Flashcard> allFlashcards = manager.listFlashcards(groupId);

        // shuffle the list
        Collections.shuffle(allFlashcards);

        if (allFlashcards.size() > 0) {
            mAdapter = new CustomFlashcardRecyclerViewAdapter(this, allFlashcards, groupId);
            flashcardView.setVisibility(View.VISIBLE);
            flashcardView.setAdapter(mAdapter);
            leftScrollImageView.setVisibility(View.VISIBLE);
            rightScrollImageView.setVisibility(View.VISIBLE);
        } else {
            flashcardView.setVisibility(View.GONE);
            leftScrollImageView.setVisibility(View.GONE);
            rightScrollImageView.setVisibility(View.GONE);
        }

        FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.activity_flashcard_floating_action_button_refresh);

        fabRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                linearLayoutManager.scrollToPosition(0);
            }
        });

        leftScrollImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (linearLayoutManager.findFirstVisibleItemPosition() > 0) {
                    flashcardView.smoothScrollToPosition(linearLayoutManager.findFirstVisibleItemPosition() - 1);
                } else {
                    flashcardView.smoothScrollToPosition(0);
                }
            }
        });

        rightScrollImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flashcardView.smoothScrollToPosition(linearLayoutManager.findLastVisibleItemPosition() + 1);
            }
        });
        // RecyclerView end
    }

    /*
    * refreshFlashcardView()
    *
    * What does it do?
    *
    * Kills the current activity instance and creates a new FlashcardActivity instance
    * */
    public void refreshFlashcardView()
    {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    /*
    * onSharedPreferenceChanged()
    *
    * What does it do?
    *
    * Listens for preference changes and assigns new values to preference variables
    * */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        // auto-flip
        String autoFlipKey = getResources().getString(R.string.preference_auto_flip_key);
        autoFlip = prefs.getBoolean(autoFlipKey, true);

        // flip time
        String flipTimeKey = getResources().getString(R.string.preference_flip_time_key);
        flipTime = prefs.getString(flipTimeKey, "3");
    }
}
