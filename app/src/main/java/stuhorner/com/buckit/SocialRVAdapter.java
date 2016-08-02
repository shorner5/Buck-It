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

import java.util.List;

/**
 * Created by Stu on 12/28/2015.
 */
public class SocialRVAdapter  extends RecyclerView.Adapter<SocialRVAdapter.ItemsViewHolder> {

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageName;
        TextView social_string;
        ImageButton likeButton;
        ImageButton addButton;

        ItemsViewHolder(View itemView) {
            super(itemView);
            imageName = (ImageView) itemView.findViewById(R.id.social_image);
            social_string = (TextView) itemView.findViewById(R.id.social_string);
            likeButton = (ImageButton) itemView.findViewById(R.id.social_like);
            addButton = (ImageButton) itemView.findViewById(R.id.social_add);
        }
    }

    List<String> social_string;
    List<Integer> social_image;
    Context context;

    SocialRVAdapter(List<String> social_string, List<Integer> social_image, Context context) {
        this.social_string = social_string;
        this.social_image = social_image;
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
        itemViewHolder.social_string.setText(social_string.get(i));
        itemViewHolder.imageName.setImageResource(social_image.get(i));
        itemViewHolder.addButton.setColorFilter(context.getResources().getColor(R.color.text_light));
        addAnimation(itemViewHolder.likeButton, itemViewHolder);
        addAnimation(itemViewHolder.addButton, itemViewHolder);

    }

    @Override
    public int getItemCount() {
        return social_string.size();
    }

    private void addAnimation(final ImageButton button, final ItemsViewHolder itemsViewHolder) {
        final Animation scaleDownLike = AnimationUtils.loadAnimation(this.context, R.anim.scale_down);
        scaleDownLike.setFillAfter(true);
        final Animation scaleUpLike = AnimationUtils.loadAnimation(this.context, R.anim.scale_up);
        scaleUpLike.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDownLike);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUpLike);
                        handleButtonPress(button, itemsViewHolder);
                        break;
                }
                return false;
            }
        });
    }

    private void handleButtonPress(ImageButton button, ItemsViewHolder itemsViewHolder) {
        if (button == itemsViewHolder.likeButton) {
            button.setImageResource(R.drawable.ic_liked);
            button.setColorFilter(context.getResources().getColor(R.color.accent_color_light));

        } else if (button == itemsViewHolder.addButton) {
            button.setColorFilter(context.getResources().getColor(R.color.accent_color_light));

        }
    }
}