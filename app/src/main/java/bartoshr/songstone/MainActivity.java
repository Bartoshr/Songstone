package bartoshr.songstone;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity implements
        PanelFragment.OnPanelClickListener,
        SongAdapter.OnItemClickListener,
        MenusAdapter.OnItemClickListener, BookmarkAdapter.OnItemClickListener, ServiceConnection{

    private static final String TAG = "MainActivity";
    private static final String PANEL_FRAGMENT_TAG = "PANEL_FRAGMENT_TAG";

    //Preferences
    private SharedPreferences preferences;
    private static final String PREFERENCES_NAME = "PREFERENCES_NAME";
    private static final String PREFERENCES_DIR = "PREFERENCES_DIR";

    // Constans
    private static final int FILE_CODE = 1;
    public static final String BUNDLE_TEXT = "BUNDLE_TEXT";
    public static final String BUNDLE_POSITION = "BUNDLE_POSITION";

    //Adapters and Finders
    public static SongsFinder finder;
    public static SongAdapter songAdapter;
    public static BookmarkAdapter bookmarkAdapter;
    String songDirecory;

    //Store saved bookmarks
    ArrayList<Bookmark> bookmarks;

    // Views
    private Toolbar toolbar;
    private TextView emptyView;

    private RecyclerView songsView;
    private RecyclerView optionsView;
    private RecyclerView bookmarksView;

    private DrawerLayout drawerLayout;

    //Services
    private SongService songService;
    private Intent songIntent;

    Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);
        songDirecory = preferences.getString(PREFERENCES_DIR, /*"/storage/"*/ new File("/storage/sdcard/Music").getPath());


        Paper.init(this);

        finder = new SongsFinder(songDirecory);
        receiver = new Receiver();

        // Setting views
        setUpToolbar();
        setUpNavDrawer();

        emptyView = (TextView) findViewById(R.id.emptyView);


        setSongsView();
        setDrawerOptionsMenu();
        setDrawerBookmarksMenu();


        Log.i(TAG, "onCreate()");
        startService();

    }


    public void setDrawerOptionsMenu(){
        final String[] options = getResources().getStringArray(R.array.drawer_options_list);
        final int[] glyphs = new int[]{
                R.drawable.ic_action_stats,
                R.drawable.ic_action_sets,
                R.drawable.ic_action_change_path};

        optionsView = (RecyclerView) findViewById(R.id.optionsView);
        optionsView.setHasFixedSize(true);
        optionsView.setLayoutManager(new LinearLayoutManager(this));
        optionsView.setAdapter(new MenusAdapter(options, glyphs, this));
    }

    public void setDrawerBookmarksMenu(){
        bookmarks = Paper.book().read("Bookmarks", new ArrayList<Bookmark>());

        bookmarksView = (RecyclerView) findViewById(R.id.bookmarksView);
        bookmarksView.setLayoutManager(new LinearLayoutManager(this));
        bookmarkAdapter = new BookmarkAdapter(bookmarks, this);
        bookmarksView.setAdapter(bookmarkAdapter);
    }

    public void setSongsView(){
        songsView = (RecyclerView) findViewById(R.id.songsview);
        songsView.setHasFixedSize(true);
        songsView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(this, finder.songs, this);
        songsView.setAdapter(songAdapter);
    }

    @Override
    protected void onStart() {

        super.onStart();
        if (finder.songs.isEmpty()) {
            songsView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            songsView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        this.bindService(songIntent, this, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SongService.ACTION_REFRESH_VIEW);
        registerReceiver(receiver, intentFilter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            /*case R.id.action_settings:
                startFilePicker();
                break;*/
            case R.id.action_bluetooth:
                Utils.toggleBluetooth(getApplicationContext());
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    // Method started when need to change title
    public void updateView(String title, String artist){

        Fragment f = getFragmentManager().findFragmentByTag(PANEL_FRAGMENT_TAG);

        if (f != null) {
            getFragmentManager().popBackStack();
        }

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TEXT, title + " - " + artist);

        PanelFragment fragment = (PanelFragment) PanelFragment.instantiate(this, PanelFragment.class.getName());
        fragment.setArguments(bundle);

        fragment.setOnPanelClickListener(this);
        fragment.setAnimationChangedListener(new PanelFragment.OnAnimationChanged() {

            @Override
            public void onAnimationEnded() {
                songsView.setPadding(0, 0, 0, 115);
            }

            @Override
            public void onAnimationStarted() {
                if (songsView.getPaddingBottom() != 0)
                    songsView.setPadding(0, 0, 0, 0);
            }
        });

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up,
                        R.animator.slide_down,
                        R.animator.slide_up,
                        R.animator.slide_down)
                .add(R.id.screenLayout, fragment,
                        PANEL_FRAGMENT_TAG
                ).addToBackStack(null).commit();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();
                preferences.edit().putString(PREFERENCES_DIR, uri.getPath()).commit();
                songDirecory = uri.getPath();

                finder.search(songDirecory);
                songAdapter.changeList(finder.songs);
                songAdapter.notifyDataSetChanged();

                Toast.makeText(this,  "Directory changed :  " + uri.getPath(), Toast.LENGTH_SHORT).show();
        }
    }

    public void startFilePicker(){
        Intent i = new Intent(this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory like so

        File f = new File(songDirecory);
        if(f.canWrite()) {
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, songDirecory);
            startActivityForResult(i, FILE_CODE);
        } else {
            Toast.makeText(this, "Don't have permission", Toast.LENGTH_SHORT).show();
        }
    }

    // Shows Popup for editing artist name and  title
    private void startPopup(int position) {
        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_POSITION, position);
        PopupFragment popupDialog = new PopupFragment();

        popupDialog.setArguments(bundle);
        popupDialog.show(fm, "popupFragment");
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "Service Connected");
        SongService.MusicBinder binder = (SongService.MusicBinder)service;
        // Here's where we finally create the MusicService
        songService = binder.getService();
        songService.setList(finder.songs);

        Song currentSong = songService.getCurrentSong();
        if(currentSong != null) {
            String title = currentSong.getTitle();
            String artist = currentSong.getArtist();
            updateView(title, artist);
        }

        songService.musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Service Disconected");

        Toast.makeText(this, "Service Disconected", Toast.LENGTH_SHORT).show();

        songService.musicBound = false;
    }

    public void startService(){
        songIntent = new Intent(this, SongService.class);
        this.startService(songIntent);
    }


    @Override
    protected void onPause() {

        Fragment fragment = getFragmentManager().findFragmentByTag(PANEL_FRAGMENT_TAG);
        getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_up,
                R.animator.slide_down)
                .remove(fragment).commit();

        super.onPause();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        unbindService(this);

        //Saving Bookmarks before exit
        Paper.book().write("Bookmarks", bookmarks);

        super.onStop();
    }

    @Override
    public void onMenuItemClick(int position) {
        switch(position) {
            case 2 /*Change Path*/:
                startFilePicker();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onSongItemClick(int position) {
        LocalBroadcastManager local = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
        broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, SongService.ACTION_PLAY);
        broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_POSITION, position);
        local.sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onSongItemLongClick(int position) {
        startPopup(position);
        return true;
    }

    @Override
    public void onBookmarkItemClick(int itemPosition) {
        Bookmark bookmark = bookmarks.get(itemPosition);
        int position  = finder.songs.indexOf(bookmark.song);

        if(position != -1) {
            LocalBroadcastManager local = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, SongService.ACTION_PLAY);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_POSITION, position);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_SONG_POSITION, bookmark.position);
            local.sendBroadcast(broadcastIntent);
        } else {
            Toast.makeText(MainActivity.this, "Position : " + position, Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    @Override
    public boolean onBookmarkItemLongClick(int position) {
        bookmarks.remove(position);
        bookmarkAdapter.notifyItemRemoved(position);
        return true;
    }

    @Override
    public void onPanelClick() {
        LocalBroadcastManager local = LocalBroadcastManager.getInstance(this);
        Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
        broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, SongService.ACTION_TOGGLE);
        local.sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onPanelLongCLick() {
            Song song = songService.getCurrentSong();
            int songPosition = songService.getCurrentPosition();

            final Bookmark bookmark = new Bookmark(song, songPosition);
            bookmarks.add(bookmark);
            bookmarkAdapter.notifyItemInserted(bookmarks.size()-1);

            Toast.makeText(this, "Bookmark Added", Toast.LENGTH_SHORT).show();
        return true;
    }

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra(SongService.TITLE_KEY);
            String artist = intent.getStringExtra(SongService.ARTIST_KEY);
            updateView(title, artist);
        }
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    private void setUpNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.setDrawerListener(mDrawerToggle);
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mDrawerToggle.syncState();
        }
    }

    public void doNothing(View v){
        // handle click on DrawerLayout to prevent
        // propagate behind
    }

}
