package stuhorner.com.buckit;

import android.location.Location;

/**
 * Created by Stu on 8/6/2016.
 */
public interface LocationReceiver {
    void initData(Location location);
    void permissionDenied();
}
