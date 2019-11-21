package com.IOT.Gates;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignIn extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button signInButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    private static final String TAG = "Sign In Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditTextSignIn);
        passwordEditText = findViewById(R.id.passwordEditTextSignIn);
        signInButton = findViewById(R.id.signInButton);
        progressBar = findViewById(R.id.signInProgressBar);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty()) emailEditText.setError("can't be empty");
                else if (password.isEmpty()) passwordEditText.setError("can't be empty");
                else signIn(email, password);
            }
        });
    }

    private void signIn(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // success
                    Log.d(TAG, "signInWithEmail:success");
                    progressBar.setVisibility(View.GONE);
                    finish();
                    startActivity(new Intent(SignIn.this, MainActivity.class));
                } else {
                    // Error
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signUp(View view) {
        startActivity(new Intent(SignIn.this, SignUp.class));
    }
}
