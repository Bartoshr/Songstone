package bartoshr.songstone;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.location.GpsStatus;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.balysv.materialripple.MaterialRippleLayout;

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
        final LayoutInflater inflater = LayoutInflater.
                from(viewGroup.getContext());
                //inflate(R.layout.list_item, viewGroup, false);

       // SongViewHolder holder = new SongViewHolder(itemView);
       // holder.vTitle.setTypeface(typeface);

        // holder.vTitle.setTypeface(typeface);
       // holder.cardView.setMinimumHeight(100);

       SongViewHolder holder = new SongViewHolder(MaterialRippleLayout.on(inflater.inflate(R.layout.list_item, viewGroup, false))
                .rippleOverlay(true)
                .rippleAlpha(0.2f)
                .rippleColor(0xFF585858)
                .rippleHover(true)
                .create());

        holder.vTitle.setTypeface(typeface);

        return holder;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView vTitle;
       // protected CardView cardView;


        public SongViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.title);
            //cardView = (CardView) v.findViewById(R.id.card_view);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("Songstone", "Hello listener");
        }
    }
}