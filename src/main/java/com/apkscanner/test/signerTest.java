package com.apkscanner.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.android.apksig.ApkVerifier;
import com.android.apksig.ApkVerifier.Result;
import com.android.apksig.apk.ApkFormatException;
import com.apkspectrum.logback.Log;

public class signerTest {

    public static void main(String[] args) throws Throwable {
        // String query = "http://" + "10.253.92.73" + "/table.html";
        //
        // Document doc = Jsoup.connect(query).timeout(20000).get();
        // Log.d("query : " + query);
        //
        // Log.d(doc.html());

        ApkVerifier verifier = new ApkVerifier.Builder(
                new File("/home/leejinhyeong/Desktop/auInitialSetting_v010414.apk")).build();
        try {
            Result result = verifier.verify();

            Log.d("" + result.isVerified() + result.isVerifiedUsingV1Scheme()
                    + result.isVerifiedUsingV2Scheme());
            Log.d("" + result.getV2SchemeSigners().get(0).getCertificate());


        } catch (ApkFormatException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

        // savefile(inputlist.html());
    }


}
