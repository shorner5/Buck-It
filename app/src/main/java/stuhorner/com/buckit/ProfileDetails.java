package stuhorner.com.buckit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileDetails extends Fragment {

    public ProfileDetails(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        TextView textView = (TextView) view.findViewById(R.id.p_text);
        textView.setText(initData());
        textView.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }
    private String initData(){
        return "Lorem ipsum dolor sit amet, eam an sumo erant, mei feugiat aliquando ne, dico graeco audiam ea pri. No mea purto elit clita. Sea in saepe quando. Doctus inermis no per, ornatus volutpat hendrerit quo cu, ea malis saepe offendit pri.\n" +
                "\n" +
                "No sit homero labitur evertitur, mel ei vulputate appellantur. Ex mei stet dolor. Meis verear vulputate usu no. Ad erat sadipscing qui. Eum eius nusquam lucilius in, sanctus invidunt scribentur ei duo.\n" +
                "\n" +
                "Dico definitionem at vis, duo id feugiat fastidii, modo alterum ea mea. Ignota scriptorem qui cu, at posse epicurei usu, sit tation omittam id. Cu quidam pertinax theophrastus quo. Ad solet meliore phaedrum mel, nam iracundia euripidis id, zril aeterno cum in. Vel evertitur theophrastus cu, ius erat error electram ad.";
    }
}
