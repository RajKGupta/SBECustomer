package com.example.soumyaagarwal.customerapp.chat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;

import com.example.soumyaagarwal.customerapp.R;
import com.example.soumyaagarwal.customerapp.helper.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.soumyaagarwal.customerapp.CustomerLogin.CustomerSession;
import com.example.soumyaagarwal.customerapp.Model.ChatMessage;
import com.example.soumyaagarwal.customerapp.Model.NameAndStatus;
import com.example.soumyaagarwal.customerapp.adapter.ViewImageAdapter;
import com.example.soumyaagarwal.customerapp.adapter.chatAdapter;
import com.example.soumyaagarwal.customerapp.helper.CompressMe;
import com.example.soumyaagarwal.customerapp.helper.MarshmallowPermissions;
import com.example.soumyaagarwal.customerapp.helper.TouchImageView;
import com.example.soumyaagarwal.customerapp.listener.ClickListener;
import com.example.soumyaagarwal.customerapp.listener.RecyclerTouchListener;
import com.example.soumyaagarwal.customerapp.services.DownloadFileForChatService;
import com.example.soumyaagarwal.customerapp.services.DownloadFileService;
import com.example.soumyaagarwal.customerapp.services.UploadPhotoAndFile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.example.soumyaagarwal.customerapp.CustomerApp.AppName;
import static com.example.soumyaagarwal.customerapp.CustomerApp.DBREF;
import static com.example.soumyaagarwal.customerapp.CustomerApp.formatter;

public class ChatActivity extends AppCompatActivity implements chatAdapter.ChatAdapterListener, View.OnClickListener {
    private EditText typeComment;
    private ImageButton sendButton;
    Intent intent;
    private RecyclerView recyclerView;
    DatabaseReference dbChat, dbOnlineStatus;
    ValueEventListener dbOnlineStatusVle;
    private String otheruserkey, mykey;
    LinearLayoutManager linearLayoutManager;
    private MarshmallowPermissions marshmallowPermissions;
    LinearLayout emptyView;
    private ArrayList<String> mResults;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private chatAdapter mAdapter;
    private ArrayList<ChatMessage> chatList = new ArrayList<>();
    String receiverToken = "nil";
    private ChildEventListener dbChatlistener;
    ImageButton photoattach, docattach;
    public String dbTableKey;
    private CustomerSession session;
    private ArrayList<String> docPaths, photoPaths;
    CompressMe compressMe;
    private AlertDialog viewSelectedImages;
    ViewImageAdapter adapter;
    String num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        marshmallowPermissions = new MarshmallowPermissions(this);
        if(!marshmallowPermissions.checkPermissionForExternalStorage())
        {
            marshmallowPermissions.requestPermissionForExternalStorage();
        }
        if(!marshmallowPermissions.checkPermissionForExternalStorage())
            Toast.makeText(this,"You wont be able to see the images and documents sent and received",Toast.LENGTH_LONG).show();

        compressMe = new CompressMe(this);
        actionModeCallback = new ActionModeCallback();

        intent = getIntent();
        dbTableKey = intent.getStringExtra("dbTableKey");
        otheruserkey = intent.getStringExtra("otheruserkey");

        dbOnlineStatus = DBREF.child("Users").child("Usersessions").child(otheruserkey).getRef();
        dbOnlineStatusVle = dbOnlineStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    NameAndStatus nameAndStatus = dataSnapshot.getValue(NameAndStatus.class);
                    num = nameAndStatus.getNum();
                    getSupportActionBar().setTitle(nameAndStatus.getName());
                    if(nameAndStatus.getOnline())
                    {
                        getSupportActionBar().setSubtitle("Online");
                    }
                    else
                    {
                        getSupportActionBar().setSubtitle("Offline");
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        System.out.println("recevier token chat act oncreate" + getRecivertoken(otheruserkey));

        session = new CustomerSession(this);

        mykey = session.getUsername();
        dbChat = DBREF.child("Chats").child(dbTableKey).child("ChatMessages").getRef();

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        emptyView = (LinearLayout) findViewById(R.id.empty_view);

        typeComment = (EditText) findViewById(R.id.typeComment);
        sendButton = (ImageButton) findViewById(R.id.sendButton);

        photoattach = (ImageButton) findViewById(R.id.photoattach);
        docattach = (ImageButton) findViewById(R.id.docattach);

        photoattach.setOnClickListener(this);
        docattach.setOnClickListener(this);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new chatAdapter(chatList, this, dbTableKey, this);
        recyclerView.setAdapter(mAdapter);
        sendButton.setOnClickListener(this);

        typeComment.setFocusableInTouchMode(true);
        typeComment.setFocusable(true);

        typeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatList.size()>0)
                    recyclerView.scrollToPosition(chatList.size()-1);
            }
        });

        loadData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+ num));
                startActivity(callIntent);
                break;
        }
        return true;
    }

    private String getRecivertoken(String otheruserkey) {
        System.out.println(otheruserkey + "recd token in chat act ");
        DBREF.child("Fcmtokens").child(otheruserkey).child("token").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    receiverToken = dataSnapshot.getValue().toString();
                    System.out.println(dataSnapshot.getValue() + "recd token in chat act " + receiverToken);
                } else {
                    receiverToken = "nil";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return receiverToken;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                    if (photoPaths.size() > 0) {
                        viewSelectedImages = new AlertDialog.Builder(ChatActivity.this)
                                .setView(R.layout.activity_view_selected_image).create();
                        viewSelectedImages.show();

                        final ImageView ImageViewlarge = (ImageView) viewSelectedImages.findViewById(R.id.ImageViewlarge);
                        ImageButton cancel = (ImageButton) viewSelectedImages.findViewById(R.id.cancel);
                        ImageButton canceldone = (ImageButton) viewSelectedImages.findViewById(R.id.canceldone);
                        ImageButton okdone = (ImageButton) viewSelectedImages.findViewById(R.id.okdone);
                        RecyclerView rv = (RecyclerView) viewSelectedImages.findViewById(R.id.viewImages);

                        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                        rv.setLayoutManager(linearLayoutManager);
                        rv.setItemAnimator(new DefaultItemAnimator());
                        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

                        adapter = new ViewImageAdapter(photoPaths, this);
                        rv.setAdapter(adapter);

                        final String[] item = {photoPaths.get(0)};
                        ImageViewlarge.setImageURI(Uri.parse(item[0]));

                        rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                adapter.selectedPosition = position;
                                adapter.notifyDataSetChanged();
                                item[0] = photoPaths.get(position);
                                ImageViewlarge.setImageURI(Uri.parse(item[0]));
                            }

                            @Override
                            public void onLongClick(View view, int position) {

                            }
                        }));

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int i = photoPaths.indexOf(item[0]);
                                if (i == photoPaths.size() - 1)
                                    i = 0;
                                if(photoPaths.size()==1)
                                {
                                    photoPaths.clear();
                                    viewSelectedImages.dismiss();

                                }
                                else {
                                    photoPaths.remove(item[0]);
                                    adapter.selectedPosition = i;
                                    adapter.notifyDataSetChanged();
                                    item[0] = photoPaths.get(i);
                                    ImageViewlarge.setImageURI(Uri.parse(item[0]));
                                }

                            }
                        });

                        canceldone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                photoPaths.clear();
                                viewSelectedImages.dismiss();
                            }
                        });

                        okdone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int i = photoPaths.size();
                                if (i > 0) {
                                    for (String result : photoPaths) {
                                        String l = compressMe.compressImage(result, getApplicationContext());
                                        uploadFile(l, "photo");

                                    }
                                    viewSelectedImages.dismiss();

                                } else {
                                    viewSelectedImages.dismiss();
                                }
                            }
                        });
                    }

                }
                break;
            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    for (String result : docPaths) {
                        uploadFile(result, "doc");
                    }
                }
                break;
        }
    }

    private void uploadFile(String filePath, String type) {
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        long curTime = Calendar.getInstance().getTimeInMillis();
        final long id = curTime;

        ChatMessage cm = new ChatMessage(mykey, otheruserkey, timestamp, type, id + "", "0", "nourl", receiverToken, dbTableKey, 0, filePath, "");
        dbChat.child(String.valueOf(id)).setValue(cm);

        Intent intent = new Intent(this, UploadPhotoAndFile.class);
        intent.putExtra("filePath",filePath);
        intent.putExtra("type",type);
        intent.putExtra("mykey",mykey);
        intent.putExtra("otheruserkey",otheruserkey);
        intent.putExtra("receiverToken",receiverToken);
        intent.putExtra("dbTableKey",dbTableKey);
        intent.putExtra("timestamp",timestamp);
        intent.putExtra("id",id);
        startService(intent);

    }

    public void loadData() {

        dbChatlistener = dbChat.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ChatActivity.this, "No more comments", Toast.LENGTH_SHORT).show();
                } else {
                    ChatMessage comment = dataSnapshot.getValue(ChatMessage.class);
                    if (!comment.getSenderUId().equals(mykey)) {

                        dbChat.child(comment.getId()).child("status").setValue("3");
                        comment.setStatus("3");  // all message status set to read
                    } else {
                        if (comment.getStatus().equals("0"))
                            dbChat.child(comment.getId()).child("status").setValue("1");
                        comment.setStatus("1");  // all message status set to read
                    }

                    chatList.add(comment);
                    sortChatMessages();
                    mAdapter.notifyDataSetChanged();

                    if(chatList.size()>0)
                        recyclerView.scrollToPosition(chatList.size()-1);

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
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbChatlistener != null)
            dbChat.removeEventListener(dbChatlistener);
        if (dbOnlineStatusVle != null)
            dbOnlineStatus.removeEventListener(dbOnlineStatusVle);
        mAdapter.removeListeners();
    }

    ////maintain all the clicks on buttons on this page
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendButton:
                if (receiverToken.matches("nil")) {
                    getRecivertoken(otheruserkey);
                    System.out.println("calling receiver token from send message" + receiverToken);
                }
                String commentString = typeComment.getText().toString().trim();
                if (TextUtils.isEmpty(commentString)) {
                    Toast.makeText(ChatActivity.this, "What?? No Comment!!", Toast.LENGTH_SHORT).show();
                } else {
                    long curTime = Calendar.getInstance().getTimeInMillis();
                    long id = curTime;
                    String timestamp = formatter.format(Calendar.getInstance().getTime());
                    System.out.println(commentString + "time stamp" + timestamp);
                    ChatMessage cm = new ChatMessage(mykey, otheruserkey, timestamp, "text", id + "", "0", commentString, receiverToken, dbTableKey);
                    dbChat.child(String.valueOf(id)).setValue(cm);
                    DBREF.child("Chats").child(dbTableKey).child("lastMsg").setValue(id);
                    typeComment.setText("");

                }


                break;

            case R.id.photoattach:
                mResults = new ArrayList<>();
                if (!marshmallowPermissions.checkPermissionForCamera())
                    marshmallowPermissions.requestPermissionForCamera();
                if (!marshmallowPermissions.checkPermissionForExternalStorage())
                    marshmallowPermissions.requestPermissionForExternalStorage();

                if (marshmallowPermissions.checkPermissionForCamera() && marshmallowPermissions.checkPermissionForExternalStorage()) {
                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(this);
                }
                break;

            case R.id.docattach:

                if (!marshmallowPermissions.checkPermissionForExternalStorage())
                    marshmallowPermissions.requestPermissionForExternalStorage();

                if (marshmallowPermissions.checkPermissionForExternalStorage()) {

                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickFile(this);
                }
                break;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    ///////////Everything below is for action mode
    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action, menu);

            // disable swipe refresh if action mode is enabled
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void deleteMessages() {
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }

    }

    @Override
    public void onMessageRowClicked(int position) {
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            ChatMessage comment = chatList.get(position);
            String type = comment.getType();
            String uri;
            if (comment.getSenderUId().equals(session.getUsername())) {
                uri = comment.getMesenderlocal_storage();
            } else {
                uri = comment.getOthersenderlocal_storage();
            }
            switch (type) {
                case "photo":
                    viewSelectedImages = new AlertDialog.Builder(ChatActivity.this)
                            .setView(R.layout.viewchatimage).create();
                    viewSelectedImages.show();

                    TouchImageView viewchatimage = (TouchImageView) viewSelectedImages.findViewById(R.id.chatimage);
                    ImageButton backbutton = (ImageButton) viewSelectedImages.findViewById(R.id.back);

                    viewchatimage.setImageURI(Uri.parse(uri));

                    backbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewSelectedImages.dismiss();
                        }
                    });
                    break;
                case "doc":
                    File file = new File(uri);
                    if (file.exists()) {

                        Uri pdfPath = Uri.fromFile(file);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(pdfPath, "application/pdf");

                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            //if user doesn't have pdf reader instructing to download a pdf reader
                        }

                    }
                    break;
            }
        }
    }

    @Override
    public void onRowLongClicked(int position) {
        enableActionMode(position);
    }

    @Override
    public void download_chatimageClicked(final int position, final chatAdapter.MyViewHolder holder)
    {
        mAdapter.showProgressBar(holder);
        final ChatMessage comment = chatList.get(position);
        String type = comment.getType();

        Intent serviceIntent = new Intent(getApplicationContext(), DownloadFileForChatService.class);
        serviceIntent.putExtra("type", type);
        serviceIntent.putExtra("url", comment.getImgurl());
        serviceIntent.putExtra("dbTableKey", dbTableKey);
        serviceIntent.putExtra("Id", comment.getId());
        startService(serviceIntent);

    }
    private void sortChatMessages() {
        Collections.sort(chatList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return Long.parseLong(o1.getId()) < Long.parseLong(o2.getId()) ? -1 : 0; // Decreasing Order
            }
        });
    }
}

/*chat activity  //cancel.    //ImageButton
task detail
create task

upload file function in chat activity "photo" =  type

.pdf
.jpg

add notification to uploadfileandphotos && "photo", "doc" = ***type
add service to download photos and document.    .jpg problem(file format)

dont show download button until imgurl = "nourl"

change pdf placeholder

download file service //onprogress //delete the code here
                      //remove task id from here.

chat activity ...... downloadimageclicked    intent to service
*/