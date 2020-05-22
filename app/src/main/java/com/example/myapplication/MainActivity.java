package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button button;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        final FingerprintManagerCallback fingerprintManagerCallback = new FingerprintManagerCallback(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    fingerprintManagerCallback.setAuthenticationListener(new AuthenticationListener() {
                        @Override
                        public void succedded() {
                            Toast.makeText(getApplicationContext(),"성공",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(getApplicationContext(),"실패",Toast.LENGTH_SHORT).show();
                        }
                    });
                    fingerprintManagerCallback.startListening();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    BiometricPromptCallback biometricPromptCallback = new BiometricPromptCallback(getApplicationContext());
                    biometricPromptCallback.setAuthenticationListener(new AuthenticationListener() {
                        @Override
                        public void succedded() {
                            Toast.makeText(getApplicationContext(),"성공",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(getApplicationContext(),"실패",Toast.LENGTH_SHORT).show();
                        }
                    });
                    biometricPromptCallback.startListening();
                }
            }
        });
    }
}
