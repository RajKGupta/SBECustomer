package com.example.soumyaagarwal.customerapp;

import com.example.soumyaagarwal.customerapp.CheckInternetConnectivity.NetWatcher;
import com.example.soumyaagarwal.customerapp.CustomerLogin.CustomerSession;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by SoumyaAgarwal on 7/11/2017.
 */

public class CustomerApp extends android.support.multidex.MultiDexApplication {
    private static CustomerApp mInstance;
    public static DatabaseReference DBREF;
    private CustomerSession session;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Fresco.initialize(getApplicationContext());

        if(!FirebaseApp.getApps(this).isEmpty()){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        DBREF = FirebaseDatabase.getInstance().getReference().child("MeChat").getRef();
        session = new CustomerSession(this);
        String userkey = session.getUsername();
        setOnlineStatus(userkey);

        Fresco.initialize(getApplicationContext());

        }
        public static synchronized CustomerApp getInstance() {
            return mInstance;
        }

        public void setConnectivityListener(NetWatcher.ConnectivityReceiverListener listener) {
            NetWatcher.connectivityReceiverListener = listener;
        }

    public static void setOnlineStatus(String userkey)
    {
        if(!userkey.equals("")){
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = DBREF.child("Users").child("Usersessions").child(userkey).child("online").getRef();

// stores the timestamp of my last disconnect (the last time I was seen online)
//            final DatabaseReference lastOnlineRef = database.getReference().child("Users").child("Usersessions").child(userkey).child("lastseen").getRef();

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        myConnectionsRef.setValue(Boolean.TRUE);
                        myConnectionsRef.onDisconnect().setValue(Boolean.FALSE);

                        // when I disconnect, update the last time I was seen online
//                        lastOnlineRef.onDisconnect().setValue(Calendar.getInstance().getTime()+"");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });
        }

    }
}