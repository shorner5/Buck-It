package stuhorner.com.buckit;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ItemsViewHolder>{
    private List<String> bucket_items = new LinkedList<>();
    private boolean completed;
    private FragmentManager fm;
    public static class ItemsViewHolder extends RecyclerView.ViewHolder{
        TextView itemName;
        CheckBox checkBox;
        ImageView arrow;

        ItemsViewHolder(View itemView) {
            super(itemView);
            itemName = (TextView)itemView.findViewById(R.id.txt);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            arrow = (ImageView) itemView.findViewById(R.id.arrow);
        }
    }

    RVAdapter(FragmentManager fm, LinkedList<String> bucket_items, boolean completed) {
        this.bucket_items = bucket_items;
        this.fm = fm;
        this.completed = completed;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View  v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemsViewHolder itemViewHolder, final int i) {
        itemViewHolder.itemName.setText(bucket_items.get(i));
        if (completed) {
            itemViewHolder.checkBox.setChecked(true);
            itemViewHolder.arrow.setVisibility(View.INVISIBLE);
        }
        else {
            itemViewHolder.arrow.setVisibility(View.VISIBLE);
            itemViewHolder.checkBox.setChecked(false);
        }
        itemViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Checked check = Checked.newInstance(bucket_items.get(i), checked);
                check.show(fm, "hello");
                if (!completed) {
                    itemViewHolder.checkBox.setChecked(false);
                }
                else {
                    itemViewHolder.checkBox.setChecked(true);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return (bucket_items != null) ? bucket_items.size() : 0;
    }

}
