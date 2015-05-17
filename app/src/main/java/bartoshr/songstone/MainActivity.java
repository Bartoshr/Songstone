package bartoshr.songstone;

import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String PANEL_FRAGMENT_TAG = "panel_fragment";

    Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    String songDirecory = "/storage/sdcard0/Music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongsFinder finder = new SongsFinder(songDirecory);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.songsview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SongAdapter(this , createList(120));
        mRecyclerView.setAdapter(mAdapter);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startFilePicker();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void togglePanel(){

        Fragment f = getFragmentManager().findFragmentByTag(PANEL_FRAGMENT_TAG);


        if (f != null) {
            getFragmentManager().popBackStack();
        } else {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.slide_up,
                            R.animator.slide_down,
                            R.animator.slide_up,
                            R.animator.slide_down)
                    .add(R.id.screenLayout, PanelFragment
                                    .instantiate(this, PanelFragment.class.getName()),
                            PANEL_FRAGMENT_TAG
                    ).addToBackStack(null).commit();
        }
    }

    public void startFilePicker(){
        // This always works
        Intent i = new Intent(this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory like so
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, "/");

        startActivityForResult(i, 5);
    }

    private List<Song> createList(int size) {

        List<Song> result = new ArrayList<Song>();
        for (int i=0; i < Cheeses.sCheeseStrings.length; i++) {
            Song ci = new Song(Cheeses.sCheeseStrings[i],"");
            result.add(ci);
        }

        return result;
    }
}
