package com.smart.androiddemo2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ComposeEmailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compose_emails);

        //get data received from main activity
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        if(name == null){
            //make the text view to dispaly some text in the case that we dont get the name
            ((TextView)findViewById(R.id.textView4)).setText("composing Message");
        }else{
            //make the text view to dispaly the name
            ((TextView)findViewById(R.id.textView4)).setText(name);

        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}