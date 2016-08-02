package stuhorner.com.buckit;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import java.util.List;

/**
 * Created by Owner on 7/31/2016.
 */
public class MyTextWatcher implements TextWatcher {
    private String mPrev;
    private EditText mEditText;
    private Context context;
    private List<String> inspire_items;
    private InspireRVAdapter adapter;
    private int textLength = 0;

    public MyTextWatcher(EditText editText, List<String> inspire_items, InspireRVAdapter adapter, Context context){
        mEditText = editText;
        this.inspire_items = inspire_items;
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int count) {
        mPrev = mEditText.getText().toString();
        textLength = count;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        inspire_items.remove(0);
        if (charSequence.length() != 0) {
            inspire_items.add(0, String.format(context.getString(R.string.add_new_buckit), charSequence.toString()));
        }
        if (charSequence.length() == 0) {
            inspire_items.add(0, context.getString(R.string.new_buckit));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d("editable", editable.toString());
        if (textLength > 8) {
            mEditText.setText(mPrev);
        }
    }
}