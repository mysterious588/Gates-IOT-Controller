package com.IOT.Gates;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.IOT.Gates.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private EditText firstNameEditText, secondNameEditText, emailEditText, passwordEditText;
    private static final String TAG = "Sign Up Activity";
    FirebaseAuth mAuth;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        mAuth = FirebaseAuth.getInstance();

        //assign the views
        firstNameEditText = findViewById(R.id.firstNameEditText);
        secondNameEditText = findViewById(R.id.secondNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.signUpProgressBar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName, secondName, email, password;
                firstName = firstNameEditText.getText().toString().trim();
                secondName = secondNameEditText.getText().toString().trim();
                email = emailEditText.getText().toString().trim();
                password = passwordEditText.getText().toString();

                //check if any edit texts are empty
                if (firstName.equals("")) firstNameEditText.setError("Can't be empty");
                else if (secondName.equals("")) secondNameEditText.setError("Can't be empty");
                else if (email.equals("")) emailEditText.setError("Can't be empty");
                else if (password.equals("")) passwordEditText.setError("Can't be empty");
                else if (password.length() < 6) passwordEditText.setError("password too short");
                else createAccount(firstName, secondName, email, password);
            }
        });
    }

    private void createAccount(final String firstName, final String secondName, final String email, final String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //success TODO Go BACK
                    Log.d(TAG, "createUserWithEmail:success");
                    final FirebaseUser user = mAuth.getCurrentUser();
                    final User currentUser = new User(firstName, secondName, email);
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            currentUser.setMessageToken(Objects.requireNonNull(task.getResult()).getToken());
                            assert user != null;
                            usersRef.child(user.getUid()).setValue(currentUser);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUp.this, "Hello " + firstName + " " + secondName, Toast.LENGTH_SHORT).show();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(firstName + " " + secondName).build();
                            user.updateProfile(profileUpdates);
                            /*startActivity(new Intent(SignUp.this, MainActivity.class));
                            finish();*/
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    });

                } else {
                    // Error
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(SignUp.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
