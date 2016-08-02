package stuhorner.com.buckit;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 12/14/2015.
 */
public class Social extends Fragment {
    List<String> social_string;
    List<Integer> social_images;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.social, container,false);
        initData();

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.social_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SocialRVAdapter mAdapter = new SocialRVAdapter(social_string, social_images, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    private void initData(){
        social_string = new ArrayList<>();
        social_images = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            social_string.add("NAME and NAME completed TASK");
            social_images.add(R.drawable.ye);
        }
    }
}
