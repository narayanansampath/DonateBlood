package com.TheProgrammingShinigami.blooddonation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.TheProgrammingShinigami.blooddonation.beans.User;

/**
 * Created by Narayanan on 24/06/17.
 */

public class UserSignupActivity extends AppCompatActivity {

    private TextView loginLink;
    private EditText password, email, name, number;
    private Button registerButton;
    ProgressDialog progressDialog;
    String userEmail, userPassword, userName, userNumber;

    public DatabaseReference dbReference;
    public FirebaseDatabase database;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loginLink = (TextView) findViewById(R.id.login_link);
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        number = (EditText) findViewById(R.id.number);
        registerButton = (Button) findViewById(R.id.signup_button);


        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(UserSignupActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                //Get the values entered by the user
                userName = name.getText().toString().trim();
                userEmail = email.getText().toString().trim();
                userPassword = password.getText().toString().trim();
                userNumber = number.getText().toString().trim();

                //Validate if the user entered information is valid
                if (!validateForm()) {return;}

                //If we reach here then the inforamtion entered by the user is valid
                //so create a user in our firebase auth
                createUser(userEmail, userPassword);
            }
        });
    }
    public void createUser(String email, String password) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering You..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // creates a user using email and password
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, then add the user details to the database
                            addUserDetails(mAuth.getCurrentUser());
                        } else {
                            // If sign in fails, display a message to the user.
                            showToast("Auth Failed");
                        }
                    }
                });
    }

    public void addUserDetails(FirebaseUser user) {
        // This function gets the Uid of the created user and created a new entry
        // in the databse under the Uid of the user
        String userId = user.getUid();  //gets the Uid
        dbReference = FirebaseDatabase.getInstance().getReference();

        //use the container class to hold the values entered by the user
        User myUser = new User(toCamelCase(userName), userEmail, userPassword, userNumber);

        // what happens here is we get a reference to the database/users/'usesrId'
        //then we add the user details under that structure
        dbReference.child("users").child(userId).setValue(myUser).
                addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.cancel();
                        if (task.isSuccessful()) {
                            //If user is added to database
                            showToast("Successfully Registered :)");
                            finish();
                            startActivity(new Intent(UserSignupActivity.this, MainActivity.class));

                        } else {
                            //if it fails
                            showToast("Registration Failed :(");
                        }
                    }
                });
    }

    //performs validation on user data and returns a boolean
    public boolean validateForm() {
        //validate if the data entered by user is valid nor not
        if (TextUtils.isEmpty(userName)) {
            name.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Required");
            return false;
        } else if (!userEmail.contains("@")) {
            email.setError("Enter a valid email");
            return false;
        }
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Required");
            return false;
        } else if (userPassword.length() < 4) {
            password.setError("Password should be more than 4");
            return false;
        }
        if (TextUtils.isEmpty(userNumber)) {
            number.setError("Required");
            return false;
        }
        return true;
    }

    //easy function for toasting
    public void showToast(String textToToast) {
        Toast.makeText(this, textToToast, Toast.LENGTH_SHORT).show();
    }

    public String toCamelCase(final String init) {
        if (init==null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length()==init.length()))
                ret.append(" ");
        }

        return ret.toString();
    }
}
