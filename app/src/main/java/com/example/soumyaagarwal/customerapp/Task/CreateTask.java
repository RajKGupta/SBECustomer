package com.example.soumyaagarwal.customerapp.Task;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import com.example.soumyaagarwal.customerapp.helper.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.example.soumyaagarwal.customerapp.CustomerLogin.CustomerSession;
import com.example.soumyaagarwal.customerapp.Model.Customer;
import com.example.soumyaagarwal.customerapp.Model.Task;
import com.example.soumyaagarwal.customerapp.R;
import com.example.soumyaagarwal.customerapp.adapter.ViewImageAdapter;
import com.example.soumyaagarwal.customerapp.adapter.taskimagesadapter;
import com.example.soumyaagarwal.customerapp.helper.CompressMe;
import com.example.soumyaagarwal.customerapp.helper.MarshmallowPermissions;
import com.example.soumyaagarwal.customerapp.listener.ClickListener;
import com.example.soumyaagarwal.customerapp.listener.RecyclerTouchListener;
import com.example.soumyaagarwal.customerapp.services.UploadTaskPhotosServices;
import com.example.soumyaagarwal.customerapp.tablayout.Tabs;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.example.soumyaagarwal.customerapp.CustomerApp.DBREF;
import static com.example.soumyaagarwal.customerapp.CustomerApp.sendNotif;
import static com.example.soumyaagarwal.customerapp.CustomerApp.sendNotifToAllCoordinators;

public class CreateTask extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener {
    DatabaseReference dbRef;
    EditText taskName,startDate,endDate,quantity,description,custName;
    RecyclerView desc_photo_grid;
    ImageButton written_desc, photo_desc;
    String customerId,customerName,curdate;
    Button submit_task;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    MarshmallowPermissions marshMallowPermission;
    private ArrayList<String> mResults;
    private AlertDialog descriptionDialog, viewSelectedImages ;
    private ArrayList<String> picUriList = new ArrayList<>();
    private int REQUEST_CODE =1;
    private String desc;
    LinearLayoutManager linearLayoutManager;
    LinearLayoutManager imagegrid;
    ViewImageAdapter adapter;
    taskimagesadapter tadapter;
    CompressMe compressMe;
    CustomerSession customerSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        marshMallowPermission = new MarshmallowPermissions(this);
        compressMe = new CompressMe(this);
        getSupportActionBar().setTitle("Create New Task");
        dbRef= DBREF;
        customerSession = new CustomerSession(this);
        customerName = customerSession.getName();
        customerId = customerSession.getUsername();
        custName = (EditText) findViewById(R.id.custName);
        taskName = (EditText) findViewById(R.id.taskName);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        custName.setText(customerName);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(now);
                MonthAdapter.CalendarDay minDate = new MonthAdapter.CalendarDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(CreateTask.this)
                        .setFirstDayOfWeek(Calendar.SUNDAY)
                        .setDateRange( minDate,null)
                        .setDoneText("Ok")
                        .setCancelText("Cancel").setThemeLight();
                cdp.show(getSupportFragmentManager(), "Select Day, Month and Year.");

            }
        });
        quantity=(EditText) findViewById(R.id.quantity);

        desc_photo_grid = (RecyclerView)findViewById(R.id.desc_photo_grid);
        photo_desc = (ImageButton)findViewById(R.id.photo_desc);
        written_desc = (ImageButton)findViewById(R.id.written_desc);

        description = (EditText) findViewById(R.id.description);

        written_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionDialog = new AlertDialog.Builder(CreateTask.this)
                        .setTitle("Enter Description").setView(R.layout.description_dialog).create();
                descriptionDialog.show();

                Button save_writ_desc;
                final EditText et_desc;

                save_writ_desc = (Button)descriptionDialog.findViewById(R.id.save_writ_desc);
                et_desc = (EditText)descriptionDialog.findViewById(R.id.et_desc);

                save_writ_desc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        desc = et_desc.getText().toString();
                        descriptionDialog.dismiss();
                        if (!desc.equals("")) {
                            description.setVisibility(View.VISIBLE);
                            description.setText(desc);
                        }
                    }
                });
            }
        });

        photo_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!marshMallowPermission.checkPermissionForCamera()&&!marshMallowPermission.checkPermissionForExternalStorage()) {
                    ActivityCompat.requestPermissions(CreateTask.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            2);
                }
                else {
                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(CreateTask.this);
                }
            }
        });

        submit_task = (Button)findViewById(R.id.submit_task);
        Calendar c = Calendar.getInstance();
        curdate = dateFormat.format(c.getTime());
        startDate.setText(curdate);

        submit_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTask();
            }
        });
    }

    void createTask()
    {
        String taskname = taskName.getText().toString().trim();
        String qty = quantity.getText().toString().trim();
        String desc = description.getText().toString().trim();
        String enddate = endDate.getText().toString().trim();
        String startdate= startDate.getText().toString().trim();

        long curTime = Calendar.getInstance().getTimeInMillis();
        curTime = 9999999999999L-curTime;

        String task_id = "task"+curTime;
        if(TextUtils.isEmpty(taskname)||TextUtils.isEmpty(qty)||(TextUtils.isEmpty(desc)&& picUriList.size()==0)||TextUtils.isEmpty(enddate)||TextUtils.isEmpty(startdate))
        {
            Toast.makeText(CreateTask.this,"Fill all the details",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Task newTask = new Task("task" + curTime, taskname, startdate, enddate, qty, desc, customerId, getRandomMaterialColor("400"));
            dbRef.child("Task").child("task" + curTime).setValue(newTask);
            dbRef.child("Customer").child(customerId).child("Task").child("task" + curTime).setValue("pending");

            Toast.makeText(CreateTask.this,"Task Created",Toast.LENGTH_SHORT).show();

            if (picUriList.size()>0) {
                Intent serviceIntent = new Intent(getApplicationContext(), UploadTaskPhotosServices.class);
                serviceIntent.putStringArrayListExtra("picUriList", picUriList);
                serviceIntent.putExtra("taskid", task_id);
                startService(serviceIntent);
                finish();
            }
            String contentforme = "You created a new Job "+taskname;
            sendNotif(customerId,customerId,"createJob",contentforme,task_id);
            String contentforother = customerName +" created new Job "+taskname;
            sendNotifToAllCoordinators(customerId,"createJob",contentforother,task_id);
            Intent intent = new Intent(CreateTask.this, Tabs.class);
            intent.putExtra("page",0);
            startActivity(intent);
            finish();
        }
    }

    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        String day=String.valueOf(dayOfMonth);
        if(dayOfMonth<10)
        {
            day= "0"+String.valueOf(dayOfMonth);
        }
        if(monthOfYear<9)
            endDate.setText(day + "-0" + (monthOfYear + 1) + "-" + year);
        else
            endDate.setText(day + "-" + (monthOfYear + 1) + "-" + year);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (data != null) {
                mResults = new ArrayList<>();
                mResults.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                assert mResults != null;
                System.out.println(String.format("Totally %d images selected:", mResults.size()));
                for (String result : mResults) {
                    String l = compressMe.compressImage(result,getApplicationContext());
                    picUriList.add(l);
                }
                if (picUriList.size() > 0)
                {
                    viewSelectedImages = new AlertDialog.Builder(CreateTask.this)
                            .setView(R.layout.activity_view_selected_image).create();
                    viewSelectedImages.show();

                    final ImageView ImageViewlarge = (ImageView) viewSelectedImages.findViewById(R.id.ImageViewlarge);
                    ImageButton cancel = (ImageButton) viewSelectedImages.findViewById(R.id.cancel);
                    ImageButton canceldone = (ImageButton)viewSelectedImages.findViewById(R.id.canceldone);
                    ImageButton okdone = (ImageButton)viewSelectedImages.findViewById(R.id.okdone);
                    RecyclerView rv = (RecyclerView) viewSelectedImages.findViewById(R.id.viewImages);

                    linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
                    rv.setLayoutManager(linearLayoutManager);
                    rv.setItemAnimator(new DefaultItemAnimator());
                    rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

                    adapter = new ViewImageAdapter(picUriList, this);
                    rv.setAdapter(adapter);

                    final String[] item = {picUriList.get(0)};
                    ImageViewlarge.setImageURI(Uri.parse(item[0]));

                    rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            adapter.selectedPosition = position;
                            adapter.notifyDataSetChanged();
                            item[0] = picUriList.get(position);
                            ImageViewlarge.setImageURI(Uri.parse(item[0]));
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i = picUriList.indexOf(item[0]);
                            if (i == picUriList.size() - 1)
                                i = 0;
                            if(picUriList.size()==1)
                            {
                                picUriList.clear();
                                viewSelectedImages.dismiss();

                            }
                            else {
                                picUriList.remove(item[0]);
                                adapter.selectedPosition = i;
                                adapter.notifyDataSetChanged();
                                item[0] = picUriList.get(i);
                                ImageViewlarge.setImageURI(Uri.parse(item[0]));
                            }
                        }
                    });

                    canceldone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            picUriList.clear();
                            viewSelectedImages.dismiss();
                        }
                    });

                    okdone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int i = picUriList.size();
                            if (i>0) {
                                desc_photo_grid.setVisibility(View.VISIBLE);

                                imagegrid = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                                desc_photo_grid.setLayoutManager(imagegrid);
                                desc_photo_grid.setItemAnimator(new DefaultItemAnimator());
                                desc_photo_grid.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

                                tadapter = new taskimagesadapter(picUriList, getApplicationContext());
                                desc_photo_grid.setAdapter(tadapter);
                                //picUriList.clear();
                                viewSelectedImages.dismiss();

                            } else {
                                picUriList.clear();
                                viewSelectedImages.dismiss();
                            }
                            //onpressing save button dont forget to add this
                            //upload images to storage
                            //on success add informatio to database
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    FilePickerBuilder.getInstance().setMaxCount(10)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(this);
                }
                else
                {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("These permissions are necessary else you cant upload images")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ActivityCompat.requestPermissions(CreateTask.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                            2);
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return;
            }

        }

    }
    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        Intent intent = new Intent(CreateTask.this, Tabs.class);
                        intent.putExtra("id", customerId);
                        startActivity(intent);
                        finish();
                    }


                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


}