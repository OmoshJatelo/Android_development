package com.smart.androiddemo2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }



    public void udateText(View v){
       TextView helloWorld= (TextView) findViewById(R.id.textView);
       helloWorld.setText("Omosh, Welcome and stay sedulous!");
       Toast.makeText(this, "Text content changed successfully",Toast.LENGTH_LONG).show();
        Log.d("Sedulos", "We are very sedulous");

    }

    public void launchSettings(View v){
        //launch settings
        Intent i = new Intent(this, SettingsActivity.class);

        //send some DATA to the settings activity
        i.putExtra("Question","How has the experience been so far");
        startActivity(i);

    }

    //compose emails for reply, function
    public void composeEmail(View view){
        Intent intent = new Intent(this, ComposeEmailsActivity.class);
        String name = ((Button)view).getText().toString();
        intent.putExtra("NAME",name);
        startActivity(intent);



    }


}
