package com.team6.quickcashteam6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDB;
    private DatabaseReference firebaseDBEmployee;
    private DatabaseReference firebaseDBEmployer;
    private final String DB_URL = "https://quickcash-team6-default-rtdb.firebaseio.com/";
    private FirebaseAuth mAuth;
    private ArrayList<Employee> employees = new ArrayList<>();
    private ArrayList<Employer> employers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load state
        super.onCreate(savedInstanceState);
        //Load view
        setContentView(R.layout.activity_main);
        //Adds login button
        mAuth = FirebaseAuth.getInstance();
        Button login = findViewById(R.id.buttonLogin);
        //Instance variables
        final EditText lEmail = findViewById(R.id.lEmail);
        final EditText lPassword = findViewById(R.id.lPassword);
        //listener to check for user submission
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(lEmail.getText().toString() + " " + lPassword.getText().toString());
                login(lEmail.getText().toString(), lPassword.getText().toString());
                checkUserType();
            }
        });

        //Loads register button
        TextView registerL = findViewById(R.id.registrationLink);
        //listener to check for user registration
        registerL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login(String email, String password) {
        //Authenticates user credentials
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Toast upon sign in success
                    Log.d("LoginActivity", "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(LoginActivity.this, "You are now logged in!" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();

                } else {
                    //Error message on sign in failure
                    Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "ERROR: Login details invalid", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void checkUserType() {

        /*
        Retrieve all employees and employers put them in an array and determine what instance of then use if statement to switch
         */

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference employeeRef = firebase.getReference("Employee");
        DatabaseReference employerRef = firebase.getReference("Employer");
        DatabaseReference UserRef = firebase.getReference("Test");

        employeeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                employees = collectEmployees(((Map<String, Object>) dataSnapshot.getValue()));
                switch2UserPage();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }


        });

        employerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                employers = collectEmployers(((Map<String, Object>) snapshot.getValue()));
                switch2UserPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private ArrayList<Employee> collectEmployees(Map<String, Object> value) {
        ArrayList<Employee> employeesList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Map singleUser = (Map) entry.getValue();

            Employee employee = new Employee((String) singleUser.get("id"), (String) singleUser.get("name"));
            long age = (long) singleUser.get("age");
            employee.setAge((int) age);
            employee.setGender((String) singleUser.get("gender"));
            employee.setEmployee();
            employeesList.add(employee);

        }


        return employeesList;

    }


    private ArrayList<Employer> collectEmployers(Map<String, Object> value) {
        ArrayList<Employer> employersList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Map singleUser = (Map) entry.getValue();

            Employer employer = new Employer((String) singleUser.get("id"), (String) singleUser.get("name"));

            long age = (long) singleUser.get("age");
            employer.setAge((int) age);
            employer.setGender((String) singleUser.get("gender"));
            employer.setEmployer();

            employersList.add(employer);
        }

        return employersList;

    }

    public void switch2UserPage() {
        String userID=mAuth.getCurrentUser().getUid();

        for (Employee employee1 : employees) {
            System.out.println("User ID: " + employee1.getID());
            if (employee1.getID().equals(mAuth.getUid())) {
                if (employee1.isEmployee()) {
                    startActivity(new Intent(LoginActivity.this, EmployeeRecommendationActivity.class));
                }
                break;

            }
        }

        for (Employer employer1 : employers) {
            System.out.println("User ID: " + employer1.getID());
            if (employer1.getID().equals(mAuth.getUid())) {
                if (employer1.isEmployer()) {
                    Intent employerIntent= new Intent(LoginActivity.this, EmployerPageActivity.class);
                    employerIntent.putExtra("ID",userID);
                    startActivity(employerIntent);
                }
                break;
            }
        }

    }
}