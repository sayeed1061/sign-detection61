package com.example.signdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;


import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Signup extends AppCompatActivity implements View.OnClickListener {


    private EditText edt_username, edt_date, edt_email, edt_password;
    private CardView signupBtn;
    private TextView alredyHaveAcc, existUsers, invalidMsgUsername;
    ProgressDialog progressDialog;
    private DatePickerDialog datePickerDialog;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        edt_username = findViewById(R.id.usernameId);
        edt_date = findViewById(R.id.birthDateId);
        edt_email = findViewById(R.id.emailId);
        edt_password = findViewById(R.id.passwordId);
        existUsers = findViewById(R.id.err);






        //Unique username checking
        DatabaseReference usersRefs = FirebaseDatabase.getInstance().getReference().child("Users");
        invalidMsgUsername = findViewById(R.id.invalidUserMsgTextView);

        edt_username.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(runnable);

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        String username = charSequence.toString().trim();

                        if (!username.isEmpty()) {
                            // Check if username is already registered in the database
                            usersRefs.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        invalidMsgUsername.setText("This username is already taken.");
                                    } else {
                                        invalidMsgUsername.setText("");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle errors
                                    invalidMsgUsername.setText(databaseError.getMessage());
                                }
                            });
                        } else {
                            invalidMsgUsername.setText("Invalid username");
                        }
                    }
                };

                handler.postDelayed(runnable, 500); // Schedule the new runnable
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });








        signupBtn = findViewById(R.id.signUpButton);
        alredyHaveAcc = findViewById(R.id.already_account);

        signupBtn.setOnClickListener(this);
        alredyHaveAcc.setOnClickListener(this);

        edt_date.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setDate();
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.signUpButton:
                registerUser();
                break;
            case R.id.already_account:
                Intent i = new Intent(Signup.this,Login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
        }
    }

    private void registerUser(){
        String username = edt_username.getText().toString().trim();
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();
        String bDate = edt_date.getText().toString().trim();



        if(username.isEmpty()){
            edt_username.setError("username required");
            edt_username.requestFocus();
            return;
        }


        if(bDate.isEmpty()){
            edt_username.setError("birthday required");
            edt_username.requestFocus();
            return;
        }

        else if(email.isEmpty()){
            edt_email.setError("email required");
            edt_email.requestFocus();
            return;
        }


        else if(password.isEmpty()){
            edt_password.setError("password required");
            edt_password.requestFocus();
            return;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edt_email.setError("Valid email required");
            edt_email.requestFocus();
            return;
        }

        else if(password.length() < 6){
            edt_password.setError("password should be at least 6 charcter");
            edt_password.requestFocus();
            return;
        }

        else{

            progressDialog.setMessage("Wait while registration");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        UserClass userClass = new UserClass(username,bDate,email,password);

                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(userClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(Signup.this,"Successfully Registered",Toast.LENGTH_SHORT).show();
                                            nextActivity();
                                        }

                                        else{
                                            progressDialog.dismiss();
                                            Toast.makeText(Signup.this,"Fail",Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                    }

                    else{
                        progressDialog.dismiss();

                        String s = ""+task.getException();
                        if(s.length() == 116){
                            existUsers.setText("Username already exist");
                        }
                        else{
                            Toast.makeText(Signup.this,""+task.getException(),Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            });
        }


    }
    private void nextActivity(){
        Intent i = new Intent(Signup.this,Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void setDate(){
        DatePicker datePicker = new DatePicker(this);
        int currentDay = datePicker.getDayOfMonth();
        int currentMonth = (datePicker.getMonth()+1);
        int currentYear = datePicker.getYear();
        datePickerDialog = new DatePickerDialog(this,


                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        edt_date.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                },currentYear, currentMonth, currentDay);

        datePickerDialog.show();
    }
}