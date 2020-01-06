package com.example.hellomemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.hellomemo.DataBase.DBAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //퍼미션 코드
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    //데이터 베이스 어답터
    static public DBAdapter mDbHelper;

    //리스트뷰 생성, 어댑터
    private ListView listview ;
    private MyAdapter adapter;

    //메뉴 선택 코드  //옵션/정렬
    private static final int MENU_TIME_SORT=0;   //시간순으로 정렬
    private static final int MENU_CRT_ALL=1;    //생성순으로 정렬
    private static final int MENU_TLT_SORT=2;    //제목순으로 정렬
    private static final int MENU_BDY_SORT=3;    //내용순으로 정렬

    public static int sort_option = MENU_CRT_ALL;
    private static final String KEY_MY_PREFERENCE = "option";
    private static final String KEY_OPTION_SORT = "sort";

    //콘텍스트 메뉴 코드
    private static final int DELETE_ID = Menu.FIRST;
    private static final int SELECT_ID = 2;
    private static final int SHARE_ID = 3;
    private static final int INFOR_ID = 4;

    //타이틀바 서치뷰
    private MaterialSearchView searchView;
    public static boolean bSearchMode = false;

    //권한쪽
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**** 안드로이드 권한 ****/
        intent = new Intent(getApplicationContext(),onTopService.class);
        if( Build.VERSION.SDK_INT>=23) permissionCheck();

        /**** DB 오픈 ****/
        mDbHelper = new DBAdapter (this);
        mDbHelper.open();

        /**** 리스트뷰 어댑터 정렬옵션 ****/
        adapter = new MyAdapter() ;
        listview = (ListView) findViewById(R.id.Notelist);
        setOptionSort(getOptionSort()); //정렬 저장되어있는거 옵션설정 불러오기
        showAllMemos();

        /**** 메모 추가 ****/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMemo();
            }
        });

        /**** 컨텍스트 메뉴 사용 ****/
        registerForContextMenu(listview);

        /**** 클릭시 메모 수정 ****/
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                //수정하기
                Intent i = new Intent(MainActivity.this, MemoAddActivity.class);
                i.putExtra(DBAdapter.KEY_ROWID, id);

                startActivityForResult(i, MEMOMODE.ACTIVITY_EDIT);
            }
        }) ;

        /**** 타이틀바 검색 ****/
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        //searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if( newText.equals(""))
                    showAllMemos();

                CharSequence charQuery = newText;
                if (charQuery != null && TextUtils.getTrimmedLength(charQuery) > 0) {
                    doSearch(newText);
                    MainActivity.bSearchMode = true;
                }
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                showAllMemos();
            }

            @Override
            public void onSearchViewClosed() {
            }
        });
    }


    /*********************************************
     * 메뉴부분
     ********************************************** */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                sortDialog();
                break;
            case R.id.menu_option:
                break;
            default:
               return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /*********************************************
     * 정렬 다이얼로그
     ********************************************** */
    private void sortDialog(){
        final String[] sort = getResources().getStringArray(R.array.title_bar_menu_sort);
        AlertDialog.Builder sortDialog =
                new AlertDialog.Builder(MainActivity.this);
        sortDialog.setTitle(R.string.title_bar_menu_sort);

        sortDialog.setItems(sort, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case MENU_TIME_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_time, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_TIME_SORT);
                        showAllMemos();
                        break;
                    case MENU_CRT_ALL:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_create, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_CRT_ALL);
                        showAllMemos();
                        break;
                    case MENU_TLT_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_title, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_TLT_SORT);
                        showAllMemos();
                        break;
                    case MENU_BDY_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_body, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_BDY_SORT);
                        showAllMemos();
                        break;
                }
            }});

        sortDialog.setNegativeButton(R.string.menu_cancel, null);

        sortDialog.show();
    }


    /*********************************************
     * 정렬 옵션 세팅
     ********************************************** */
    public void setOptionSort(int c) {
        sort_option = c;
        optionFileWrite(KEY_OPTION_SORT, sort_option);
    }
    public int getOptionSort() {
        return optionFileRead(KEY_OPTION_SORT);
    }

    /*********************************************
     * 옵션 저장
     ********************************************** */
    private void optionFileWrite(String kindOption, int value) {
        //파일저장
        SharedPreferences prefs = getSharedPreferences(kindOption, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_MY_PREFERENCE, value);
        editor.commit();
    }

    /*********************************************
     * 옵션 로드
     ********************************************** */
    public int optionFileRead(String kindOption) {
        //파일읽어오기
        SharedPreferences prefs = getSharedPreferences(kindOption, MODE_PRIVATE);
        int value = prefs.getInt(KEY_MY_PREFERENCE, 0);
        return value;
    }


    /*********************************************
     * 안드로이드 6.0 이후 퍼미션
     ********************************************** */
    @SuppressLint("NewApi")
    public void permissionCheck() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
        else {}
    }

    /*********************************************
     * 메모 생성 인텐트
     ********************************************** */
    private void createMemo() {
        Intent i = new Intent(this, MemoAddActivity.class);
        startActivityForResult(i, MEMOMODE.ACTIVITY_CREATE);
    }

    /*********************************************
    * 모든 메모 보여주기 (List)
    ********************************************** */
    private void showAllMemos() {
        Cursor notesCursor =  getSortOption();
        startManagingCursor(notesCursor);

        String[] from = new String[] { DBAdapter.TITLE , DBAdapter.BODY, DBAdapter.CHANGED_DATE};
        int[] to = new int[] { R.id.textView1,R.id.textbody,R.id.date_row};

        SimpleCursorAdapter memos =
                new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        listview.setAdapter(memos);
    }

    /*********************************************
     * 정렬에 따라 Cursor 반환
     ********************************************** */
    private Cursor getSortOption(){
        switch(sort_option) {
            case MENU_CRT_ALL:
                return mDbHelper.fetchAllMemos(mDbHelper.CHANGED_DATE_VALUE, "");
            case MENU_TIME_SORT:
                return mDbHelper.fetchAllMemos(mDbHelper.CHANGED_DATE_VALUE, mDbHelper.DESC);
            case MENU_TLT_SORT:
                return mDbHelper.fetchAllMemos(mDbHelper.TITLE, mDbHelper.ASC);
            case MENU_BDY_SORT:
                return mDbHelper.fetchAllMemos(mDbHelper.BODY, mDbHelper.ASC);
            default:
                return mDbHelper.fetchAllMemos(mDbHelper.CHANGED_DATE_VALUE, "");
        }
    }


    /*********************************************
     * 컨텍스트 메뉴
     ********************************************** */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //설정시 무엇눌렀는지 팝업메뉴 위에 TITLE띄어줌
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(getDBString(info, DBAdapter.TITLE));
        menu.add(0, DELETE_ID, 0, R.string.contextmenu_Delete);
        menu.add(0, SELECT_ID, 0, R.string.contextmenu_Selecet);
        menu.add(0, SHARE_ID, 0, R.string.contextmenu_Share);
        menu.add(0, INFOR_ID, 0, R.string.contextmenu_Information);
    }

    /*********************************************
     * 유저가 선택한 노트 반환
     ********************************************** */
    private String getDBString(AdapterView.AdapterContextMenuInfo info, String selectKey) {
        //모든 노트 검색해서
        Cursor notesCursor = getSortOption();
        startManagingCursor(notesCursor);
        //포지션에 맞는거 찾으면
        notesCursor.moveToPosition(info.position);

        String labelColumn_body = notesCursor.getString(notesCursor.
                getColumnIndex(selectKey));
        //그거 리턴
        return labelColumn_body;
    }


    /*********************************************
     * 컨텍스트 메뉴 선택시
     ********************************************** */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteMemo(info.id);
                showAllMemos();
                return true;
            case SELECT_ID:
                startService(intent);
                //팝업 부분
                break;
            case SHARE_ID:
                String str = "";
                Intent msg = new Intent(Intent.ACTION_SEND);
                msg.addCategory(Intent.CATEGORY_DEFAULT);
                str = "<--" + getResources().getString(R.string.app_name) + "-->\n"
                        + "[" + getResources().getString(R.string.memo_share_title) +
                        getDBString(info, DBAdapter.TITLE) + "]" + "\n" +
                        getResources().getString(R.string.memo_share_body) + getDBString(info, DBAdapter.BODY)
                        + "\n\n" + "[" + getResources().getString(R.string.memo_share_time) +
                        getDBString(info, DBAdapter.CHANGED_DATE) + "]";
                msg.putExtra(Intent.EXTRA_TEXT, str);
                msg.setType("text/plain");
                startActivity(Intent.createChooser(msg, "공유하기"));
                break;

            case INFOR_ID:
                memoInformationDialog(getDBString(info, DBAdapter.BODY),
                        getDBString(info, DBAdapter.CREATE_DATE));
                break;
        }
        return super.onContextItemSelected(item);
    }


    /*********************************************
     * 메모 정보 다이얼로그 (컨텍스트 메뉴)
     ********************************************** */
    private void memoInformationDialog(String strBody, String strDate) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        dialog.setTitle(R.string.contextmenu_Information);

        dialog.setMessage("길이 : " + Integer.
                valueOf(strBody.length()).toString() +
                "\n생성된 날짜 : " + strDate);
        dialog.show();
    }


    /*********************************************
     * 메모 정보 다이얼로그 (컨텍스트 메뉴)
     ********************************************** */
    private void doSearch(String search) {
        Cursor notesCursor = mDbHelper.searchMemo(search);

        startManagingCursor(notesCursor);

        String[] from = new String[] { DBAdapter.TITLE , DBAdapter.BODY, DBAdapter.CHANGED_DATE};
        int[] to = new int[] { R.id.textView1,R.id.textbody,R.id.date_row};

        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        listview.setAdapter(notes);
    }


    /*********************************************
     * 검색 결과
     ********************************************** */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        showAllMemos();
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*********************************************
     * 검색 상황 뒤로가기
     ********************************************** */
    @Override
    public void onBackPressed() {
        //검색된 상황에서 뒤로가기 누르면
        //앱이 꺼지는게 아니라 데이터 보여주기
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            showAllMemos();
        } else {
            if( MainActivity.bSearchMode) {
                MainActivity.bSearchMode = false;
                showAllMemos();
            } else {
                super.onBackPressed();
            }
        }
    }
}
