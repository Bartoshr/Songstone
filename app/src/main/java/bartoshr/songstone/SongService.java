package bartoshr.songstone;

/**
 * Created by bartosh on 28.05.15.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class SongService extends Service
        implements  MediaPlayer.OnPreparedListener,
                    MediaPlayer.OnCompletionListener,
                    MediaPlayer.OnErrorListener,
                    AudioManager.OnAudioFocusChangeListener{

    private static final String TAG = "SongService";

    enum State { playing, paused, stopped};

    // Inidicate player status
    State state;

    //Allows interaction with media buttons
    MediaSession mSession;

    //Component name of the MusicIntentReceiver.
    ComponentName mediaButtonEventReceiver;


    // Array of Songs
    public ArrayList<Song> songs = new ArrayList<Song>();

    //Current Song
    public int currentSong = 0;
    public int songPosition = 0;

    //Filter
    public static final String BROADCAST_ORDER = "bartoshr.songstone.MUSIC_SERVICE";
    public static final String BROADCAST_EXTRA_GET_ORDER = "bartoshr.songstone.ORDER";
    public static final String BROADCAST_EXTRA_GET_SONG_POSITION = "bartoshr.songstone.SONG_POSITION";
    public static final String BROADCAST_EXTRA_GET_POSITION = "bartoshr.songstone.POSITION";

    //Actions
    public static final String ACTION_PLAY="bartoshr.songstone.ACTION_PLAY";
    public static final String ACTION_PAUSE="bartoshr.songstone.ACTION_PAUSE";
    public static final String ACTION_TOGGLE="bartoshr.songstone.ACTION_TOGGLE";
    public static final String ACTION_NEXT = "bartoshr.songstone.ACTION_NEXT";
    public static final String ACTION_PREV="bartoshr.songstone.ACTION_PREV";

    public static final String ACTION_REFRESH_VIEW = "bartosh.songstone.REFRESH_VIEW";
    public static final String TITLE_KEY = "TITLE_KEY";
    public static final String ARTIST_KEY = "ARTIST_KEY";

    //Marks
    public static final String NOTIFICATION_MARK="bartoshr.songstone.NOTIFICATION_MARK";


    // real player
    public static MediaPlayer player = new MediaPlayer();
    static final private int ONGOING_NOTIFICATION_ID = 1;

    AudioManager audioManager;
    int requstResult = AudioManager.AUDIOFOCUS_REQUEST_FAILED;


    public SongService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        initPlayer();

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .registerReceiver(localBroadcastReceiver, new IntentFilter(SongService.BROADCAST_ORDER));

        requestFocus();
        setMediaSession();

        state = State.stopped;

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        mSession.release();
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

        Log.i(TAG, "initPlayer");

    }

    // player functions

    public void playSong() {

        if (requstResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        player.reset();

        String title = songs.get(currentSong).getTitle();
        String artist = songs.get(currentSong).getArtist();


        // Seding intent for refresh current View in MainActivity
        Intent intent = new Intent();
        intent.setAction(ACTION_REFRESH_VIEW);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(ARTIST_KEY, artist);
        sendBroadcast(intent);


        Log.i(TAG, songs.get(currentSong).getPath());

        try {
            player.setDataSource(songs.get(currentSong).getPath());
            //player.seekTo(songPosition);
        }
        catch(IOException io) {
            Log.e("Songtone", "IOException: couldn't change the song", io);
        }
        catch(Exception e) {
            Log.e("Songstone", "Error when changing the song", e);
        }

        player.prepareAsync();
        setForeground(artist, title);

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
        removeForeground();
        state = State.paused;
    }

    public void resumePlayer() {

        String title = songs.get(currentSong).getTitle();
        String artist = songs.get(currentSong).getArtist();

        player.start();
        state = State.playing;
        setForeground(artist, title);
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

        audioManager.abandonAudioFocus(this);
    }

    public int getNextSong(){
       return  (++currentSong)%(songs.size());
    }

    public int getPrevSong(){
         int result = (--currentSong)%(songs.size());
         return (result>=0)? result : result+songs.size() ;
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
        player.seekTo(songPosition);
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
                songPosition = intent.getIntExtra(SongService.BROADCAST_EXTRA_GET_SONG_POSITION, 0);
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

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {

        boolean phoned = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if(phoned) {
                        resumePlayer();
                        Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE", Toast.LENGTH_SHORT).show();
                        phoned = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if(player.isPlaying()) {
                        pausePlayer();
                        Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING", Toast.LENGTH_SHORT).show();
                        phoned = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK", Toast.LENGTH_SHORT).show();
                    if(player.isPlaying()) {
                        pausePlayer();
                        Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING", Toast.LENGTH_SHORT).show();
                        phoned = true;
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

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

    public void setList( ArrayList<Song> songs){
        this.songs = songs;
    }

    public Song getCurrentSong() {
        if(currentSong >songs.size()-1) return null;
        return songs.get(currentSong);
    }

    public int getCurrentPosition()
    {
        return player.getCurrentPosition();
    }

    public void setCurrentPosition(int position){
        player.seekTo(position);
    }

    //Forefround

    void setForeground(String artist, String title){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(NOTIFICATION_MARK, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_note)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    void removeForeground(){
        stopForeground(true);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume
//            Toast.makeText(this, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK", Toast.LENGTH_SHORT).show();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Raise it back to normal
//            Toast.makeText(this, "AUDIOFOCUS_GAIN", Toast.LENGTH_SHORT).show();
        } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS) {
           // Toast.makeText(this, "AUDIOFOCUS_LOSS", Toast.LENGTH_SHORT).show();
//            player.pause();
            audioManager.abandonAudioFocus(this);
        }
    }

    private void requestFocus(){
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        requstResult = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    private void setMediaSession(){
        mSession = new MediaSession(getApplicationContext(), "tag");

        if (mSession == null) {
            Log.e(TAG, "initMediaSession: _mediaSession = null");
            return;
        }

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);

                int keycode = keyEvent.getKeyCode();

                if (keyEvent.getAction() != KeyEvent.ACTION_UP) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            playNextSong();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            playPrevSong();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            togglePlayer();
                            break;
                    }
                    Log.v("Songstone", keyEvent.toString());
                }
                return false;
            }
        });

        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY)
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();

        mSession.setPlaybackState(state);
        mSession.setActive(true);
    }


}
