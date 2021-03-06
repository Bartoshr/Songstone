package bartoshr.songstone.activities;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

import bartoshr.songstone.adapters.MenusAdapter;
import bartoshr.songstone.fragments.PanelFragment;
import bartoshr.songstone.fragments.PopupFragment;
import bartoshr.songstone.R;
import bartoshr.songstone.model.Song;
import bartoshr.songstone.adapters.SongAdapter;
import bartoshr.songstone.services.SongService;
import bartoshr.songstone.model.Songmark;
import bartoshr.songstone.adapters.SongmarkAdapter;
import bartoshr.songstone.utils.SongsFinder;
import bartoshr.songstone.utils.Utils;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity implements
        PanelFragment.OnPanelClickListener,
        SongAdapter.OnItemClickListener,
        MenusAdapter.OnItemClickListener, SongmarkAdapter.OnItemClickListener, ServiceConnection{

    private static final String TAG = "MainActivity";
    private static final int MY_WRITE_EXTERNAL_STORAGE = 1;
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
    public static SongmarkAdapter songmarkAdapter;
    String songDirecory;

    //Store saved songmarks
    ArrayList<Songmark> songmarks;

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

        Log.i(TAG, "onCreate()");

        preferences = getSharedPreferences(PREFERENCES_NAME, AppCompatActivity.MODE_PRIVATE);
        songDirecory = preferences.getString(PREFERENCES_DIR, /*"/storage/"*/ new File("/storage").getPath());

        Paper.init(this);

        askForPermissions();
        finder = new SongsFinder(songDirecory);
        receiver = new Receiver();

        // Setting views
        setUpToolbar();
        setUpNavDrawer();

        emptyView = (TextView) findViewById(R.id.emptyView);

        setSongsView();
        setDrawerOptionsMenu();
        setDrawerBookmarksMenu();

        startService();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

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
        songmarks = Paper.book().read("Bookmarks", new ArrayList<Songmark>());

        bookmarksView = (RecyclerView) findViewById(R.id.bookmarksView);
        bookmarksView.setLayoutManager(new LinearLayoutManager(this));
        songmarkAdapter = new SongmarkAdapter(songmarks, this);
        bookmarksView.setAdapter(songmarkAdapter);
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

        Log.i(TAG,"onStart");

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
    public void updatePanel(String title, String artist){

        Log.d(TAG, "UpdatePanel()");

        Fragment f = getFragmentManager().findFragmentByTag(PANEL_FRAGMENT_TAG);

        if (f != null) {
            getFragmentManager().popBackStack();
        }

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TEXT, title + " - " + artist);

        PanelFragment fragment = (PanelFragment) PanelFragment.instantiate(this, PanelFragment.class.getName());
        fragment.setArguments(bundle);
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

                if(songService != null)
                songService.setList(finder.songs);

                Toast.makeText(this,  "Directory changed :  " + uri.getPath(), Toast.LENGTH_SHORT).show();
        }
    }

    public void startFilePicker(){
        Intent i = new Intent(this, FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory like so
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, songDirecory);
            startActivityForResult(i, FILE_CODE);

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
            updatePanel(title, artist);
        }

        songService.musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Service Disconected");
        songService.musicBound = false;
    }

    public void startService(){
        songIntent = new Intent(this, SongService.class);
        this.startService(songIntent);
    }


    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        hidePanel();
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");

        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SongService.ACTION_REFRESH_VIEW);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        Log.d(TAG, "OnNewIntent()");

        boolean shouldUpdatePanel =
                intent.getBooleanExtra(SongService.NOTIFICATION_MARK, false);


        if(shouldUpdatePanel) {
            Song currentSong = songService.getCurrentSong();
            if (currentSong != null) {
                String title = currentSong.getTitle();
                String artist = currentSong.getArtist();
                updatePanel(title, artist);
            }
        }

        super.onNewIntent(intent);
    }

    public void hidePanel(){
        Fragment f = getFragmentManager().findFragmentByTag(PANEL_FRAGMENT_TAG);
        if (f != null) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onStop() {
        unbindService(this);
        //Saving Bookmarks before exit
        Paper.book().write("Bookmarks", songmarks);
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
        Songmark songmark = songmarks.get(itemPosition);
        int position  = finder.songs.indexOf(songmark.song);

        if(position != -1) {
            LocalBroadcastManager local = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, SongService.ACTION_PLAY);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_POSITION, position);
            broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_SONG_POSITION, songmark.position);
            local.sendBroadcast(broadcastIntent);
        } else {
            Toast.makeText(MainActivity.this, "Position : " + position, Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    @Override
    public boolean onBookmarkItemLongClick(int position) {
        songmarks.remove(position);
        songmarkAdapter.notifyItemRemoved(position);
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

            final Songmark songmark = new Songmark(song, songPosition);
            songmarks.add(songmark);
            songmarkAdapter.notifyItemInserted(songmarks.size()-1);

            Toast.makeText(this, "Songmark Added", Toast.LENGTH_SHORT).show();
        return true;
    }

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra(SongService.TITLE_KEY);
            String artist = intent.getStringExtra(SongService.ARTIST_KEY);
            updatePanel(title, artist);
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

    public void askForPermissions(){

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

            Log.d(TAG, "Permission (WRITE_EXTERNAL_STORAGE) == "+permissionCheck);

            if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_WRITE_EXTERNAL_STORAGE);
            }

    }

    public void doNothing(View v){
        // handle click on DrawerLayout to prevent
        // propagate behind
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

       if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
           //event.startTracking();
           //return true;
       }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Toast.makeText(this, "VOLUME UP", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    public void updateSongs(){
        finder.search(songDirecory);
        songAdapter.changeList(finder.songs);
        songAdapter.notifyDataSetChanged();

        if(songService != null)
            songService.setList(finder.songs);
    }


}
