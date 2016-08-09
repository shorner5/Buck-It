package stuhorner.com.buckit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class NearMeAdapter extends RecyclerView.Adapter<NearMeAdapter.ItemsViewHolder>  {

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;
        ImageView divider;
        TextView item1;
        TextView item2;

        ItemsViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            img = (ImageView) itemView.findViewById(R.id.img);
            divider = (ImageView) itemView.findViewById(R.id.item0);
            item1 = (TextView) itemView.findViewById(R.id.item1);
            item2 = (TextView) itemView.findViewById(R.id.item2);
        }
    }
    private LinkedList<NearMeHolder> users;
    Context context;

    NearMeAdapter(Context context, LinkedList<NearMeHolder> users) {
        this.users = users;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.near_me_item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemsViewHolder itemViewHolder, int i) {
        itemViewHolder.name.setText(users.get(i).getName());
        itemViewHolder.img.setImageBitmap(users.get(i).getImg());
        if (!users.get(i).getItems().isEmpty()) {
            itemViewHolder.item1.setText(users.get(i).getItems().get(0));
            itemViewHolder.item1.setVisibility(View.VISIBLE);
            if (users.get(i).getItems().size() > 1) {
                itemViewHolder.item2.setText(users.get(i).getItems().get(1));
                itemViewHolder.item2.setVisibility(View.VISIBLE);
            }
            else {
                itemViewHolder.item2.setVisibility(View.GONE);
            }
            itemViewHolder.divider.setVisibility(View.VISIBLE);
        }
        else {
            itemViewHolder.item1.setVisibility(View.GONE);
            itemViewHolder.item2.setVisibility(View.GONE);
            itemViewHolder.divider.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

}
