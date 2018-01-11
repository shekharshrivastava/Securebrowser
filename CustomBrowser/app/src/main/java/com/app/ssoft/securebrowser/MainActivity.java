package com.app.ssoft.securebrowser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.crashlytics.android.Crashlytics;
import com.felix.bottomnavygation.BottomNav;
import com.felix.bottomnavygation.ItemNav;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener, WebView.FindListener, BottomNav.OnTabSelectedListener, AdapterView.OnItemClickListener {
    private static final int REQ_CODE_SPEECH_INPUT = 3;
    private EditText urlText;
    TabHost host;
    private String sharedUrl;
    private SwipeRefreshLayout swipeContainer;
    private String title;
    TabHost.TabSpec spec;
    private static WebView webView;
    private String url = "";
    private String webUrl;
    private String urlInputText;
    private CheckBox bookmarkCheckbox;
    private BottomSheet bottomSheet;
    //    private BottomNavigationView navigation;
    int flags = 1024;
    private Menu menuItems;
    private int mCurrentSearchIndex = -1;
    private String[] mSearchTerms = new String[]{};
    private LinearLayout urlLinearLayout;
    private LinearLayout searchLinearLayout;
    private EditText searchEditText;
    private DownloadManager dm;
    private String downloadedUrl;
    private Menu bottomNavigationMenu;
    private boolean isPageLoaded = false;
    private MenuItem bottomMenuItem;
    private ArrayList<String> adList;
    private Hashtable hashTable;
    private BottomNav bottomNav;
    private DBHelper dbHelper;
    private GridView quickLinksGV;
    private View gridLinearLayout;


    //    private TextView mTextMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        String[] web = {
                "Google",
                "Facebook",
                "Amazon",
                "Youtube",
                "Cricbuzz",
                "Twitter",

        };
        int[] imageId = {
                R.drawable.google,
                R.drawable.facebook,
                R.drawable.amazon,
                R.drawable.youtube,
                R.drawable.cricbuzz,
                R.drawable.twitter,


        };
//        SQLiteDatabase.loadLibs(this);
        //blocked ads
        adBlockers();
      /*  host = (TabHost) findViewById(tabHost);
        host.setup();*/

        Fabric.with(this, new Crashlytics());
//        exportDatabase(DBHelper.DATABASE_NAME);
        gridLinearLayout = findViewById(R.id.gridLinearLayout);
        quickLinksGV = findViewById(R.id.quickLinksGV);
        CustomQLGridAdapter adapter = new CustomQLGridAdapter(MainActivity.this, web, imageId);
        quickLinksGV.setAdapter(adapter);
        quickLinksGV.setOnItemClickListener(this);


        bottomNav = (BottomNav) findViewById(R.id.bottomNav);
        bottomNav.addItemNav(new ItemNav(this, R.drawable.back, "Back"));
        bottomNav.addItemNav(new ItemNav(this, R.drawable.forward, "Forward"));
        bottomNav.addItemNav(new ItemNav(this, R.drawable.homepage, "Home"));
        bottomNav.addItemNav(new ItemNav(this, R.drawable.find, "Find"));
        bottomNav.addItemNav(new ItemNav(this, R.drawable.interface_rows, "Options"));
        bottomNav.build();

        bottomNav.setTabSelectedListener(this);
        urlLinearLayout = (LinearLayout) findViewById(R.id.urlLinearLayout);
        searchLinearLayout = (LinearLayout) findViewById(R.id.searchLinearLayout);
        searchEditText = (EditText) findViewById(R.id.searchText);
        searchEditText.setSelectAllOnFocus(true);
        bookmarkCheckbox = (CheckBox) findViewById(R.id.bookmarkCheckBox);
        if (getIntent().getData() != null) {
            sharedUrl = getIntent().getData().toString();
            url = sharedUrl;
        }
        urlText = (EditText) findViewById(R.id.urlText);

        urlText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    gridLinearLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    url = urlText.getText().toString();
                    if (!hashTable.containsKey(url)) {
                        if ((url.endsWith(".com") || url.endsWith(".in") || url.endsWith(".org") || url.contains(".gov"))) {
                            if (!url.startsWith("https://")) {
                                webView.loadUrl("https://" + url);

                            }
                        } else {
                            String webSearchText = "https://www.google.com/search?q=" + url;
                            webView.loadUrl(webSearchText);
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchTextOnPage();
                return false;
            }
        });
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
//        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setFindListener(this);

        /*if (url.isEmpty() || url == null) {
            webView.loadUrl(getString(R.string.default_url));
        } else {
            webView.loadUrl(url);
        }*/

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onPageStarted(WebView view, String urlLink, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                bottomNavigationMenu = navigation.getMenu();
//                bottomMenuItem = bottomNavigationMenu.findItem(R.id.navigation_forward);
//                bottomMenuItem.setIcon(R.drawable.close_button);
//                bottomMenuItem.setTitle("Stop");
                isPageLoaded = false;
                bookmarkCheckbox.setChecked(false);
                findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
                title = view.getTitle();
                url = urlLink;
                urlInputText = urlLink;
                urlText.setText(urlLink);
//                urlText.setCompoundDrawables(new BitmapDrawable(view.getFavicon()),null,null,null);
                hideKeyBoard();

                Log.d("WebView", "your current url when webpage loading.." + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                isPageLoaded = true;
//                bottomNavigationMenu = navigation.getMenu();
               /* bottomMenuItem = bottomNavigationMenu.findItem(R.id.navigation_forward);
                bottomMenuItem.setIcon(R.drawable.forward);
                bottomMenuItem.setTitle("Forward");*/
                findViewById(R.id.progressBar1).setVisibility(View.GONE);
                title = view.getTitle();
                try {
                    insertHistoryIntoDB(title, url);
                } catch (Exception SQLException) {

                }
//                swipeContainer.setRefreshing(false);
                Log.d("WebView", "your current url when webpage loading.. finish" + url);
//                nextSearch();
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                System.out.println("when you click on any interlink on webview that time you got url :-" + url);
  /*              for (String adURL : adList) {
                    if (!(adURL.contains(url))) {*/

                if (!hashTable.containsKey("url")) {
                    webView.loadUrl(url);
                } else {
                    Toast.makeText(MainActivity.this, "Adv Blocked", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                Toast.makeText(MainActivity.this, "onReceivedHttpAuthRequest: host => " + host + ", realm => "
                        + realm, Toast.LENGTH_SHORT);
                String[] up = webView.getHttpAuthUsernamePassword(host, realm);

                if (up != null && up.length == 2 && up[0] != null && up[1] != null) {
                    handler.proceed(up[0], up[1]);
                } else {
                    super.onReceivedHttpAuthRequest(webView, handler, host, realm);
                }
            }
        });

        downloadingFile(MainActivity.this);
//        navigation = (BottomNavigationView)findViewById(R.id.navigation);
       /* disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);*/
        this.

                registerForContextMenu(webView);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data != null) {
                    String urlLink = data.getStringExtra("url");
                    webView.loadUrl(urlLink);
                }
                break;
            case 2:
                if (data != null) {
                    String urlLink = data.getStringExtra("url");
                    webView.loadUrl(urlLink);
                }
                break;
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    webView.loadUrl("http://www.google.com/#q=" + result.get(0));
                }
                break;

            }
        }
    }


    @Override
    public void onBackPressed() {
        if (searchLinearLayout.getVisibility() == View.GONE) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (gridLinearLayout.getVisibility() == View.GONE) {
                    urlText.setText("");
                    webView.clearHistory();
                    webView.clearCache(true);
                    webView.setVisibility(View.GONE);
                    gridLinearLayout.setVisibility(View.VISIBLE);


                } else {
                    finish();
                }
            }
        } else {
            searchLinearLayout.setVisibility(View.GONE);
            urlLinearLayout.setVisibility(View.VISIBLE);
            webView.clearMatches();
        }


    }


    /*public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }*/

    private void shareTextUrl() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_TEXT, urlInputText);

        startActivity(Intent.createChooser(share, "Share link!"));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final WebView.HitTestResult result = webView.getHitTestResult();
        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // handle on context menu click
                return true;
            }
        };

        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            menu.setHeaderTitle(result.getExtra());
            menu.add(0, 666, 0, "Save Image").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
//                    downloadExternalFile(MainActivity.this, webView.getUrl(), webView.getSettings().getUserAgentString(), "", mime.getExtensionFromMimeType(extension));
//                    downloadingFile(MainActivity.this);
                    return false;
                }
            });
        }
    }


    private void insertBookmarkIntoDB(String title, String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db = DBHelper.getInstance(this).getWritableDatabase("");
        ContentValues values = new ContentValues();
        values.put(BookmarkEntryFeed.BookmarkEntry.COLUMN_NAME_TITLE, title);
        values.put(BookmarkEntryFeed.BookmarkEntry.COLUMN_URL, url);

        db.insert(BookmarkEntryFeed.BookmarkEntry.TABLE_NAME, null, values);

        /*Cursor cursor = db.rawQuery("SELECT * FROM '" + BookmarkEntryFeed.BookmarkEntry.TABLE_NAME + "';", null);
        Log.d(MainActivity.class.getSimpleName(), "Rows count: " + cursor.getCount());
        cursor.close();*/
        db.close();

        // this will throw net.sqlcipher.database.SQLiteException: file is encrypted or is not a database: create locale table failed
        //db = FeedReaderDbHelper.getInstance(this).getWritableDatabase("");
    }

    public void bookmarkCheckbox(View view) {
        boolean isChecked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.bookmarkCheckBox:
                if (isChecked) {
                    try {
                        insertHistoryIntoDB(title, url);
                    } catch (Exception SQLException) {

                    }
                } else {
                    //// TODO: 28-Nov-17 delete the bookmark
                }
        }
    }

    private void insertHistoryIntoDB(String title, String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HistoryEntryFeed.HistoryEntry.COLUMN_NAME_TITLE, title);
        values.put(HistoryEntryFeed.HistoryEntry.COLUMN_URL, url);
        db.insert(HistoryEntryFeed.HistoryEntry.TABLE_NAME, null, values);
        db.close();
    }

    public static void clearHistory() {
        webView.clearHistory();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void nextSearch() {
        mCurrentSearchIndex++; //the fisrt tiem this is called the index gets set to 0
        if (mCurrentSearchIndex < mSearchTerms.length) {
            //get the search string corresponding to this index and then search it
            webView.findAllAsync(mSearchTerms[mCurrentSearchIndex]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void search(View view) {
        searchTextOnPage();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void searchTextOnPage() {
        mCurrentSearchIndex = -1;
        String searchText = searchEditText.getText().toString();
        if (!Utils.isNullOrEmpty(searchText)) {
            mSearchTerms = new String[]{searchText};
            hideKeyBoard();
            nextSearch();
        } else {
            Toast.makeText(this, "Enter text to search", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
        if (isDoneCounting) {
            if (numberOfMatches > 0) {

            } else {
                Toast.makeText(this, "No result found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void hideKeyBoard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    public void exportDatabase(String databaseName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + getPackageName() + "//databases//" + databaseName + "";
                String backupDBPath = "backupname.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {


        }
    }

    public void micButton(View view) {
        askSpeechInput();
//        addTab();
    }

    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    public static void downloadingFile(final Context context) {
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition,
                        mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, File.separator + "secure downloads" + File.separator + URLUtil.guessFileName(url, contentDisposition,
                                mimetype));
                DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(context.getApplicationContext(), "Downloading File",
                        Toast.LENGTH_LONG).show();
            }
        });

    }


    /*     public void getDownloadList (){
              Intent intent = new Intent();
              Bundle extras = intent.getExtras();
              DownloadManager.Query q = new DownloadManager.Query();
              q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
              Cursor c = dm.query(q);

              if (c.moveToFirst()) {
                  int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                  if (status == DownloadManager.STATUS_SUCCESSFUL) {
                      // process download
                      title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                      // get other required data by changing the constant passed to getColumnIndex
                  }
              }
          }*/
  /*  public void addTab() {
        spec = host.newTabSpec("Tab Four");
        spec.setContent(new Intent(this, MainActivity.class));
        spec.setIndicator("Tab Four");
        host.addTab(spec);

    }*/
    public void adBlockers() {
        String line;
        adList = new ArrayList<String>();
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("hosts",
                        "raw", getPackageName()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ins));
        try {
            while ((line = bufferedReader.readLine()) != null) {
                adList.add(line);
            }

            hashTable = new Hashtable();
            for (String s : adList) {
                String[] array = s.split(" ");
                String sKey = "", sValue = "";
                if (array.length > 1) {
                    sKey = array[1];
                    sValue = array[0];
                    hashTable.put(sKey, sValue);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onTabSelected(int i) {
        switch (i) {
            case 0:
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {

                }
                break;
            case 1:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
            case 2:
                gridLinearLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("http://google.com");
                if (searchLinearLayout.getVisibility() == View.VISIBLE) {
                    searchLinearLayout.setVisibility(View.GONE);
                    urlLinearLayout.setVisibility(View.VISIBLE);
                    webView.clearMatches();
                }
                break;
            case 3:
                if (searchLinearLayout.getVisibility() == View.VISIBLE) {
                    searchLinearLayout.setVisibility(View.GONE);
                    urlLinearLayout.setVisibility(View.VISIBLE);
                    webView.clearMatches();
                    searchEditText.setText("");
                } else {
                    searchLinearLayout.setVisibility(View.VISIBLE);
                    urlLinearLayout.setVisibility(View.GONE);
                }
                break;
            case 4:
                if (bottomSheet == null) {
                    bottomSheet = new BottomSheet.Builder(MainActivity.this).title("Options").sheet(R.menu.menu).listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case R.id.share:
                                    shareTextUrl();
                                    break;
                               /* case R.id.help:
                                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                    startActivity(intent);

                                    break;*/

                                case R.id.bookmark:
                                    Intent bookmarkIntent = new Intent(MainActivity.this, BookmarkActivity.class);
                                    startActivityForResult(bookmarkIntent, 1);
                                    break;
                                case R.id.history:
                                    Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                                    startActivityForResult(historyIntent, 2);
                                    break;

                                case R.id.download:
                                    Intent downloadIntent = new Intent(MainActivity.this, DownloadActivity.class);
                                    startActivity(downloadIntent);
                                    break;
                                case R.id.fullScreen:
                                    Menu menu = bottomSheet.getMenu();
                                    //navigation.setVisibility(View.GONE);
                                    if (flags == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                        flags = 1025;
                                        MenuItem menuItem = menu.findItem(R.id.fullScreen);
                                        menuItem.setTitle("Exit Full screen");
                                        menuItem.setIcon(R.drawable.ic_fullscreen_exit_black);
                                    } else {
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                        flags = 1024;
                                        MenuItem menuItem = menu.findItem(R.id.fullScreen);
                                        menuItem.setTitle("Full screen");
                                        menuItem.setIcon(R.drawable.ic_fullscreen_black);
                                    }


                            }
                        }
                    }).show();
                } else {
                    bottomSheet.show();
                }
        }
    }


    @Override
    public void onTabLongSelected(int i) {

    }

    public void manageQuickLinks() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(MainActivity.this, "You Clicked at " + position, Toast.LENGTH_SHORT).show();
        gridLinearLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        String quickUrl = "";


        switch (position) {
            case 0:
                quickUrl = "www.google.com";
                webView.loadUrl("https://" + quickUrl);
                break;
            case 1:
                quickUrl = "www.facebook.com";
                webView.loadUrl("https://" + quickUrl);
                break;
            case 2:
                quickUrl = "www.amazon.in";
                webView.loadUrl("https://" + quickUrl);
                break;

            case 3:
                quickUrl = "www.youtube.com";
                webView.loadUrl("https://" + quickUrl);
                break;

            case 4:
                quickUrl = "www.cricbuzz.com";
                webView.loadUrl("https://" + quickUrl);
                break;

            case 5:
                quickUrl = "www.twitter.com";
                webView.loadUrl("https://" + quickUrl);
                break;

        }

    }
}






