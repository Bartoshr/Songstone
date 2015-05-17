package bartoshr.songstone;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by bartosh on 06.05.15.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    protected Typeface typeface;

    public SongAdapter(Context context, List<Song> songList) {
        this.songList = songList;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    @Override
    public void onBindViewHolder(SongViewHolder songViewHolder, int i) {
        Song s = songList.get(i);
        songViewHolder.vTitle.setText(s.getTitle());
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.list_item, viewGroup, false);

        SongViewHolder holder = new SongViewHolder(itemView);
        holder.vTitle.setTypeface(typeface);

        return holder;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;

        public SongViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.title);

        }
    }
}