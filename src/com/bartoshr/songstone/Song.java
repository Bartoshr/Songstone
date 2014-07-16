package com.bartoshr.songstone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Song {

	private String title = "";
	private String path = "";
	
	public Song(String title, String path) {
		this.path = path;
		this.title = title;
	}
	
	public void setTitle(String title) { this.title = title; }
	public void setPath(String path) { this.path = path; }
	
	public String getTitle() { return this.title; }
	public String getPath() { return this.path; }
	

	
}
