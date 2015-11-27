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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;


public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener,
        MenusAdapter.OnItemClickListener, ServiceConnection{

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

    //Data model
    public static SongsFinder finder;
    public static SongAdapter adapter;
    String songDirecory;

    // Views
    private Toolbar toolbar;
    private TextView emptyView;

    private RecyclerView songsView;
    private RecyclerView optionsView;

    private LinearLayout parentView;
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
        songDirecory = preferences.getString(PREFERENCES_DIR, /*"/storage/"*/ Environment.getExternalStorageDirectory().getPath());


        finder = new SongsFinder(songDirecory);
        receiver = new Receiver();

        // Setting views
        setUpToolbar();
        setUpNavDrawer();

        parentView = (LinearLayout) findViewById(R.id.parentView);

        emptyView = (TextView) findViewById(R.id.emptyView);

        songsView = (RecyclerView) findViewById(R.id.songsview);
        songsView.setHasFixedSize(true);
        songsView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, finder.songs, this);
        songsView.setAdapter(adapter);


        setDrawerOptionsMenu();


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
                adapter.changeList(finder.songs);
                adapter.notifyDataSetChanged();

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
        super.onStop();
    }

    @Override
    public void onMenuItemClick(int position) {

        switch(position) {
            case 2 /*Change Path*/:
                startFilePicker();
                break;
        }

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

}
