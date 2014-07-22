package com.bartoshr.songstone;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SongDialog extends DialogFragment {
	
    private  EditText dialogArtist;
    private  EditText dialogTitle;

    private  Button applyButton;

    private Tagger tag;
	
	 static SongDialog newInstance(int num) {
	        SongDialog f = new SongDialog();
	        return f;
	    }
	 
	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);

	    }


    public void setTag(Tagger tag) { this. tag = tag; }
	 
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.dialog, container, false);
	        dialogArtist = (EditText)v.findViewById(R.id.artist);
            dialogArtist.setText(tag.getArtist());

			dialogTitle = (EditText)v.findViewById(R.id.title);
            dialogTitle.setText(tag.getTitle());

            applyButton = (Button)v.findViewById(R.id.applyButton);
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tag.setTitle(dialogTitle.getText().toString());
                    tag.setArtist(dialogArtist.getText().toString());
                    tag.save();
                }
            });
	       
	        return v;
	    }


}
