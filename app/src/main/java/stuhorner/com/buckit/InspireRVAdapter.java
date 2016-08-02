package stuhorner.com.buckit;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

/**
 * Created by Stu on 12/28/2015.
 */
public class InspireRVAdapter  extends RecyclerView.Adapter<InspireRVAdapter.ItemsViewHolder> {

    public static class ItemsViewHolder extends RecyclerView.ViewHolder{
        TextView itemName;
        CardView cardView;
        ItemsViewHolder(View itemView) {
            super(itemView);
            itemName = (TextView)itemView.findViewById(R.id.inspire_txt);
            cardView = (CardView)itemView.findViewById(R.id.inspire_card);
        }
    }

    List<String> inspire_items;
    Context context;

    InspireRVAdapter(List<String> inspire_items, Context context) {
        this.inspire_items = inspire_items;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View  v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inspire_item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemsViewHolder itemViewHolder, int i) {
        itemViewHolder.itemName.setText(inspire_items.get(i));
        if (inspire_items.get(0).equals(itemViewHolder.itemName.getText())) {
            itemViewHolder.cardView.setBackgroundColor(context.getResources().getColor(R.color.accent_color_light));
            itemViewHolder.itemName.setTextColor(context.getResources().getColor(R.color.primary_color));
        }
    }

    @Override
    public int getItemCount() {
        return inspire_items.size();
    }

}