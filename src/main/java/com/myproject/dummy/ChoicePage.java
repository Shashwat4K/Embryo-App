package com.myproject.dummy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChoicePage extends AppCompatActivity {

    //private Button signinbtn;
    //private Button signupbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice);
        ((Button) findViewById(R.id.signin)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View ignored){
                // Change to LoginActivity
                Log.d("ChoicePage", "Sign-IN Clicked!");
                Intent signinpageintent = new Intent(ChoicePage.this, SignInPage.class);
                startActivity(signinpageintent);
            }
        });

        ((Button) findViewById(R.id.signup)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View ignored){
                // Change to SignupActivity
                Log.d("ChoicePage", "Sign-UP Clicked!");
                Intent signuppageintent = new Intent(ChoicePage.this, SignUpPage.class);
                startActivity(signuppageintent);
            }
        });
    }
}
