package com.myproject.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpPage extends AppCompatActivity {

    private EditText entered_key;
    private Button authorize_btn;
    private TextView error_msg;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        // TODO: Database Connection and checking if the key is valid. If Valid, Grant access to the user to the next page where he can fill in itss personal details (profile creation)

        entered_key = findViewById(R.id.kit_key);
        authorize_btn = findViewById(R.id.signup_btn);
        error_msg = findViewById(R.id.error_msg_signup);



        authorize_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View ignored){
                //TODO: Validation code
                String key_in_string = entered_key.getText().toString();
                /**
                 * If Successfully validated :
                 *      Intent profilepageintent = new Intent(SignUpPage.this, ProfileCreationPage.class);
                 *      startActivity(profilepageintent);
                 * else:
                 *      error_msg.setVisibility(View.VISIBLE);
                 *      entered_key.setText(null);
                 */
                if(key_in_string.equals("abcde")){
                    Log.d("signup_page", "Entered key is valid!!!");
                    error_msg.setVisibility(View.INVISIBLE);
                }else{
                    error_msg.setVisibility(View.VISIBLE);
                    entered_key.setText(null);
                }
            }
        });
    }
}
