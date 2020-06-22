package com.pluslatestmemes.database;

/**
 * Created by mwarachael on 6/21/2019.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "UserManager.db";

    // User table name
    private static final String TABLE_USER = "user";
    //Loan Table
    private static final String TABLE_LOAN = "loan";


    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_ID_NUMBER = "user_id_fb";

    // drop table sql query
    private String DROP_LOAN_TABLE = "DROP TABLE IF EXISTS " + TABLE_LOAN;



    // create table sql query
    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("+ COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_ID_NUMBER + " TEXT"+");";

    // drop table sql query
    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;

    /**
     * Constructor
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);
        db.execSQL(DROP_LOAN_TABLE);

        // Create tables again
        onCreate(db);

    }

    /**
     * This method is to create user record
     *
     * @param user
     */
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_NUMBER, user.getUser_id());

        // Inserting Row
        db.insert(TABLE_USER, null, values);
        db.close();
    }
    /**
     * This method is to create user record
     *
     * @param loanAmount
     */

    /**
     * This method is to fetch all user and return the list of user records
     *
     * @return list
     */
    public List<User> getAllUser() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,COLUMN_USER_ID_NUMBER
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID_NUMBER + " ASC";
        List<User> userList = new ArrayList<User>();

        SQLiteDatabase db = this.getReadableDatabase();

        // query the user table
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id,user_name,user_email,user_password FROM user ORDER BY user_name;
         */
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order


        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))));
                user.setUser_id(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID_NUMBER)));
                // Adding user record to list
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // return user list
        return userList;
    }

    /**
     * This method to update user record
     *
     * @param user
     */
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_NUMBER, user.getUser_id());

        // updating row
        db.update(TABLE_USER, values, COLUMN_USER_ID_NUMBER + " = ?",
                new String[]{String.valueOf(user.getUser_id())});
        db.close();
    }

    /**
     * This method is to delete user record
     *
     * @param user
     */
    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete user record by id
        db.delete(TABLE_USER, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    /**
     * This method to check user exist or not
     *
     * @param idnumber
     * @return true/false
     */
    public String getUsernameF(String idnumber) throws SQLException {
        String username = "";

        String selection = COLUMN_USER_ID_NUMBER + " = ?";

        String[] selectionArgs = {idnumber};

        Cursor cursor = this.getReadableDatabase().query(
                TABLE_USER, new String[] { COLUMN_USER_ID_NUMBER },
                selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                username = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return username;
    }


    /**
     * This method to check user exist or not
     *
     * @param idnumber
     * @return true/false
     */
    public String[] getProfile(String idnumber) throws SQLException {
        String[] profile = {
                null,null
        };

        String selection = COLUMN_USER_ID_NUMBER + " = ?";

        String[] selectionArgs = {idnumber};

        Cursor cursor = this.getReadableDatabase().query(
                TABLE_USER, new String[] { COLUMN_USER_ID_NUMBER },
                selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {

            } while (cursor.moveToNext());
        }
        cursor.close();

        return profile;
    }

    /**
     * This method to check user exist or not
     *
     * @param idnumber
     * @return true/false
     */
    public boolean checkUser(String idnumber) {

        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();

        // selection criteria
        String selection = COLUMN_USER_ID_NUMBER + " = ?";

        // selection argument
        String[] selectionArgs = {idnumber};

        // query user table with condition
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com';
         */

        Cursor cursor = db.query(TABLE_USER,columns, selection,selectionArgs,
                null,
                null,
                null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        if (cursorCount > 0) {
            return true;
        }

        return false;
    }

    /**
     * This method to check user exist or not
     *
     *
     * @return true/false
     */


    public Cursor getUsername(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_USER,null);
        return res;
    }

    /**
     * This method to check user exist or not
     *
     * @param idnumber
     * @return true/false
     */
}
