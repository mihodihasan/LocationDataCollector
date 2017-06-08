package com.droiddigger.lushan.locationdatacollector;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<UserLocation> locations;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    Adapter adapter;
    boolean buttonToggle = false;

    private static final int RC_SIGN_IN = 1;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    FirebaseRecyclerAdapter<UserLocation, FirebaseVH> recyclerAdapter;

    DatabaseReference databaseReferenceRoot;

    Button startBTN;
//    TextView cordinatesTV;

    BroadcastReceiver broadcastReceiver;

    private String mUsername;
    private String ANONYMOUS = "USER";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBTN = (Button) findViewById(R.id.start_btn);
//        stopBTN = (Button) findViewById(R.id.end_btn);
//        cordinatesTV = (TextView) findViewById(R.id.cordinates_tv);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsername = ANONYMOUS;
        databaseReferenceRoot = mFirebaseDatabase.getReference().child("rootNode");
        mMessageDatabseReference = databaseReferenceRoot.child(mUsername);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    //Signed In
                    Toast.makeText(MainActivity.this, "You Are Logged In! Welcome ", Toast.LENGTH_SHORT).show();
                    onSignedInInitialize(user.getDisplayName());
//                    Log.d("useruser", user.getDisplayName().substring(0, user.getEmail().indexOf('@')));
                } else {
                    //Signed out
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(), RC_SIGN_IN);
                }
            }
        };
//        Log.e("checkchecks","");
        if (!runtimePermission()) {
            enableButton();
        }

        locations = new ArrayList<>();
        manager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new Adapter(this, locations);
        recyclerView.setLayoutManager(manager);
//        recyclerView.setAdapter(adapter);
        Log.e("amarbaal", mFirebaseAuth.getCurrentUser().getUid());
        recyclerAdapter = new FirebaseRecyclerAdapter<UserLocation, FirebaseVH>(UserLocation.class, R.layout.list_row,
                FirebaseVH.class, databaseReferenceRoot.child(mFirebaseAuth.getCurrentUser().getUid())) {
            @Override
            protected void populateViewHolder(FirebaseVH viewHolder, UserLocation location, int position) {
                viewHolder.userTV.setText(location.getUsername());
                viewHolder.timeTV.setText(location.getTimeStamp());
                viewHolder.latitudeTV.setText(location.getLatitude());
                viewHolder.longitudeTV.setText(location.getLatitude());
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
        attachDatabaseReadListener();
    }

    private void enableButton() {

        startBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buttonToggle) {
                    Intent intent = new Intent(getApplicationContext(), GPS_Service.class);
                    startService(intent);
                    buttonToggle = true;
                    startBTN.setText("STOP SERVICE");
                    startBTN.setBackgroundColor(Color.RED);
                } else {
                    Intent intent = new Intent(getApplicationContext(), GPS_Service.class);
                    stopService(intent);
                    buttonToggle = false;
                    startBTN.setText("START SERVICE");
                    startBTN.setBackgroundColor(Color.GREEN);
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        locations.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
//                    cordinatesTV.append(intent.getStringExtra("location"));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy-hh-mm-ss");
                    String timeStamp = simpleDateFormat.format(new Date());
//                    String userId=user.getEmail().substring(0,user.getEmail().indexOf('@')).replace(".","-");
                    UserLocation userLocation = new UserLocation(user.getDisplayName(), timeStamp,
                            intent.getStringExtra("location_lat"), intent.getStringExtra("location_long"));
//                    Log.d("firebaseusername", userLocation.getUsername());
                    mMessageDatabseReference = databaseReferenceRoot.child(user.getUid());
                    if (intent.getStringExtra("location_lat") != null || intent.getStringExtra("location_long") != null) {
                        mMessageDatabseReference.push().setValue(userLocation);
//                        Log.e("firebaseuserid",user.getProviderId());
                    }

//                    Log.e("aminainanianianain", intent + "");
//                    Log.e("aminainanianianain",intent.getStringExtra("location_long"));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("broadcast_location"));
    }

    private boolean runtimePermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.
                    PERMISSION_GRANTED) {
                enableButton();
            } else {
                runtimePermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    public void onSignedOutCleanUp() {
        mUsername = ANONYMOUS;
        locations.clear();
        adapter.notifyDataSetChanged();
        detachDatabaseReadListener();
    }

    public void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
//                    mMessageAdapter.add(friendlyMessage);
//                    cordinatesTV.append(userLocation.getLongitude());
//                    Log.d("huhahahaha", userLocation.getLongitude());
                    if (userLocation != null) {
                        locations.add(userLocation);
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
            };
            mMessageDatabseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessageDatabseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public static class FirebaseVH extends RecyclerView.ViewHolder {

        TextView userTV, timeTV, latitudeTV, longitudeTV;

        public FirebaseVH(View itemView) {
            super(itemView);

            userTV = (TextView) itemView.findViewById(R.id.user);
            timeTV = (TextView) itemView.findViewById(R.id.time);
            latitudeTV = (TextView) itemView.findViewById(R.id.latitude);
            longitudeTV = (TextView) itemView.findViewById(R.id.longitude);
        }
    }
}