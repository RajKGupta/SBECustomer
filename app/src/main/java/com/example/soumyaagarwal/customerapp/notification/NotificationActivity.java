package com.example.soumyaagarwal.customerapp.notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.example.soumyaagarwal.customerapp.CustomerLogin.CustomerSession;
import com.example.soumyaagarwal.customerapp.Model.Notif;
import com.example.soumyaagarwal.customerapp.R;
import com.example.soumyaagarwal.customerapp.adapter.notification_adapter;
import com.example.soumyaagarwal.customerapp.helper.DividerItemDecoration;
import com.example.soumyaagarwal.customerapp.tablayout.Tabs;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.soumyaagarwal.customerapp.CustomerApp.DBREF;

public class NotificationActivity extends AppCompatActivity{

    RecyclerView recview;
    notification_adapter adapter;
    List<Notif> list = new ArrayList<>();
    Notif notif  = new Notif();
    String Username;
    CustomerSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        session = new CustomerSession(getApplicationContext());
        Username = session.getUsername();

        recview = (RecyclerView) findViewById(R.id.notification_list);
        recview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recview.setItemAnimator(new DefaultItemAnimator());

        adapter = new notification_adapter(list, getApplicationContext());
        recview.setAdapter(adapter);

        preparelist();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Tabs.class);
        startActivity(intent);
        finish();
    }

    private void preparelist()
    {
        final DatabaseReference db = DBREF.child("Notification").child(Username).getRef();
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists()) {
                    notif = dataSnapshot.getValue(Notif.class);
                    list.add(notif);
                    sortNotification();
                    adapter.notifyDataSetChanged();
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
    private void sortNotification() {
        Collections.sort(list, new Comparator<Notif>() {
            @Override
            public int compare(Notif o1, Notif o2) {
                return Long.parseLong(o1.getId()) < Long.parseLong(o2.getId()) ? -1 : 0; // Decreasing Order
            }
        });
    }
}
