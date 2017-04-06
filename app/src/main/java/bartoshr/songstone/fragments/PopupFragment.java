package bartoshr.songstone.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import bartoshr.songstone.R;
import bartoshr.songstone.model.Song;
import bartoshr.songstone.activities.MainActivity;


public class PopupFragment extends DialogFragment {

    EditText editArtist;
    EditText editTitle;

    ImageButton acceptButton;
    ImageButton deleteButton;
    ImageButton swapButton;

    Song song;

    int position;

    public PopupFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup, container);

        Bundle bundle = this.getArguments();
        position = bundle.getInt(MainActivity.BUNDLE_POSITION);
        song = MainActivity.finder.songs.get(position);


        editArtist = (EditText) view.findViewById(R.id.popup_artist);
        editArtist.setText(song.getArtist());
        editTitle = (EditText) view.findViewById(R.id.popup_title);
        editTitle.setText(song.getTitle());

        acceptButton = (ImageButton) view.findViewById(R.id.button_accept);
        deleteButton = (ImageButton) view.findViewById(R.id.button_delete);
        swapButton = (ImageButton) view.findViewById(R.id.button_swap);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(song.getPath());

                String absolutePath = file.getAbsolutePath();
                String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));

                song.setArtist(editArtist.getText().toString());
                song.setTitle(editTitle.getText().toString());

                File newFile = new File(filePath+File.separator+song.getArtist()+" - "+song.getTitle()+".mp3");
                song.setPath(newFile.getPath());

                if(file.canWrite()) {
                    file.renameTo(newFile);
                } else {
                    Toast.makeText(getActivity(), "Dont't have permision", Toast.LENGTH_SHORT).show();
                }

                getDialog().dismiss();
                MainActivity.songAdapter.notifyItemChanged(position);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(song.getPath());

                Toast.makeText(getActivity(), song.getPath().toString(), Toast.LENGTH_LONG).show();

                if(file.canWrite()) {
                    file.delete();
                } else {
                    Toast.makeText(getActivity(), "Dont't have permision", Toast.LENGTH_SHORT).show();
                }

                getDialog().dismiss();
                MainActivity.songAdapter.remove(position);
            }
        });

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String helper = editArtist.getText().toString();
                editArtist.setText(editTitle.getText().toString());
                editTitle.setText(helper);
            }
        });

        return view;
    }
}
