package net.globulus.easyflavor.demo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyFlavors.setResolver(c -> AppFlavors.get());
        FtueManager ftueManager = EasyFlavors.get(this, FtueManager.class);
        ftueManager.signup("email", "password", v ->
                Log.e("CALLBACK",
                        "Callback value: " + ((v != null) ? v.toString() : "null"))
        );
    }
}
