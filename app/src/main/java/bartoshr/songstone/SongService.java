package bartoshr.songstone;

/**
 * Created by bartosh on 28.05.15.
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class SongService extends Service
        implements  MediaPlayer.OnPreparedListener,
                    MediaPlayer.OnCompletionListener,
                    MediaPlayer.OnErrorListener {

    enum State { playing, paused, stopped};

    //Interace to change the panelView displaying title of song
    ChangePanelView changePanelView;

    // Inidicate player status
    State state;

    //Allows interaction with media buttons
    MediaSessionCompat mSession;

    //Component name of the MusicIntentReceiver.
    ComponentName mediaButtonEventReceiver;

    // Array of Songs
    public ArrayList<Song> songs = new ArrayList<Song>();

    //Current Song
    public int currentSong = 0;

    //Filter
    public static final String BROADCAST_ORDER = "bartoshr.songstone.MUSIC_SERVICE";
    public static final String BROADCAST_EXTRA_GET_ORDER = "bartoshr.songstone.ORDER";
    public static final String BROADCAST_EXTRA_GET_POSITION = "bartoshr.songstone.POSITION";

    //Actions
    public static final String ACTION_PLAY="bartoshr.songstone.ACTION_PLAY";
    public static final String ACTION_PAUSE="bartoshr.songstone.ACTION_PAUSE";
    public static final String ACTION_TOGGLE="bartoshr.songstone.ACTION_TOGGLE";
    public static final String ACTION_NEXT = "bartoshr.songstone.ACTION_NEXT";
    public static final String ACTION_PREV="bartoshr.songstone.ACTION_PREV";

    // real player
    public static MediaPlayer player = new MediaPlayer();


    public SongService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        initPlayer();

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .registerReceiver(localBroadcastReceiver, new IntentFilter(SongService.BROADCAST_ORDER));

        mediaButtonEventReceiver = new ComponentName(this,
                ExternalBroadcastReceiver.class);

        mSession = new MediaSessionCompat(getApplicationContext(),"tag",mediaButtonEventReceiver,null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);

        state = State.stopped;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void initPlayer(){
        if(player == null)
            player = new MediaPlayer();

        // Assures the CPU continues running this service
        // even when the device is sleeping.
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);

        Log.d("Songstone", "initPlayer");

    }

    // player functions

    public void playSong() {

        player.reset();

        if(changePanelView != null)
            changePanelView.togglePanel(songs.get(currentSong).getTitle());

        Log.d("Songstone", songs.get(currentSong).getPath());

        try {
            player.setDataSource(songs.get(currentSong).getPath());
        }
        catch(IOException io) {
            Log.e("Songtone", "IOException: couldn't change the song", io);
        }
        catch(Exception e) {
            Log.e("Songstone", "Error when changing the song", e);
        }

        player.prepareAsync();

    }

    public void playNextSong(){
        currentSong = getNextSong();
        playSong();
    }

    public void playPrevSong(){
        currentSong = getPrevSong();
        playSong();
    }


    public void pausePlayer() {
        player.pause();
        state = State.paused;
    }

    public void resumePlayer() {
        player.start();
        state = State.playing;
    }

    public void togglePlayer(){
        if(state == State.paused) resumePlayer();
        else pausePlayer();
    }

    public void stopPlayer() {
        if (player == null)
            return;

        player.stop();
        player.release();
        player = null;

        state = State.stopped;
    }

    public int getNextSong(){
       return  (++currentSong)%(songs.size());
    }

    public int getPrevSong(){
         int result = (--currentSong)%(songs.size());
         return (result>0)? result : result+songs.size() ;
    }


    // MediaPlayer interfaces

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNextSong();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        state = State.stopped;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
        state = State.playing;
    }

    // Broadcasts

    BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String order = intent.getStringExtra(SongService.BROADCAST_EXTRA_GET_ORDER);

            if (order.equals(SongService.ACTION_PLAY)) {
                currentSong = intent.getIntExtra(SongService.BROADCAST_EXTRA_GET_POSITION, currentSong);
                playSong();
            } else if (order.equals(SongService.ACTION_PAUSE)) {
                pausePlayer();
            } else if (order.equals(SongService.ACTION_TOGGLE)) {
                togglePlayer();
            } else if (order.equals(SongService.ACTION_NEXT)) {
                playNextSong();
            } else if (order.equals(SongService.ACTION_PREV)) {
                playPrevSong();
            }
        }
    };

    public static class ExternalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            LocalBroadcastManager local = LocalBroadcastManager.getInstance(context);
            String action = intent.getAction();


            if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

                KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);

                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                    return;

                String intentValue = null;

                switch (keyEvent.getKeyCode()) {

                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        intentValue = SongService.ACTION_TOGGLE;
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        intentValue = SongService.ACTION_TOGGLE;
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        intentValue = SongService.ACTION_NEXT;
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        intentValue = SongService.ACTION_PREV;
                        break;

                }

                // Actually sending the Intent
                if (intentValue != null) {
                    Intent broadcastIntent = new Intent(SongService.BROADCAST_ORDER);
                    broadcastIntent.putExtra(SongService.BROADCAST_EXTRA_GET_ORDER, intentValue);
                    local.sendBroadcast(broadcastIntent);
                }
            }
        }
    }


    // Binding things

    public boolean musicBound = false;
    private final IBinder musicBind = new MusicBinder();

    public class MusicBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    // Binding profits

    void setList( ArrayList<Song> songs){
        this.songs = songs;
    }

    // Interfaces

    interface ChangePanelView {
       public void togglePanel(String text);
    }
}
