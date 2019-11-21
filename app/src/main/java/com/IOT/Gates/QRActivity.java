package com.IOT.Gates;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.IOT.Gates.Models.fcm.Data;
import com.IOT.Gates.Models.fcm.FirebaseCloudMessage;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QRActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private static final int PERMISSION_REQUEST_CAMERA = 9219;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";
    private static final String TAG = "QR Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

            }
        } else {
            CodeScannerView scannerView = findViewById(R.id.scanner_view);
            mCodeScanner = new CodeScanner(this, scannerView);
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    QRActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("gates").child(result.getText()).child("owners").hasChild(mAuth.getUid())) {
                                        rootRef.child("gates").child(result.getText()).child("state").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(QRActivity.this, "Gate " + result.getText() + " Unlocked", Toast.LENGTH_SHORT).show();
                                                sendMessage(mAuth.getCurrentUser().getDisplayName() + " just unlocked gate " + result.getText(), "Gate " + result.getText(), result.getText());
                                            }
                                        });
                                    } else {
                                        Toast.makeText(QRActivity.this, "You aren't authorized", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            });
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCodeScanner.startPreview();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPermission();
        if (mCodeScanner != null) mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        if (mCodeScanner != null) mCodeScanner.releaseResources();
        super.onPause();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    private void sendMessage(final String title, final String message, final String topic) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        final API fcmApI = retrofit.create(API.class);
        final HashMap<String, String> headers = new HashMap<>();
        final String[] key = new String[1];
        rootRef.child("server").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                key[0] = dataSnapshot.getValue(String.class);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + key[0]);
                Data data = new Data();
                data.setMessage(message);
                data.setTitle(title);
                data.setData_type("data_type_admin_broadcast");
                FirebaseCloudMessage firebaseCloudMessage = new FirebaseCloudMessage();
                firebaseCloudMessage.setData(data);
                firebaseCloudMessage.setTo("/topics/" + topic);
                Call<ResponseBody> call = fcmApI.sendNotification(headers, firebaseCloudMessage);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d(TAG, "response received " + response.message());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}