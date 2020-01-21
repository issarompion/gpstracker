package com.example.issarompion.testaes;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static String encrypt(String textToEncrypt, String myOwnSalt) throws Exception {
        final byte[] pass = textToEncrypt.getBytes(StandardCharsets.UTF_8);
        final byte[] salt = (new SecureRandom()).generateSeed(8);
        final byte[] inBytes = myOwnSalt.getBytes(StandardCharsets.UTF_8);

        final byte[] passAndSalt = array_concat(pass, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            final byte[] hashData = array_concat(hash, passAndSalt);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }

        final byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        final byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        final SecretKeySpec key = new SecretKeySpec(keyValue, "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(inBytes);
        data =  array_concat(array_concat("Salted__".getBytes(StandardCharsets.UTF_8), salt), data);
        return Base64.getEncoder().encodeToString( data );
    }

    private static byte[] array_concat(final byte[] a, final byte[] b) {
        final byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void main(String[] args) throws Exception {
        String s = encrypt("myPassword", "lesnainsdejardin");
        System.out.println(s);
    }

}