package com.app.ssoft.securebrowser;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.crashlytics.android.Crashlytics;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

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
import test.jinesh.easypermissionslib.EasyPermission;

//import com.yarolegovich.lovelydialog.LovelyStandardDialog;
/*import io.salyangoz.updateme.UpdateMe;
import io.salyangoz.updateme.listener.OnNegativeButtonClickListener;
import io.salyangoz.updateme.listener.OnPositiveButtonClickListener;*/

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener, WebView.FindListener, AdapterView.OnItemClickListener, BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, EasyPermission.OnPermissionResult {
    //    BottomNav.OnTabSelectedListener,
    private static final int REQ_CODE_SPEECH_INPUT = 3;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
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
    //    private BottomNav bottomNav;
    private DBHelper dbHelper;
    private GridView quickLinksGV;
    private View gridLinearLayout;
    private ArrayList headlines;
    private ArrayList links;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArticleAdapter mAdapter;
    private String urlString;
    private BottomNavigationView bottomNavigation;
    private EasyPermission easyPermission;
    private ImageView faviconImageView;
    private String favicons;


    //    private TextView mTextMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* UpdateMe.with(this).check();
        UpdateMe.with(this, 30).setDialogVisibility(true)
                .continueButtonVisibility(true)
                .setDialogIcon(R.drawable.common_google_signin_btn_icon_dark)
                .onNegativeButtonClick(new OnNegativeButtonClickListener() {

                    @Override
                    public void onClick(LovelyStandardDialog dialog) {

                        Log.d(UpdateMe.TAG, "Later Button Clicked");
                        dialog.dismiss();
                    }
                })
                .onPositiveButtonClick(new OnPositiveButtonClickListener() {

                    @Override
                    public void onClick(LovelyStandardDialog dialog) {

                        Log.d(UpdateMe.TAG, "Update Button Clicked");
                        dialog.dismiss();
                    }
                })
                .check();

*/
        urlString = "http://www.androidcentral.com/feed";
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


        /******* news feed ********/

        progressBar = findViewById(R.id.progressBar);

        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        faviconImageView = findViewById(R.id.faviconImageView);

        mSwipeRefreshLayout = findViewById(R.id.refreshContainer);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.canChildScrollUp();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mAdapter.clearData();
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(true);
                loadFeed();
            }
        });

        if (!isNetworkAvailable()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_message)
                    .setTitle(R.string.alert_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();

        } else if (isNetworkAvailable()) {
            loadFeed();
        }
        /*****/

        Fabric.with(this, new Crashlytics());
//        exportDatabase(DBHelper.DATABASE_NAME);
        gridLinearLayout = findViewById(R.id.gridLinearLayout);
        quickLinksGV = findViewById(R.id.quickLinksGV);
        CustomQLGridAdapter adapter = new CustomQLGridAdapter(MainActivity.this, web, imageId);
        quickLinksGV.setAdapter(adapter);
        quickLinksGV.setOnItemClickListener(this);


        bottomNavigation = findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        Utils.disableShiftMode(bottomNavigation);
        urlLinearLayout = (LinearLayout) findViewById(R.id.urlLinearLayout);
        searchLinearLayout = (LinearLayout) findViewById(R.id.searchLinearLayout);
        searchEditText = (EditText) findViewById(R.id.searchText);


        searchEditText.setSelectAllOnFocus(true);
        bookmarkCheckbox = (CheckBox) findViewById(R.id.bookmarkCheckBox);
        bookmarkCheckbox.setOnCheckedChangeListener(this);
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
                    enableOrDisableControls(true);
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
                return true;
            }
        });
        urlText.setOnClickListener(this);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchTextOnPage();
                return true;
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

        //handling external intent
        if (!url.isEmpty()) {
            gridLinearLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
            enableOrDisableControls(true);
        } else {
            enableOrDisableControls(false);
        }


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                if (icon != null && gridLinearLayout.getVisibility() == View.GONE) {
                    faviconImageView.setImageBitmap(icon);
                    favicons = Utils.BitMapToString(icon);
                } else {
                    faviconImageView.setImageDrawable(getResources().getDrawable(R.drawable.search_url));
                    favicons = null;
                }
                try {
                    insertHistoryIntoDB(title, url, favicons);
                } catch (Exception SQLException) {

                }
            }
        });
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
//                bottomNavigationMenu = bottomNav.
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
        easyPermission = new EasyPermission();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // only for gingerbread and newer versions
            easyPermission.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            downloadingFile(MainActivity.this);
        }
        this.registerForContextMenu(webView);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data != null) {
                    gridLinearLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    enableOrDisableControls(true);
                    String urlLink = data.getStringExtra("url");
                    webView.loadUrl(urlLink);
                }
                break;
            case 2:
                if (data != null) {
                    gridLinearLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    enableOrDisableControls(true);
                    enableOrDisableControls(true);
                    String urlLink = data.getStringExtra("url");
                    webView.loadUrl(urlLink);
                }
                break;
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    gridLinearLayout.setVisibility(View.GONE);
                    enableOrDisableControls(true);
                    webView.setVisibility(View.VISIBLE);
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    webView.reload();
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
                    webView.stopLoading();
                    webView.clearCache(true);
                    webView.setVisibility(View.GONE);
                    faviconImageView.setImageDrawable(getResources().getDrawable(R.drawable.search_url));
                    gridLinearLayout.setVisibility(View.VISIBLE);
                    enableOrDisableControls(false);


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
        final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();
        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // handle on context menu click
                return true;
            }
        };

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            menu.setHeaderTitle(webViewHitTestResult.getExtra());
            menu.add(0, 666, 0, "Save Image").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String DownloadImageURL = webViewHitTestResult.getExtra();

                    if (URLUtil.isValidUrl(DownloadImageURL)) {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);

                        Toast.makeText(MainActivity.this, "Image Downloaded Successfully.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Sorry.. Something Went Wrong.", Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        }
    }


    private void insertBookmarkIntoDB(String title, String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

                    // custom dialog
                    final Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.bookmark_dialog);
                    dialog.setTitle("New Bookmark");

                    // set the custom dialog components - text, image and button
                    final EditText titleText = (EditText) dialog.findViewById(R.id.bookMarkTitle);
                    titleText.setText(title);
                    final EditText urlText = (EditText) dialog.findViewById(R.id.bookMarkURL);
                    urlText.setText(url);


                    Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            bookmarkCheckbox.setChecked(false);
                        }
                    });
                    Button buttonSave = (Button) dialog.findViewById(R.id.buttonSave);
                    // if button is clicked, close the custom dialog
                    buttonSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if ((urlText.getText().toString() != null && !(urlText.getText().toString().isEmpty()) &&
                                        (titleText.getText().toString() != null && !(titleText.getText().toString().isEmpty())))) {
                                    insertBookmarkIntoDB(titleText.getText().toString(), urlText.getText().toString());
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Bookmark added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "All fields are required for bookmark the link", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception SQLException) {

                            }
                        }
                    });

                    dialog.show();


                } else {
                    //// TODO: 28-Nov-17 delete the bookmark
                }
        }
    }

    private void insertHistoryIntoDB(String title, String url, String favicons) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HistoryEntryFeed.HistoryEntry.COLUMN_NAME_TITLE, title);
        values.put(HistoryEntryFeed.HistoryEntry.COLUMN_URL, url);
        values.put(HistoryEntryFeed.HistoryEntry.COLUMN_FAVICON, favicons);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(MainActivity.this, "You Clicked at " + position, Toast.LENGTH_SHORT).show();
        gridLinearLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        String quickUrl = "";
        enableOrDisableControls(true);


        switch (position) {
            case 0:
                quickUrl = getString(R.string.googleURL);
                webView.loadUrl("https://" + quickUrl);
                break;
            case 1:
                quickUrl = getString(R.string.facebookURL);
                webView.loadUrl("https://" + quickUrl);
                break;
            case 2:
                quickUrl = getString(R.string.amazonURL);
                webView.loadUrl("https://" + quickUrl);
                break;

            case 3:
                quickUrl = getString(R.string.youtubeURL);
                webView.loadUrl("https://" + quickUrl);
                break;

            case 4:
                quickUrl = getString(R.string.cricbuzzURL);
                webView.loadUrl("https://" + quickUrl);
                break;

            case 5:
                quickUrl = getString(R.string.twitterURL);
                webView.loadUrl("https://" + quickUrl);
                break;

        }

    }

    public void loadFeed() {

        if (!mSwipeRefreshLayout.isRefreshing())
            progressBar.setVisibility(View.VISIBLE);

        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            //what to do when the parsing is done
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                //list is an Array List with all article's information
                //set the adapter to recycler view
                mAdapter = new ArticleAdapter(list, R.layout.row, MainActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                progressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

            }

            //what to do in case of error
            @Override
            public void onError() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Unable to load data.",
                                Toast.LENGTH_LONG).show();
                        Log.i("Unable to load ", "articles");
                    }
                });
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onResume() {

        super.onResume();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mAdapter != null)
            mAdapter.clearData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigation_back:
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    if (gridLinearLayout.getVisibility() == View.GONE) {
                        urlText.setText("");
                        webView.clearHistory();
                        webView.stopLoading();
                        webView.clearCache(true);
                        webView.setVisibility(View.GONE);
                        gridLinearLayout.setVisibility(View.VISIBLE);
                        faviconImageView.setImageDrawable(getResources().getDrawable(R.drawable.search_url));
                        enableOrDisableControls(false);

                    } else {
                        finish();
                    }
                }
                break;
            case R.id.navigation_forward:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
            case R.id.navigation_home:
                gridLinearLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                enableOrDisableControls(true);
                webView.loadUrl("http://google.com");
                if (searchLinearLayout.getVisibility() == View.VISIBLE) {
                    searchLinearLayout.setVisibility(View.GONE);
                    urlLinearLayout.setVisibility(View.VISIBLE);
                    webView.clearMatches();
                }
                break;
            case R.id.navigation_find:
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
            case R.id.navigation_interface:
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
        return false;
    }


    public void enableOrDisableControls(boolean isEnabled) {
        if (bottomNavigation != null && urlText != null && bookmarkCheckbox != null) {
            if (isEnabled) {
                bookmarkCheckbox.setVisibility(View.VISIBLE);
                urlText.setCursorVisible(true);
                bottomNavigation.getMenu().getItem(0).setEnabled(true);
                bottomNavigation.getMenu().getItem(1).setEnabled(true);
                bottomNavigation.getMenu().getItem(4).setEnabled(true);

            } else {
                bookmarkCheckbox.setVisibility(View.INVISIBLE);
                urlText.setCursorVisible(false);
                bottomNavigation.getMenu().getItem(0).setEnabled(false);
                if (!webView.canGoForward()) {
                    bottomNavigation.getMenu().getItem(1).setEnabled(false);
                } else {
                    bottomNavigation.getMenu().getItem(1).setEnabled(true);
                }
                bottomNavigation.getMenu().getItem(4).setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.urlText:
                urlText.setCursorVisible(true);

        }
    }


    @Override
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (isGranted) {
                    //granted read contacts permission
                    //asking for one more permission after contacts permission is granted
                    downloadingFile(this);
                } else {
                    //denied read contacts permission
                    //asking for one more permission even after contacts permission is denied
                    Toast.makeText(this, "Please give permission for writing external storage from setting", Toast.LENGTH_LONG).show();


                }
                break;

        }
    }
}






