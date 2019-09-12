package net.globulus.easyflavor.demo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import net.globulus.easyflavor.EasyFlavor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyFlavor.setResolver(c -> AppFlavors.get());
        FtueManager ftueManager = EasyFlavor.get(this, FtueManager.class);
        ftueManager.signup("email", "password", v ->
                Log.e("CALLBACK",
                        "Callback value: " + ((v != null) ? v.toString() : "null"))
        );
    }
}
