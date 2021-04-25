package com.assignment.connectnow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference myDatabase;
    HomeActivity homeActivity;
    private static final int PERMISSION_REQ_ID = 22;

    // Ask for Android device permissions at runtime.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        homeActivity = this;
        setContentView(R.layout.activity_home);
        gotoFragment(new LiveStreaming(),null,null,this);
        setUpFireBase();

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                    checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {

            }
        }

    }
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                    checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {

            }else Toast.makeText(this,"Permission denied, some of the functionality may not work",Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpFireBase() {
        FirebaseApp.initializeApp(this);
        myDatabase = ((MainApplication)getApplication()).getFirebaseDatabase().getReference("token");
        myDatabase.child("0").get().addOnCompleteListener(task -> {
            ClassUtils.writePrefString(this,"token",(String) task.getResult().getValue());
        });
    }



    public void gotoFragment(Fragment fragment, Bundle bundle, String backStackTag, Context activity) {
        try {
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(0, 0);
            if (activity instanceof HomeActivity) {
                fragmentTransaction.replace(R.id.frame_home, fragment);
            }
            if (backStackTag != null) {
                fragmentTransaction.addToBackStack(backStackTag);
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
}