package stuhorner.com.buckit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemsViewHolder>{

    public static class ItemsViewHolder extends RecyclerView.ViewHolder{
        TextView personName;
        TextView subtitle;
        ImageView icon;

        ItemsViewHolder(View itemView) {
            super(itemView);
            personName = (TextView)itemView.findViewById(R.id.chat_name);
            subtitle = (TextView)itemView.findViewById(R.id.chat_subtitle);
            icon = (ImageView) itemView.findViewById(R.id.chat_icon);
        }
    }

    List<String> personNames;
    List<String> subtitles;
    List<Integer> chatIcons;

    public ChatAdapter(List<String> personNames, List<String> subtitles, List<Integer> chatIcons) {
        this.personNames = personNames;
        this.subtitles = subtitles;
        this.chatIcons = chatIcons;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemsViewHolder itemViewHolder, int i) {
        itemViewHolder.personName.setText(personNames.get(i));
        itemViewHolder.subtitle.setText(subtitles.get(i));
    }

    @Override
    public int getItemCount() {
        return personNames.size();
    }

}
