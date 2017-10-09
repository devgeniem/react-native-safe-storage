package com.geniem.reactnativesafestorage;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Exception;
import java.lang.String;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

class SafeStorageModule extends ReactContextBaseJavaModule {
    private static final String PREFS_NAME = "SafeStoragePreferences";
    private static final String TAG = "SafeStorageModule";
    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";
    private final String KEYSTORE="AndroidKeyStore";
    private final String ALIAS = "f7ad0b08-a9be-11e7-abc4-cec278b6b50a"; //key name


    private Context context;
    private KeyStore keystore;

    public SafeStorageModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from javascript.
     */
    @Override
    public String getName() {
        return "SafeStorage";
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            keystore = KeyStore.getInstance(KEYSTORE);
            keystore.load(null);

            if (!keystore.containsAlias(ALIAS)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(ALIAS)
                        .setSubject(new X500Principal("CN=SecStorage"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", KEYSTORE);
                generator.initialize(spec);
                KeyPair keyPair = generator.generateKeyPair();

            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @ReactMethod
    public void setEntry(String key, String value) {
        try {
            //check if keystore is initialized, return if it is not
            if (!keystore.containsAlias(ALIAS)) {return;}

            //encrypt the value string
            String encryptedString = encrypt(value);
            if(encryptedString == null) { return; }

            //store encrypted string to shared preferences
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(key, encryptedString);
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @ReactMethod
    public void getEntry(String key, String defaultValue, Callback callback) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            //check if keystore is initialized, return default value if it is not
            if (!keystore.containsAlias(ALIAS)) { callback.invoke(defaultValue);}

            //get encrypted string from shared preferences, if it does not exists return defaultValue
            String s = prefs.getString(key, null);
            if(s == null) {
                callback.invoke(defaultValue);
                return;
            }

            //return decrypted string back to the react-native
            callback.invoke(decrypt(s, defaultValue));
        } catch (KeyStoreException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            callback.invoke(defaultValue);
        }
    }

    private String encrypt(String value) {
        try {
            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry(ALIAS, null);
            RSAPublicKey pubKey = (RSAPublicKey) keyEntry.getCertificate().getPublicKey();

            Cipher chipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            chipher.init(Cipher.ENCRYPT_MODE, pubKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, chipher);
            cipherOutputStream.write(value.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private String decrypt(String value, String defaultValue) {
        try {
            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry(ALIAS, null);

            Cipher chipher = Cipher.getInstance(CIPHER_TYPE);
            chipher.init(Cipher.DECRYPT_MODE, keyEntry.getPrivateKey());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(value, Base64.DEFAULT));
            CipherInputStream cis = new CipherInputStream(inputStream, chipher);

            //convert CipherInputStream to byte array list
            ArrayList<Byte> list = new ArrayList<>();
            int data;
            while ((data = cis.read()) != -1) { list.add((byte)data);}

            //convert Arraylist to byte array (byte[])
            byte[] bytes = new byte[list.size()];
            for(int i = 0; i < bytes.length; i++) { bytes[i] = list.get(i).byteValue();}

            String decryptedString = new String(bytes, 0, bytes.length, "UTF-8");
            return  decryptedString;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return defaultValue;
        }
    }
}
