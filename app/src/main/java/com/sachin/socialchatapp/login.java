package com.sachin.socialchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class login extends AppCompatActivity {

    //public CardView cardView;

    Button but1;

    EditText usernameInput;
    EditText passwordInput;

    TextView testText;

    public static String uName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //cardView = (CardView) findViewById(R.cardView);



        usernameInput = (EditText) findViewById(R.id.username);
        passwordInput = (EditText) findViewById(R.id.password);

        // testText = (TextView) findViewById(R.id.testText);

        setSingleEvent();

        //setSingleEvent(cardView);           // Set Event



    }

    public void setSingleEvent() {

        but1 = (Button) findViewById(R.id.but1);

        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uName = usernameInput.getText().toString();
                //Preferences.username = uName;

                Intent intent = new Intent(login.this, MainActivity.class);
                startActivity(intent);

            }
        });

    }
//    private void setSingleEvent(CardView cardView) {
//        cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                uName = usernameInput.getText().toString();
//
//                // Now can replace Toast with start new activity code
//                Intent intent = new Intent(login.this, MainActivity.class);
//
//                startActivity(intent);  // to start intent
//                //Toast.makeText(login.this, "Clicked", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


}
