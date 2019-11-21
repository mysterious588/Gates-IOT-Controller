package com.IOT.Gates;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.IOT.Gates.Models.Gate;
import com.IOT.Gates.Models.fcm.Data;
import com.IOT.Gates.Models.fcm.FirebaseCloudMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference gateRef;
    ListView gatesListView;

    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";
    private static final String TAG = "Main Activity";

    ArrayList<String> authorizedGatesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gateRef = FirebaseDatabase.getInstance().getReference().child("gates");

        authorizedGatesArrayList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignIn.class));
            finish();
        } else {
            updateAuthorizedGates();


            FloatingActionButton fab = findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createAlertDialog();
                }
            });
        }
        ;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void openGate(View view) {
        View layout = LayoutInflater.from(this).inflate(R.layout.open_gate, null);

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setCancelable(true);

        gatesListView = layout.findViewById(R.id.gatesListView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, authorizedGatesArrayList);
        gatesListView.setAdapter(adapter);
        gatesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TextView textView = (TextView) view;
                rootRef.child("gates").child(textView.getText().toString()).child("state").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendMessage("Gate " + textView.getText(), Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName() + " Just unlocked " + textView.getText(), textView.getText().toString());
                        Toast.makeText(MainActivity.this, "Gate " + textView.getText() + " Unlocked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        gatesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TextView textView = (TextView) view;
                rootRef.child("gates").child(textView.getText().toString()).child("state").setValue("0").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Gate " + textView.getText() + " Locked", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });
        alertDialog.setView(layout);
        alertDialog.show();
    }

    public void scanQR(View view) {
        startActivity(new Intent(MainActivity.this, QRActivity.class));
    }

    private void createAlertDialog() {
        View layout = getLayoutInflater().inflate(R.layout.add_gate, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle( Html.fromHtml("<font color='#b71c1c'>Add A New Gate</font>"));
        alertDialog.setCancelable(true);

        final EditText name = layout.findViewById(R.id.gateName);
        final EditText PIN = layout.findViewById(R.id.gatePIN);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String gateName = name.getText().toString().trim();
                String gatePIN = PIN.getText().toString();
                final Gate gate = new Gate(gateName, gatePIN);
                gateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (Objects.equals(snapshot.child("authorization").child("PIN").getValue(String.class), gate.getPIN()) && Objects.equals(snapshot.child("authorization").child("name").getValue(String.class), gate.getName())) {
                                gateRef.child(Objects.requireNonNull(snapshot.getKey())).child("owners").child(Objects.requireNonNull(mAuth.getUid())).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(MainActivity.this, "Gate " + snapshot.getKey() + " has been added successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(layout);
        alertDialog.show();

        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonNegative.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void updateAuthorizedGates() {
        rootRef.child("gates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("owners").hasChild(mAuth.getUid()) && !authorizedGatesArrayList.contains(snapshot.getKey())) {
                        authorizedGatesArrayList.add(snapshot.getKey());
                        subscribe(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void subscribe(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Hooray";
                if (!task.isSuccessful()) {
                    msg = "failed";
                }
                Log.d(TAG, msg);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            mAuth.signOut();
            finish();
            startActivity(new Intent(MainActivity.this, SignIn.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
