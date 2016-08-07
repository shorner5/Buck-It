package stuhorner.com.buckit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Stu on 12/14/2015.
 */
public class Checked extends DialogFragment {
    private String match_item;
    private EditText editText;
    private ImageButton addPhoto, share;
    private Button post;
    private TextView title;
    private View view;
    private final static int GALLERY_REQUEST = 1;
    private final static int PROFILE_CREATED_REQUEST = 2;

    //Firebase references
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference socialRef = FirebaseDatabase.getInstance().getReference("social/" + mUser.getUid());
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + mUser.getUid());

    static Checked newInstance(String match_item, boolean checked){
        Checked c = new Checked();
        Bundle args = new Bundle();
        args.putString("title", match_item);
        args.putBoolean("checked", checked);
        c.setArguments(args);
        return c;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.checked, new LinearLayout(getActivity()), false);
        this.match_item = getArguments().getString("title");
        boolean checked = getArguments().getBoolean("checked");
        Dialog builder = new Dialog(getActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);

        SharedPreferences pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        if (!pref.getBoolean("profile_created", false)) {
            Intent intent = new Intent(getActivity(), CreateProfileActivity.class);
            startActivityForResult(intent, PROFILE_CREATED_REQUEST);
        }
        else if (checked) {
            initDialog();
        }
        else {
            userRef.child("buckits").child(match_item).setValue(1);
            userRef.child("completed").child(match_item).setValue(null);
            dismiss();
        }
        return builder;
    }

    private void initDialog() {
        editText = (EditText) view.findViewById(R.id.checked_edit_text);
        editText.setLines(10);
        editText.setHorizontallyScrolling(false);
        addPhoto = (ImageButton) view.findViewById(R.id.checked_photo);
        post = (Button) view.findViewById(R.id.checked_post);
        title = (TextView) view.findViewById(R.id.checked_subtitle);
        share = (ImageButton) view.findViewById(R.id.checked_share);
        handleButtons();
        handleEditText();
        getName();

        userRef.child("buckits").child(match_item).setValue(null);
        userRef.child("completed").child(match_item).setValue(1);

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
                post();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(getString(R.string.share_text), title.getText().toString()));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

    private void post() {
        socialRef.child(title.getText().toString()).child("story").setValue(editText.getText().toString());
        socialRef.child(title.getText().toString()).child("time").setValue(ServerValue.TIMESTAMP);
        socialRef.child(title.getText().toString()).child("likes").setValue(0);
        socialRef.child(title.getText().toString()).child("likedBy").setValue(null);
        dismiss();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("storage","Permission is granted");
                return true;
            } else {
                Log.v("storage","Permission is revoked");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
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
            BitmapUploadTask task = new BitmapUploadTask(getPathFromURI(selectedImage), "social/" + mUser.getUid() + "/" + title.getText().toString() + "/img");
            task.execute();
            addPhoto.setColorFilter(getResources().getColor(R.color.accent_color_dark));
        }
        else if (requestCode == PROFILE_CREATED_REQUEST) {
            if (resultCode != Activity.RESULT_CANCELED)
                initDialog();
            else {
                dismiss();
            }
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

    private void getName() {
        SharedPreferences pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        if (pref.getString("name", null) != null) {
            title.setText(String.format(getString(R.string.completed_subtitle), pref.getString("name", null), match_item));
        }
        else {
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        title.setText(String.format(getString(R.string.completed_subtitle), dataSnapshot.getValue().toString(), match_item));
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                        editor.putString("name", dataSnapshot.getValue().toString());
                        editor.apply();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}
