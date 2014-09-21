
package com.bartoshr.songstone;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.AbstractID3v2FrameBody;
import org.farng.mp3.id3.FrameBodyTALB;
import org.farng.mp3.id3.FrameBodyTIT2;
import org.farng.mp3.id3.FrameBodyTPE1;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v2_4Frame;
import org.farng.mp3.object.AbstractMP3Object;

public class Tagger {

    private final String empty = "";

    String fileName;
    String filePath;
    File file;

    String title;
    String artist;
    String album;

    public Tagger(String path) {

        file = new File(path);
        fileName = file.getName();

        String absolutePath = file.getAbsolutePath();
        filePath = absolutePath.
                substring(0,absolutePath.lastIndexOf(File.separator));

        title = fileName;
        artist = "Songstone";
        if(fileName.matches(".*-.*"))
        {
            String[] array = fileName.split("-");
            title = array[1];
            artist = array[0];
        }
        title = title.substring(0,title.length()-4);

    }


    public Tagger(File path) {

        file = path;
        fileName = file.getName();

        title = fileName;
        artist = "Songstone";
        if(fileName.matches(".*-.*"))
        {
            String[] array = fileName.split("-");
            title = array[1];
            artist = array[0];
        }
        title = title.substring(0,title.length()-4);
    }

    public String getTitle() { return title; }

    public String getAlbum() { return album; }

    public String getArtist() { return artist; }


    public void setTitle(String name)
    {
       title = name;
    }

    public void setAlbum(String name)
    {
       album = name;
    }

    public void setArtist(String name)
    {
       artist = name;
    }



    public void save() {
       // Log.i("Aperture", filePath+File.separator+artist+" - "+title);
        file.renameTo(new File(filePath+File.separator+artist+" - "+title+".mp3"));
    }

}
