package edu.utep.cs.cs4330.battleship;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.RadialGradient;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button p2pBtn;
    private Button stratBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        p2pBtn = (Button) findViewById(R.id.p2pBtn);
        stratBtn = (Button) findViewById(R.id.stratBtn);
    }

    public void p2pClicked(View view){
        Intent intent = new Intent(this, gameActivityBT.class);
        startActivity(intent);
    }

    public void strategyClicked(View view){
        Intent intent = new Intent(this, gameActivity.class);
        startActivity(intent);
    }
}
