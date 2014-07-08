package com.bartoshr.songstone;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
 
public class SongsManager {

    final String MEDIA_PATH = new String("/sdcard/");
    private ArrayList<Song> songsList = new ArrayList<Song>();
 
    public SongsManager(){
 
    }
    
    private String[] STAR = { "*" };

    public ArrayList<Song> ListAllSongs(Context context) 
        { 
            Cursor cursor;
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            if (isSdPresent()) {
                
                cursor = context.getContentResolver().query(uri, STAR, selection, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                        	
                            String songname = cursor
                                    .getString(cursor
                                            .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                            
                            String fullpath = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DATA));
                            
                            Song song = new Song(songname.substring(0, songname.length()-3), fullpath);
                            
                            // Adding each song to SongList
                            songsList.add(song);
                            

                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                }
            }
            return songsList;
        }
    


    public static boolean isSdPresent() 
    {
        return android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
    }
 
}