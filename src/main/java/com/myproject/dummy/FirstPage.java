package com.myproject.dummy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FirstPage extends AppCompatActivity{

    private LinearLayout screen;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        try{
            super.onCreate(savedInstanceState);
            display();
        }catch(android.view.InflateException e){
            Log.e("ERROR", "Error inflating class!");
            e.printStackTrace();

        }
    }
    private void display(){
        setContentView(R.layout.welcome_page);
        screen = (LinearLayout) findViewById(R.id.welcome_image);

        screen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View ignored){
                // Change Page to ChoicePage
                Log.d("FirstPage", "Image Clicked!");
                Intent in = new Intent(FirstPage.this, ChoicePage.class);
                startActivity(in);
            }
        });
    }
}
