package bartoshr.songstone;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;


public class PopupFragment extends DialogFragment {

    EditText editArtist;
    EditText editTitle;

    Button acceptButton;
    Button deleteButton;
    Button swapButton;

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

        acceptButton = (Button) view.findViewById(R.id.button_accept);
        deleteButton = (Button) view.findViewById(R.id.button_delete);
        swapButton = (Button) view.findViewById(R.id.button_swap);

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

                file.renameTo(newFile);

                getDialog().dismiss();
                MainActivity.adapter.notifyItemChanged(position);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(song.getPath());
                file.delete();
                getDialog().dismiss();
                MainActivity.adapter.remove(position);
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
