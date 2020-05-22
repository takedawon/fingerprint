package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

@RequiresApi(api = Build.VERSION_CODES.P)
public class BiometricPromptCallback extends BiometricPrompt.AuthenticationCallback
        implements CancellationSignal.OnCancelListener {
    private final BiometricPrompt mBiometricPrompt;
    String DEFAULT_KEY_NAME = "ubit_key";
    Context context;
    private Signature signature;
    private CancellationSignal cancellationSignal;
    private AuthenticationListener authenticationListener;

    public BiometricPromptCallback(final Context context) {
        Log.d("log", "BiometricPromptCallback");
        this.context = context;
        mBiometricPrompt = new BiometricPrompt.Builder(context)
                .setDescription("손가락을 붙여주세요!")
                .setTitle("지문을 입력해주세요.")
                .setSubtitle("Subtitle")
                .setNegativeButton("Cancel", context.getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "지문 입력에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }

    @Override
    public void onCancel() {

    }

    private KeyPairGenerator createkey(String keyName, boolean invalidatedByBiometricEnrollment) throws Exception {
        Log.d("biometric", "createkey");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");

        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA384,
                        KeyProperties.DIGEST_SHA512)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);

        keyPairGenerator.initialize(builder.build());

        return keyPairGenerator;
    }

    private boolean init() {
        Log.d("log", "init");
        try {
            KeyPairGenerator keyPairGenerator = createkey(DEFAULT_KEY_NAME, true);
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(keyPairGenerator.generateKeyPair().getPrivate());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startListening() {
        Log.d("log", "startListening");
        if (init()) {
            cancellationSignal = new CancellationSignal();
            cancellationSignal.setOnCancelListener(this);
            mBiometricPrompt.authenticate(new BiometricPrompt.CryptoObject(signature), cancellationSignal, context.getMainExecutor(), this);
        } else {
            Log.w("faild", "init failure");
            authenticationListener.failed();
        }
    }

    public void setAuthenticationListener(AuthenticationListener authenticationListener) {
        this.authenticationListener = authenticationListener;
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        authenticationListener.failed();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        authenticationListener.succedded();
    }
}
