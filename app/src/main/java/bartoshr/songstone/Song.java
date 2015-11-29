package bartoshr.songstone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Song {

    private String title = "";
    private String path = "";
    private String artist = "";

    public Song(){

    }

    public Song(String title, String path) {
        this.path = path;
        this.title = title;
    }

    public Song(String title, String path, String artist) {
        this.path = path;
        this.title = title;
        this.artist = artist;
    }

    public void setTitle(String title) { this.title = title; }
    public void setPath(String path) { this.path = path; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getTitle() { return this.title; }
    public String getPath() { return this.path; }
    public String getArtist() { return this.artist; }


    @Override
    public boolean equals(Object o) {
        if(o instanceof Song) {
            if(this.title.compareTo(((Song) o).getTitle()) == 0
                    && this.artist.compareTo(((Song) o).getArtist()) == 0) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
