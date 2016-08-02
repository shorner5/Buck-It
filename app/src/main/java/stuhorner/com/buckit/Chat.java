package stuhorner.com.buckit;

import android.content.Intent;
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
public class Chat extends Fragment {
    List<String> personNames;
    List<String> subtitles;
    List<Integer> chatIcons;
    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initData();
        ChatAdapter adapter = new ChatAdapter(personNames, subtitles, chatIcons);

        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //transition to page activity
                Intent intent = new Intent(getActivity(), ChatPage.class);
                intent.putExtra(PERSON_NAME, personNames.get(position));
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);

            }
        });
        return view;
    }

    private void initData() {
        personNames = new ArrayList<>();
        personNames.add("Stuart Horner");
        personNames.add("Giselle Townsend");
        personNames.add("Eric Roydon");
        personNames.add("Tobias Studwick");
        personNames.add("Wilfreda Trent");
        personNames.add("Patience Ellery");
        personNames.add("Glenda Stringer");
        personNames.add("Neil Baines");
        personNames.add("Don Draper");
        personNames.add("Robert Sterling");
        personNames.add("Erin Wade");
        personNames.add("Amelia Bloxam");
        personNames.add("Sawyer Jameson");

        subtitles = new ArrayList<>();
        subtitles.add("The artist is the creator of beautiful things.  To reveal art and");
        subtitles.add("conceal the artist is art's aim.  The critic is he who can translate");
        subtitles.add("into another manner or a new material his impression of beautiful things.");
        subtitles.add("The highest as the lowest form of criticism is a mode of autobiography.");
        subtitles.add("Those who find ugly meanings in beautiful things are corrupt without being charming");
        subtitles.add("Those who find beautiful meanings in beautiful things are the cultivated.");
        subtitles.add("For these there is hope.  They are the elect to whom beautiful things mean only beauty.");
        subtitles.add("There is no such thing as a moral or an immoral book.  Books are well");
        subtitles.add("The nineteenth century dislike of realism is the rage of Caliban seeing his own face in a glass.");
        subtitles.add("The nineteenth century dislike of romanticism is the rage of Caliban");
        subtitles.add("not seeing his own face in a glass.  The moral life of man forms part");
        subtitles.add("of the subject-matter of the artist, but the morality of art consists");
        subtitles.add("Swagkillas");

        chatIcons = new ArrayList<>();
        for (int i = 0; i < 13; i++){
            chatIcons.add(R.drawable.ic_profile);
        }

    }
}
