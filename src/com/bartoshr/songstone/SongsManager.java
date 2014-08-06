package com.bartoshr.songstone;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
 
public class SongsManager {

    //final String MEDIA_PATH = new String("/sdcard/");
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
                        	
                        	 String fullpath = cursor.getString(cursor
                                     .getColumnIndex(MediaStore.Audio.Media.DATA));
                        	 
                        	if((new File(fullpath)).exists()) {
                            String songname = cursor
                                    .getString(cursor
                                            .getColumnIndex(MediaStore.Audio.Media.TITLE));

                            String artist = cursor
                                    .getString(cursor
                                             .getColumnIndex(MediaStore.Audio.Media.ARTIST));

                            songname = songname.replaceAll("\\(.*?\\)", "");
                            songname = songname.replaceAll("\\W*\\d+( |. |.)", "");
                           // songname = songname.substring(0, songname.length()-3);
                           
                            Main.Log(" MEDIA DATA"+fullpath);
                            
                            Song song = new Song(songname, fullpath, artist);
                            
                            // Adding each song to SongList
                            songsList.add(song);
                        	}
                            

                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                }
            }
            
            Collections.sort(songsList, new SongComparator());
            
            return songsList;
        }
    
	public class SongComparator implements Comparator<Song> {
	    @Override
	    public int compare(Song x, Song y) {
	        if((int)x.getTitle().charAt(0) > (int)y.getTitle().charAt(0)) {
	        	return 1;
	        } else if ((int)x.getTitle().charAt(0) < (int)y.getTitle().charAt(0)) {
	        	return -1;
	    } else {
	    	return 0;
	    }
	    }
	}


    public static boolean isSdPresent() 
    {
        return android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
    }
 
}