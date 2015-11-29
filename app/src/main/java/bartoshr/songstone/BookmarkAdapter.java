package bartoshr.songstone;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bartosh on 28.11.2015.
 */
public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ItemViewHolder> {

    private ArrayList<Bookmark> names;

    OnItemClickListener clickListener;

    public BookmarkAdapter(ArrayList<Bookmark> names, OnItemClickListener clickListener){
        this.names = names;
        this.clickListener = clickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.
                from(viewGroup.getContext());
        View contentView = inflater.inflate(R.layout.bookmark_item, viewGroup, false);
        ItemViewHolder holder = new ItemViewHolder(contentView);
        holder.clickListener = clickListener;
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
        Song song = names.get(i).song;
        String name = song.getTitle();
        itemViewHolder.nameView.setText(name);
        itemViewHolder.nameView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_bookmark, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public interface OnItemClickListener {
        public void onBookmarkItemClick(int position);
        public boolean onBookmarkItemLongClick(int position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView nameView;
        private OnItemClickListener clickListener;

        public ItemViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.nameView);
            nameView.setOnClickListener(this);
            nameView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onBookmarkItemClick(getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View v) {
            return clickListener.onBookmarkItemLongClick(getAdapterPosition());
        }
    }
}


