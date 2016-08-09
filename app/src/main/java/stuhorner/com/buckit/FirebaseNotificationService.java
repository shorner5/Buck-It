package stuhorner.com.buckit;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Stu on 8/7/2016.
 */
public class FirebaseNotificationService extends Service {
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private HashSet<String> addedUsers = new HashSet<>();
    private SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();
        pref = getSharedPreferences("data", MODE_PRIVATE);
        rootRef.child("users").child(mUser.getUid()).child("social").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    addListener(data.getKey(), false);
                    addedUsers.add(data.getKey());
                    createNewUserListener();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createNewUserListener() {
        rootRef.child("users").child(mUser.getUid()).child("social").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!addedUsers.contains(dataSnapshot.getKey())) {
                    Log.d("onChildAdded", "new user! " + dataSnapshot.getKey());
                    addListener(dataSnapshot.getKey(), true);
                    addedUsers.add(dataSnapshot.getKey());

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    private void addListener(final String UID, final boolean newUser) {
        Log.d("adding listener for", UID);
        rootRef.child("messages").child(mUser.getUid()).child(UID).child("metadata").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("messages onChildAdded", dataSnapshot.getKey());
                if (dataSnapshot.getKey().equals("last_message") && dataSnapshot.child("sender").getValue().equals(UID)
                        && newUser && !checkApp() && pref.getBoolean("messages", true)) {
                    getName(UID, dataSnapshot.child("body").getValue().toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("messages onChildChanged", dataSnapshot.getKey());
                if (dataSnapshot.getKey().equals("last_message") && dataSnapshot.child("sender").getValue().equals(UID)
                        && !checkApp() && pref.getBoolean("messages", true)) {
                    getName(UID, dataSnapshot.child("body").getValue().toString());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("messages onChildMoved", dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getName(final String UID, final String body) {
        rootRef.child("users").child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    makeNotification(UID, dataSnapshot.getValue().toString(), body);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void makeNotification(String UID, String name, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(this, ChatPage.class);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("uid", UID);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatPage.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.mipmap.ic_launcher;
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(name)
                .setContentText(body)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), icon))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(UID.hashCode(), notification);
    }

    public boolean checkApp(){
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        return (componentInfo.getPackageName().equalsIgnoreCase("stuhorner.com.buckit") && pm.isScreenOn());
    }
}
