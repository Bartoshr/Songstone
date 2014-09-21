package com.bartoshr.songstone;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class SongsFinder {

    private Tagger tag;

    ArrayList<Song> songs;
    ArrayList<File> folders;
    ArrayList<File> files;

    public SongsFinder(String directory) {

        folders = listFolders(new File(directory));
        folders.add(new File(directory));

        files = listFiles();
        songs = listSongs();
    }


    public ArrayList<File> listFolders(File dir)
    {
        File[] matches = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.isHidden() && "Android".compareTo(file.getName()) != 0;
            }
        });
        ArrayList<File> result = new ArrayList<File>();

        if(matches != null) {
            result = new ArrayList<File>(Arrays.asList(matches));

            for (File file : matches) {
                Log("Folder name: " + file.getName());
                listFolders(file);
                result.addAll(listFolders(file));
            }
        }

        return result;
    }

    public ArrayList<File> listFiles()
    {
        File[] matches;
        ArrayList<File> result = new ArrayList<File>();

        for(File file : folders)
        {
            matches = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() &&
                            (file.getName().endsWith("mp3") || file.getName().endsWith("MP3") || file.getName().endsWith("wav"));
                }
            });

            result.addAll(Arrays.asList(matches));

        }
        return result;
    }


    public ArrayList<Song> listSongs()
    {
        ArrayList<Song> result = new ArrayList<Song>();

        for(File file : files)
        {

            String fileName = file.getName();
            fileName = fileName.replaceAll("\\(.*?\\)", "");
            fileName = fileName.replaceAll("\\W*\\d+( |. |.)", "");
            fileName = fileName.substring(0,fileName.length()-4);


            String title = fileName;
            String artist = "Songstone";
            if(fileName.matches(".*-.*"))
            {
                String[] array = fileName.split("-");
                title = array[1];
                artist = array[0];
            }
            title = title.trim();
            artist = artist.trim();

            Log("Processign "+title);



            String path = file.getPath();

            result.add(new Song(title ,path, artist));
        }

        Collections.sort(result, new SongComparator());

        return result;
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


    public void Log(String str)
    {
        Log.i("Aperture", str);
    }


}
