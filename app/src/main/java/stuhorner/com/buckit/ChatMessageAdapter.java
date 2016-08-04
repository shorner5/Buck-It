package stuhorner.com.buckit;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

/**
 * Created by Stu on 1/12/2016.
 */
public class ChatMessageAdapter extends ArrayAdapter<Message> {
    private List<Message> messages;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public ChatMessageAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_message, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.body = (TextView)convertView.findViewById(R.id.chat_tvBody);
            convertView.setTag(holder);
        }
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = (messages.get(position).getSender().equals(mUser.getUid()));

        // Display the message text to the right for our user, left for other users.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (isMe) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(200, 0, 25, 0);
            holder.body.setTextColor(Color.BLACK);
            holder.body.setBackgroundResource(R.drawable.chat_message_out);
            holder.body.setLayoutParams(params);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(25, 0, 200, 0);
            holder.body.setTextColor(Color.WHITE);
            holder.body.setBackgroundResource(R.drawable.chat_message_in);
            holder.body.setLayoutParams(params);
        }
        holder.body.setText(messages.get(position).getBody());
        return convertView;
    }
    final class ViewHolder {
        public TextView body;
    }

}