package stuhorner.com.buckit;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by Stu on 12/14/2015.
 */
public class Checked extends DialogFragment {
    String match_item;
    EditText editText;
    ImageButton addPhoto, share;
    Button post, addPerson;



    static Checked newInstance(String match_item){
        Checked c = new Checked();
        Bundle args = new Bundle();
        args.putString("title", match_item);
        c.setArguments(args);
        return c;
    }

    public Checked(){

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.checked, new LinearLayout(getActivity()), false);
        this.match_item = getArguments().getString("title");

        Dialog builder = new Dialog(getActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);

        TextView title = (TextView) view.findViewById(R.id.checked_title);
        title.setText(R.string.completed_title);
        TextView subtitle = (TextView) view.findViewById(R.id.checked_subtitle);
        subtitle.setText(getResources().getQuantityString(R.plurals.completed_text, 1, "Stu", "Kanye", match_item));

        editText = (EditText) view.findViewById(R.id.checked_edit_text);
        handleEditText();
        addPhoto = (ImageButton) view.findViewById(R.id.checked_photo);
        post = (Button) view.findViewById(R.id.checked_post);
        addPerson = (Button) view.findViewById(R.id.checked_subtitle);
        share = (ImageButton) view.findViewById(R.id.checked_share);
        handleButtons();

        return builder;
    }

    private void handleEditText() {

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // position the text type in the left top corner
                    editText.setGravity(Gravity.START | Gravity.TOP);
                } else {
                    // no text entered. Center the hint text.
                    editText.setGravity(Gravity.CENTER);
                }
            }
        });
    }

    private void handleButtons() {
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        addPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
