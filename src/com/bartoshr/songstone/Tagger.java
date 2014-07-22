package com.bartoshr.songstone;

import java.io.IOException;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.ID3v1;

public class Tagger {

	
	private MP3File mp3file;
	private ID3v1 id3v1;
	private AbstractID3v2 id3v2;
	
	
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
		else return "";
		}
	
	public String getAlbum() { 	
		if(mp3file.hasID3v1Tag()) return mp3file.getID3v1Tag().getAlbum();
		else if(mp3file.hasID3v2Tag()) return mp3file.getID3v2Tag().getAlbumTitle();
		else return "";
		}
	
	public String getArtist() { 	
		if(mp3file.hasID3v1Tag()) return mp3file.getID3v1Tag().getArtist();
		else if(mp3file.hasID3v2Tag()) return mp3file.getID3v2Tag().getLeadArtist();
		else return "";
		}
	
	
	public void setTitle(String name)
	{
		if(mp3file.hasID3v1Tag())
		{
		id3v1.setTitle(name);
        mp3file.setID3v1Tag(id3v1);
		}
		
		if(mp3file.hasID3v2Tag())
		{
        id3v2.setSongTitle(name);
		mp3file.setID3v2Tag(id3v2);
		}	
	}
	
	public void setAlbum(String name)
	{
		if(mp3file.hasID3v1Tag())
		{
		id3v1.setAlbum(name);
        mp3file.setID3v1Tag(id3v1);
		}
		
		if(mp3file.hasID3v2Tag())
		{
        id3v2.setAlbumTitle(name);
		mp3file.setID3v2Tag(id3v2);
		}	
	}
	
	public void setArtist(String name)
	{
		if(mp3file.hasID3v1Tag())
		{
		id3v1.setArtist(name);
        mp3file.setID3v1Tag(id3v1);
		}
		
		if(mp3file.hasID3v2Tag())
		{
        id3v2.setLeadArtist(name);
		mp3file.setID3v2Tag(id3v2);
		}	
	}
	
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
