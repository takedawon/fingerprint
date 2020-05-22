package com.example.myapplication;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintManagerCallback extends FingerprintManager.AuthenticationCallback implements CancellationSignal.OnCancelListener {
    String DEFAULT_KEY_NAME = "ubit_key";
    FingerprintManager fingerprintManager;
    CancellationSignal cancellationSignal;
    AuthenticationListener authenticationListener;
    private Cipher cipher;


    public FingerprintManagerCallback(Context context) {
        fingerprintManager
                = context.getSystemService(FingerprintManager.class);
    }

    private boolean init() {
        try {
            KeyGenerator keyGenerator = createKey(DEFAULT_KEY_NAME, true);

            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startListening() {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        if (init()) {
            cancellationSignal = new CancellationSignal();
            cancellationSignal.setOnCancelListener(this);
            authenticationListener.succedded();
            fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), cancellationSignal, 0, this, null);
        } else {
            Log.w("error:", "fingerprint init failure");
            authenticationListener.failed();
        }
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    /**
     * 지문인식이 가능한 디바이스인지 isHardwareDetected()
     * 디바이스에 지문이 등록 되어있는지 hasEnrolledFingerprints()
     * 없다면 false
     */
    public boolean isFingerprintAuthAvailable() {
        return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
    }

    private KeyGenerator createKey(String keyName, boolean invalidatedByBiometricEnrollment) throws Exception {
        KeyGenerator mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
        }
        mKeyGenerator.init(builder.build());
        mKeyGenerator.generateKey();
        return mKeyGenerator;
    }

    @Override
    public void onCancel() {

    }

    public void setAuthenticationListener(AuthenticationListener authenticationListener) {
        this.authenticationListener = authenticationListener;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        authenticationListener.succedded();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        authenticationListener.failed();
    }
}


