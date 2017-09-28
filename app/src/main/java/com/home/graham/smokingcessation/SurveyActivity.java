package com.home.graham.smokingcessation;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity {

    private static final String TAG = "SurveyActivity";
    private LinearLayout layout;
    private Button signOutButton, submitButton;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    DatabaseReference questionListReference;
    ArrayList<Question> surveyQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        // Set up UI
        initUI();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // if user is null launch login activity
                    startActivity(new Intent(SurveyActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutButton();
            }
        });

        questionListReference = FirebaseDatabase.getInstance().getReference().child("surveyQuestions");
        questionListReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get list of all survey questions
                        getQuestions((Map<String, Object>) dataSnapshot.getValue());
                        populateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }
    // Initialize UI elements
    private void initUI() {
        layout = (LinearLayout)findViewById(R.id.surveyLayout);
        signOutButton = new Button(layout.getContext());
        signOutButton.setId(R.id.signout_btn_id);
        signOutButton.setText(R.string.btn_sign_out);
        signOutButton.setWidth(100);
        signOutButton.setHeight(25);
        layout.addView(signOutButton);
    }

    // Get survey questions from DataSnapshot
    private void getQuestions(Map<String, Object> questions) {
        for (Map.Entry<String, Object> entry : questions.entrySet()) {
            Map question = (Map) entry.getValue();
            // Create a Question object from the data and add to list
            surveyQuestions.add(new Question(entry.getKey(), Integer.parseInt(question.get("position").toString()), question.get("text").toString()));
        }
        Collections.sort(surveyQuestions);
    }

    // Populate UI with questions fetched from database
    private void populateUI() {
        for (Question question : surveyQuestions) {
            TextView questionText = new TextView(layout.getContext());
            questionText.setText(question.getText());
            questionText.setWidth(100);
            questionText.setHeight(75);
            layout.addView(questionText);
            EditText answerField = new EditText(layout.getContext());
            answerField.setId(question.getPosition());
            answerField.setWidth(100);
            answerField.setHeight(150);
            layout.addView(answerField);
        }
        createSubmitButton();
    }

    private void createSubmitButton() {
        submitButton = new Button(layout.getContext());
        submitButton.setText(R.string.submit_btn);
        submitButton.setId(R.id.submit_btn_id);
        submitButton.setWidth(100);
        submitButton.setHeight(25);
        layout.addView(submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validSubmission = true;
                for (int i=0;i<surveyQuestions.size();i++) {
                    if (((EditText)findViewById(i)).getText().length()==0) {
                        validSubmission = false;
                        break;
                    }
                }
                if (!validSubmission) {
                    Toast.makeText(getApplicationContext(), R.string.submission_fail, Toast.LENGTH_LONG).show();
                } else {
                    DatabaseReference answerReference = FirebaseDatabase.getInstance().getReference().child("surveyAnswers");
                    for (Question question : surveyQuestions) {
                        answerReference.child(question.getName()).child("answer").setValue(((EditText)findViewById(question.getPosition())).getText().toString());
                    }
                    Toast.makeText(getApplicationContext(), R.string.submission_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //sign out method
    public void signOutButton() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}