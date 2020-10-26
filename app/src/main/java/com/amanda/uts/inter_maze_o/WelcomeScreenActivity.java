package com.amanda.uts.inter_maze_o;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amanda.uts.inter_maze_o.R;

public class WelcomeScreenActivity extends AppCompatActivity {
    public int buttonID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
    }

    public void selectSize(View view) {
        Intent intent;
        buttonID = view.getId();
        if(buttonID == R.id.button6) {
            intent = new Intent(this, CustomSizeActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
            Button button = findViewById(buttonID);
            int [] mazeSize = new int [2];
            /* rows */
            mazeSize[0] = Integer.parseInt(button.getText().toString().substring(0, 2));
            /* columns */
            mazeSize[1] = Integer.parseInt(button.getText().toString().substring(0, 2));
            intent.putExtra("mazeSize", mazeSize);
        }
        startActivity(intent);
    }
}
