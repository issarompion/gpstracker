package com.example.issarompion.testaes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String strToEncrypt = "My text to encrypt";
        final String strPssword = "encryptor key";
        try {
            System.out.println(AES.encrypt("hello","lesnainsdejardin"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
