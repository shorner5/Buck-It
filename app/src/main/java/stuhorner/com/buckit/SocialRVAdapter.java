package stuhorner.com.buckit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.LinkedList;

/**
 * Created by Stu on 12/28/2015.
 */
public class SocialRVAdapter extends RecyclerView.Adapter<SocialRVAdapter.ItemsViewHolder> {

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title;
        TextView story;
        TextView numLikes;
        ImageButton likeButton;

        ItemsViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
            title = (TextView) itemView.findViewById(R.id.title);
            story = (TextView) itemView.findViewById(R.id.story);
            numLikes = (TextView) itemView.findViewById(R.id.like_number);
            likeButton = (ImageButton) itemView.findViewById(R.id.social_like);
        }
    }
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinkedList<SocialPostHolder> social_items;
    private Context context;

    SocialRVAdapter(Context context, LinkedList<SocialPostHolder> social_items) {
        this.social_items = social_items;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.social_item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemsViewHolder itemViewHolder, int i) {
        itemViewHolder.title.setText(social_items.get(i).getTitle());
        if (social_items.get(i).getImg() != null) {
            itemViewHolder.img.setImageBitmap(social_items.get(i).getImg());
        }
        else {
            itemViewHolder.img.setVisibility(View.GONE);
        }
        if (social_items.get(i).getStory() != null) {
            itemViewHolder.story.setText(social_items.get(i).getStory());
        }
        else {
            itemViewHolder.story.setVisibility(View.GONE);
        }
        if (social_items.get(i).isLiked()) {
            itemViewHolder.likeButton.setImageResource(R.drawable.ic_liked);
            itemViewHolder.likeButton.setColorFilter(context.getResources().getColor(R.color.accent_color_light));
        }
        else {
            itemViewHolder.likeButton.setImageResource(R.drawable.ic_like);
            itemViewHolder.likeButton.clearColorFilter();
        }
        itemViewHolder.numLikes.setText(String.valueOf(social_items.get(i).getLikes()));
        addAnimation(itemViewHolder.likeButton, itemViewHolder, i);
    }

    @Override
    public int getItemCount() {
        return social_items != null ? social_items.size() : 0;
    }

    private void addAnimation(final ImageButton button, final ItemsViewHolder itemsViewHolder, final int i) {
        final Animation scaleDown = AnimationUtils.loadAnimation(this.context, R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(this.context, R.anim.scale_up);
        scaleUp.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUp);
                        handleButtonPress(button, itemsViewHolder, i);
                        break;
                }
                return false;
            }
        });
    }

    private void handleButtonPress(ImageButton button, ItemsViewHolder itemsViewHolder, int i) {
        if (button == itemsViewHolder.likeButton) {
            if (!social_items.get(i).isLiked()) {
                social_items.get(i).setLiked(true);
                rootRef.child("social").child(social_items.get(i).getUID()).child(social_items.get(i).getTitle()).child("likedBy").child(mUser.getUid()).setValue(1);
                rootRef.child("social").child(social_items.get(i).getUID()).child(social_items.get(i).getTitle()).child("likes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData != null) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                // Set value and report transaction success
                                int num_users = mutableData.getValue(Integer.class);
                                mutableData.setValue(++num_users);
                            }
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });
                social_items.get(i).incrementLikes();
                this.notifyDataSetChanged();
            } else {
                rootRef.child("social").child(social_items.get(i).getUID()).child(social_items.get(i).getTitle()).child("likedBy").child(mUser.getUid()).setValue(null);
                social_items.get(i).setLiked(false);
                rootRef.child("social").child(social_items.get(i).getUID()).child(social_items.get(i).getTitle()).child("likes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData != null) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                // Set value and report transaction success
                                int num_users = mutableData.getValue(Integer.class);
                                mutableData.setValue(--num_users);
                            }
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });
                social_items.get(i).decrementLikes();
                this.notifyDataSetChanged();
            }
        }
    }
}