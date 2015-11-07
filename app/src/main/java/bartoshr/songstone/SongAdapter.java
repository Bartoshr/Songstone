package bartoshr.songstone;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.balysv.materialripple.MaterialRippleLayout;

import java.util.List;

/**
 * Created by bartosh on 06.05.15.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    protected Typeface typeface;

    OnItemClickListener clickListener;

    public SongAdapter(Context context, List<Song> songList, OnItemClickListener listener) {
        this.songList = songList;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
        this.clickListener = listener;
    }

    public void changeList(List<Song> songList){
        this.songList = songList;
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

       SongViewHolder holder = new SongViewHolder(MaterialRippleLayout.on(inflater.inflate(R.layout.list_item, viewGroup, false))
                .rippleOverlay(true)
                .rippleAlpha(0.2f)
                .rippleColor(0xFF585858)
                .rippleHover(true)
                .create());

        holder.vTitle.setTypeface(typeface);
        holder.clickListener = clickListener;

        return holder;
    }

    public void remove(int position) {
        songList.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        public void onItemClick(int position);
        public boolean onItemLongClick(int position);
    }


    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected TextView vTitle;
        private OnItemClickListener clickListener;


        public SongViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            return clickListener.onItemLongClick(getAdapterPosition());
        }
    }
}