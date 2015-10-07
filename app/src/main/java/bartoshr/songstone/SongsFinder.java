package bartoshr.songstone;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class SongsFinder {

    public ArrayList<Song> songs = new ArrayList<Song>();
    public ArrayList<File> folders = new ArrayList<File>();
    public ArrayList<File> files = new ArrayList<File>();

    public SongsFinder(String directory) {

        try {
            folders = listFolders(new File(directory));
            folders.add(new File(directory));

            files = listFiles();
            songs = listSongs();
        } catch(Exception e) {
            e.printStackTrace();
            Log.e("SongFinder","SongsFinder problem");
        }
    }


    public ArrayList<File> listFolders(File dir) throws IOException
    {
        File[] matches = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file){
                return file.isDirectory() && !isSymlink(file)  && !file.isHidden() && "Android".compareTo(file.getName()) != 0;
            }
        });
        ArrayList<File> result = new ArrayList<File>();

        if(matches != null) {
            result = new ArrayList<File>(Arrays.asList(matches));

            for (File file : matches) {
               // Log("Folder name: " + file.getAbsolutePath());
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
                    return file.isFile() && !file.getName().startsWith(".") &&
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
            //fileName = fileName.substring(0, fileName.length() - 4);
            //fileName = fileName.substring(0,1).toUpperCase()+fileName.substring(1);


            String title = fileName;
            String artist = "Unknown";
            if(fileName.matches(".*-.*"))
            {
                String[] array = fileName.split("-");
                title = array[1];
                artist = array[0];
            }
            title = title.trim();
            artist = artist.trim();

           // Log("Processign "+title);
            
            String path = file.getPath();

            result.add(new Song(title, path, artist));
        }

        Collections.sort(result, new SongComparator());

        return result;
    }

    public String getTitle(int position) {
        return songs.get(position).getTitle();
    }

    public String getArtist(int position) {
        return songs.get(position).getArtist();
    }

    public String getPath(int position) {
        return songs.get(position).getPath();
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

    public static boolean isSymlink(File file) {
        try {

            if (file == null)
                throw new NullPointerException("File must not be null");
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        }catch (IOException e){
            return true ;
        }

    }


}
