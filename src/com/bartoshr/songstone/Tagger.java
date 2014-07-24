
package com.bartoshr.songstone;

import java.io.IOException;
import java.util.Iterator;

import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagConstant;
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


    private MP3File mp3file;
    private ID3v1 id3v1;
    private AbstractID3v2 id3v2;

    // Advanced shit
    AbstractID3v2Frame frame;
    AbstractID3v2FrameBody frameBody;

    private final String empty = "";

    final String TITLE = "TIT2";
    final String ARTIST = "TPE1";
    final String ALBUM = "TALB";

    public Tagger(String path) {
        try {
            mp3file = new MP3File(path);

            id3v1 = mp3file.getID3v1Tag();
            id3v2 = mp3file.getID3v2Tag();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        if(mp3file.hasID3v1Tag()) return mp3file.getID3v1Tag().getTitle();
        else if(mp3file.hasID3v2Tag()) return mp3file.getID3v2Tag().getSongTitle();
        else return empty;
    }

    public String getAlbum() {
        if(mp3file.hasID3v1Tag()) return mp3file.getID3v1Tag().getAlbum();
        else if(mp3file.hasID3v2Tag()) return mp3file.getID3v2Tag().getAlbumTitle();
        else return empty;
    }

    public String getArtist() {
        if(mp3file.hasID3v1Tag()) return mp3file.getID3v1Tag().getArtist();
        else if(mp3file.hasID3v2Tag()) return mp3file.getID3v2Tag().getLeadArtist();
        else return empty;
    }


    public void setTitle(String name)
    {
        if (id3v1 == null) id3v1 = new ID3v1();
        id3v1.setTitle(name);
        mp3file.setID3v1Tag(id3v1);


        System.out.println((id3v1 == null));

        if(mp3file.hasID3v2Tag()) {

            if(hasTitleFrame()) {
                editTitleFrame(name);
                mp3file.setID3v2Tag(id3v2);
            }

            else {
                setTitleFrame(name);
            }
        }

        if (!mp3file.hasID3v1Tag() && !mp3file.hasID3v2Tag())
        {
            setTitleFrame(name);
        }
    }

    public void setAlbum(String name)
    {
        if (id3v1 == null) id3v1 = new ID3v1();
        id3v1.setAlbum(name);
        mp3file.setID3v1Tag(id3v1);


        if(mp3file.hasID3v2Tag())
        {
            if(hasAlbumFrame()){
                editAlbumFrame(name);
                mp3file.setID3v2Tag(id3v2);
            }

            else {
                setAlbumFrame(name);
            }
        }

        if (!mp3file.hasID3v1Tag() && !mp3file.hasID3v2Tag())
        {
            setAlbumFrame(name);
        }
    }

    public void setArtist(String name)
    {
        if (id3v1 == null) id3v1 = new ID3v1();
        id3v1.setArtist(name);
        mp3file.setID3v1Tag(id3v1);


        if(mp3file.hasID3v2Tag()) {

            if(hasArtistFrame()) {
                editArtistFrame(name);
                mp3file.setID3v2Tag(id3v2);
            }

            else {
                setArtistFrame(name);
            }
        }

        if (!mp3file.hasID3v1Tag() && !mp3file.hasID3v2Tag())
        {
            setArtistFrame(name);
        }
    }

    private void editTitleFrame(String name)
    {
        frame = id3v2.getFrame(TITLE);
        //byte encoding = ((FrameBodyTIT2) frame.getBody()).getTextEncoding();
        ((FrameBodyTIT2) frame.getBody()).setTextEncoding((byte) 0);
        ((FrameBodyTIT2) frame.getBody()).setText(name);
    }

    private void editArtistFrame(String name)
    {
        frame = id3v2.getFrame(ARTIST);
        //byte encoding = ((FrameBodyTIT2) frame.getBody()).getTextEncoding();
        ((FrameBodyTPE1) frame.getBody()).setTextEncoding((byte) 0);
        ((FrameBodyTPE1) frame.getBody()).setText(name);
    }

    private void editAlbumFrame(String name)
    {
        frame = id3v2.getFrame(ALBUM);
        //byte encoding = ((FrameBodyTIT2) frame.getBody()).getTextEncoding();
        ((FrameBodyTALB) frame.getBody()).setTextEncoding((byte) 0);
        ((FrameBodyTALB) frame.getBody()).setText(name);
    }

    private void setAlbumFrame(String name)
    {
        System.out.print("SET NEW ALBUM_FRAME");
        frameBody = new FrameBodyTALB((byte) 0, name);
        frame = new ID3v2_4Frame(frameBody);
        id3v2.setFrame(frame);
    }

    private void setArtistFrame(String name)
    {
        System.out.print("SET NEW ARTIST_FRAME");
        frameBody = new FrameBodyTPE1((byte) 0, name);
        frame = new ID3v2_4Frame(frameBody);
        id3v2.setFrame(frame);
    }

    private void setTitleFrame(String name)
    {
        System.out.print("SET NEW TITLE_FRAME");
        frameBody = new FrameBodyTIT2((byte) 0, name);
        frame = new ID3v2_4Frame(frameBody);
        id3v2.setFrame(frame);
    }

    public boolean hasTitleFrame() { return id3v2.hasFrame(TITLE); }

    public boolean hasArtistFrame(){ return id3v2.hasFrame(ARTIST); }

    public boolean hasAlbumFrame() { return id3v2.hasFrame(ALBUM); }


    public void printTags()
    {
        if(mp3file.hasID3v1Tag())
        {
            System.out.println(mp3file.getID3v1Tag().toString());
        }
        if(mp3file.hasID3v2Tag())
        {
            System.out.println(mp3file.getID3v2Tag().toString());
        }
        if(mp3file.hasLyrics3Tag())
        {
            System.out.println(mp3file.getLyrics3Tag().toString());
        }
    }


    public byte getEncoding(String frame)
    {
        String mark = getObject(frame, "Text Encoding").toString();
        int i = Integer.parseInt(mark);
        return (byte) i;
    }


    public AbstractMP3Object getObject(String frame, String identifier) {
        AbstractMP3Object object = null;
        if(id3v2 != null && id3v2.hasFrameOfType(frame))
        {
            final Iterator iterator = id3v2.getFrame(frame).getBody().iterator();
            while (iterator.hasNext()) {
                final AbstractMP3Object abstractMP3Object = (AbstractMP3Object) iterator.next();
                final String currentIdentifier = abstractMP3Object.getIdentifier();
                if (currentIdentifier.equals(identifier)) {
                    object = abstractMP3Object;
                }

            }
        }
        return object;

    }


    public void save()
    {
        try {
            mp3file.save();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TagException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
