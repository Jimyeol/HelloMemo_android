package com.example.hellomemo.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    /*********************************************
     ------- Attribute
    * KEY_ROWID - Index
    * TITLE - 제목
    * BODY - 내용
    * CHANGED_DATE - 수정된 보여주기 날짜 ex)2016. 9. 5 7:56
    * CHANGED_DATE_VALUE - 수정된 정렬용 날짜 ex)201695756 (정렬용)
    * CREATE_DATE - 생성된 날짜  ex)2016. 9. 5 7:56
    * ********************************************** */
    public static final String KEY_ROWID = "_id";
    public static final String TITLE = "title";
    public static final String CHANGED_DATE = "date_changed";
    public static final String BODY = "body";
    public static final String CHANGED_DATE_VALUE = "time_changed_value";
    public static final String CREATE_DATE = "date_create";


    private static final String TAG = "MemoDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /* DB Name */
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "hello_memos";
    private static final int DATABASE_VERSION = 2;

    public static final String ASC = "ASC";         //오름차순
    public static final String DESC = "DESC";       //내림차순

    /*********************************************
     * DB 생성 Query
     ********************************************** */
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + "("
                    + KEY_ROWID + " integer primary key autoincrement, "
                    + TITLE + " text not null, "
                    + BODY + " text not null, "
                    + CHANGED_DATE + " text not null, "
                    + CHANGED_DATE_VALUE + " long not null, "
                    + CREATE_DATE +  " text not null"  + ");";


    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "업그레이드 " + oldVersion + " to "
                    + newVersion + ", 삭제하고 새롭게");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /*********************************************
     * 메모 생성
     ********************************************** */
    public long createMemo(String title, String body, String date, Long time, String date_create) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(TITLE, title);
        initialValues.put(BODY, body);
        initialValues.put(CHANGED_DATE, date);
        initialValues.put(CHANGED_DATE_VALUE, time);
        initialValues.put(CREATE_DATE, date_create);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /*********************************************
     * 특정 메모만 보여주기
     ********************************************** */
    public Cursor fetchMemo(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                TITLE, BODY,CHANGED_DATE, CHANGED_DATE_VALUE
                                ,CREATE_DATE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /*********************************************
     * 모든 메모 보여주기
     ********************************************** */
    public Cursor fetchAllMemos(String key, String str) {
        //첫번째 인자값은 어떤것을 정렬할것인가
        //두번째 인자값은 내림차순인가 오름차순인가
        //내림차순인데 KEY_TIME값이 제일 최근이 위로감
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, TITLE,
                        BODY, CHANGED_DATE, CHANGED_DATE_VALUE, CREATE_DATE}, null, null, null,
                null, key + " " + str, null);    //ASC
        // Order by (내림차순 정렬기능)
    }

    /*********************************************
     * 메모 수정
     ********************************************** */
    public boolean updateMemo(long rowId, String title, String body,String date,
                              Long time, String date_create) {
        ContentValues args = new ContentValues();
        args.put(TITLE, title);
        args.put(BODY, body);
        args.put(CHANGED_DATE, date);
        args.put(CHANGED_DATE_VALUE, time);
        args.put(CREATE_DATE, date_create);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

}
