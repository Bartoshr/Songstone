package bartoshr.songstone.adapters;

/**
 * Created by bartosh on 27.11.2015.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bartoshr.songstone.R;

public class MenusAdapter extends  RecyclerView.Adapter<MenusAdapter.ItemViewHolder> {

    private String names[];
    private int glyphs[];

    OnItemClickListener clickListener;

    public MenusAdapter(String names[], int glyphs[], OnItemClickListener clickListener){
        this.names = names;
        this.glyphs = glyphs;
        this.clickListener = clickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.
                from(viewGroup.getContext());
        View contentView = inflater.inflate(R.layout.menu_item, viewGroup, false);
        ItemViewHolder holder = new ItemViewHolder(contentView);
        holder.clickListener = clickListener;
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
        String name = names[i];
        int glyph = glyphs[i];
        itemViewHolder.nameView.setText(name);
        itemViewHolder.nameView.setCompoundDrawablesWithIntrinsicBounds(glyph, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public interface OnItemClickListener {
        public void onMenuItemClick(int position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameView;
        private OnItemClickListener clickListener;

        public ItemViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.nameView);
            nameView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onMenuItemClick(getAdapterPosition());
        }
    }
}
