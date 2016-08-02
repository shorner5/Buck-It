package stuhorner.com.buckit;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileBuckits extends Fragment {
    private List<String> data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_buckits, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.p_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        ProfileBuckitsAdapter adapter;
        data = initData();
        adapter = new ProfileBuckitsAdapter(data);
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Snackbar.make(v, String.format(getResources().getString(R.string.add_confirm), data.get(position)), Snackbar.LENGTH_SHORT).show();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_down_up));
                //TODO: add to buckit buckit_list
            }
        });

        return view;
    }

    private List<String> initData(){
        List<String> bucket_items = new ArrayList<>();
        bucket_items.add("Visit Europe");
        bucket_items.add("Jump off a waterfall");
        bucket_items.add("Run from the police");
        bucket_items.add("Become friends with a wild animal");
        bucket_items.add("Poop in a bucket");
        bucket_items.add("Find Jesus");
        bucket_items.add("Invent a type of toilet paper that dissolves in water but not while wiping.");
        return bucket_items;
    }
}
