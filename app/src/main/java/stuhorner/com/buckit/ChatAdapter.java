package stuhorner.com.buckit;

import android.content.Context;
import android.graphics.Typeface;
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

    List<ChatRow> chatRows;
    private Context context;

    public ChatAdapter(List<ChatRow> chatRows, Context context) {
        this.chatRows = chatRows;
        this.context = context;
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
        if (chatRows.get(i).getName() != null)
            itemViewHolder.personName.setText(chatRows.get(i).getName());
        if (chatRows.get(i).getSubtitle() != null)
            itemViewHolder.subtitle.setText(chatRows.get(i).getSubtitle());
        if (chatRows.get(i).getChatIcon() != null)
            itemViewHolder.icon.setImageBitmap(chatRows.get(i).getChatIcon());
        if (chatRows.get(i).isNewMessage()) {
            itemViewHolder.personName.setTypeface(null, Typeface.BOLD);
            itemViewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.accent_color_light));
        }
        else {
            itemViewHolder.personName.setTypeface(null, Typeface.NORMAL);
            itemViewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.text_light));
        }

    }

    @Override
    public int getItemCount() {
        return chatRows.size();
    }

}
