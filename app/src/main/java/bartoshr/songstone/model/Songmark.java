package bartoshr.songstone.model;

import bartoshr.songstone.model.Song;

/**
 * Created by bartosh on 28.11.2015.
 */
public class Songmark {
    public Song song;
    public int position;

        public Songmark(){

        }

        public Songmark(Song song, int position){
            this.song = song;
            this.position = position;
        }


}
