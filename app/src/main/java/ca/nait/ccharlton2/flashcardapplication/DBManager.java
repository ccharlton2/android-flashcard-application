package ca.nait.ccharlton2.flashcardapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper
{
    View mainView;
    static final String DB_NAME = "FlashcardApplication.db";
    static final int DB_VERSION = 5;

    // Tables
    static final String TABLE_FLASHCARD_GROUPS = "FlashcardGroups";
    static final String TABLE_FLASHCARDS = "Flashcards";
    static final String TABLE_CATEGORIES = "Categories";

    // BaseColumn
    static final String C_ID = BaseColumns._ID;

    // Categories fields
    static final String C_CATEGORY_NAME = "category_name";

    // FlashcardGroups fields
    static final String C_CHILD_CATEGORY_ID = "category_id";
    static final String C_CARD_TITLE = "card_title";

    // Flashcards fields
    static final String C_CHILD_FLASHCARD_GROUP_ID = "flashcard_group_id";
    static final String C_FLASHCARD_QUESTION = "flashcard_question";
    static final String C_FLASHCARD_ANSWER = "flashcard_answer";
    static final String C_FLASHCARD_HINT = "flashcard_hint";

    public DBManager (Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        // Category
        String sql = String.format("create table %s (%s integer primary key autoincrement, %s text not null unique) ", TABLE_CATEGORIES, C_ID, C_CATEGORY_NAME);
        database.execSQL(sql);

        // FlashcardCard
        sql = String.format("create table %s (%s integer primary key autoincrement, %s integer, %s text) ", TABLE_FLASHCARD_GROUPS, C_ID, C_CHILD_CATEGORY_ID, C_CARD_TITLE);
        database.execSQL(sql);

        // Flashcard
        sql = String.format("create table %s (%s integer primary key autoincrement, %s integer, %s text, %s text, %s text) ", TABLE_FLASHCARDS, C_ID, C_CHILD_FLASHCARD_GROUP_ID, C_FLASHCARD_QUESTION, C_FLASHCARD_HINT, C_FLASHCARD_ANSWER);
        database.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        database.execSQL("drop table if exists " + TABLE_FLASHCARDS);
        database.execSQL("drop table if exists " + TABLE_FLASHCARD_GROUPS);
        database.execSQL("drop table if exists " + TABLE_CATEGORIES);
        onCreate(database);
    }

    ArrayList<FlashcardGroup> listGroups() {
            ArrayList<FlashcardGroup> storeContacts = new ArrayList<>();
        try {
            String MY_QUERY = "SELECT flshgrp._id, flshgrp.card_title, catgr.category_name, (SELECT COUNT(_id) FROM Flashcards WHERE flashcard_group_id=flshgrp._id) FROM " + TABLE_FLASHCARD_GROUPS + " flshgrp JOIN " + TABLE_CATEGORIES + " catgr ON flshgrp.category_id=catgr._id ORDER BY catgr._id";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(MY_QUERY, null);
            if (cursor.moveToFirst()) {
                do {
                    int id = Integer.parseInt(cursor.getString(0));
                    String groupName = cursor.getString(1);
                    String categoryName = cursor.getString(2);
                    String flashcardCount = cursor.getString(3);

                    storeContacts.add(new FlashcardGroup(id, groupName, categoryName, flashcardCount));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "YEET");
        }
        return storeContacts;
    }

    ArrayList<Flashcard> listFlashcards(String groupId) {
        ArrayList<Flashcard> flashcards = new ArrayList<>();
        try {
            String sql = "SELECT _id, flashcard_question, flashcard_answer, flashcard_hint FROM " + TABLE_FLASHCARDS + " WHERE flashcard_group_id = " + groupId;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    int id = Integer.parseInt(cursor.getString(0));
                    String answer = cursor.getString(1);
                    String question = cursor.getString(2);
                    String hint = cursor.getString(3);

                    flashcards.add(new Flashcard(id, answer, question, hint));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "YEET");
        }
        return flashcards;
    }
}
