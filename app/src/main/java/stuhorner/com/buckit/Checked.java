package stuhorner.com.buckit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


/**
 * Created by Stu on 12/14/2015.
 */
public class Checked extends DialogFragment {
    String match_item;
    EditText editText;
    ImageButton addPhoto, share;
    Button post, addPerson;
    int position;
    private final static int GALLERY_REQUEST = 1;

    //Firebase references
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference socialRef = FirebaseDatabase.getInstance().getReference("social/" + mUser.getUid());
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + mUser.getUid());


    static Checked newInstance(String match_item, int position){
        Checked c = new Checked();
        Bundle args = new Bundle();
        args.putString("title", match_item);
        args.putInt("position", position);
        c.setArguments(args);
        return c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.checked, new LinearLayout(getActivity()), false);
        this.match_item = getArguments().getString("title");
        this.position = getArguments().getInt("position");

        Dialog builder = new Dialog(getActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);

        editText = (EditText) view.findViewById(R.id.checked_edit_text);
        editText.setLines(10);
        editText.setHorizontallyScrolling(false);
        addPhoto = (ImageButton) view.findViewById(R.id.checked_photo);
        post = (Button) view.findViewById(R.id.checked_post);
        addPerson = (Button) view.findViewById(R.id.checked_subtitle);
        share = (ImageButton) view.findViewById(R.id.checked_share);
        handleButtons();
        handleEditText();
        addPerson.setText(getResources().getQuantityString(R.plurals.completed_text, 2, "Stu", "Kanye", match_item));

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
                if (isStoragePermissionGranted()) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                }
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socialRef.child(addPerson.getText().toString()).child("time").setValue(ServerValue.TIMESTAMP);
                socialRef.child(addPerson.getText().toString()).child("text").setValue(editText.getText().toString());
                userRef.child("buckits").child(Integer.toString(position)).setValue(null);

                dismiss();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(getString(R.string.share_text), addPerson.getText().toString()));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        addPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("storage","Permission is granted");
                return true;
            } else {
                Log.v("storage","Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("storage","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case GALLERY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                } else {
                    Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            //upload image
            Uri selectedImage = data.getData();
            BitmapUploadTask task = new BitmapUploadTask(getPathFromURI(selectedImage), "social/" + mUser.getUid() + "/" + addPerson.getText().toString() + "/img");
            task.execute();
            addPhoto.setColorFilter(getResources().getColor(R.color.accent_color_light));
        }
    }

    private String getPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getActivity().getContentResolver().query(uri,proj,null,null,null);
            int column_index = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
            if (cursor != null) {
                cursor.moveToFirst();
            }
            return cursor != null ? cursor.getString(column_index) : null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
