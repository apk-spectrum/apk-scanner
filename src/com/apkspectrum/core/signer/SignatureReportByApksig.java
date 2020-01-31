package com.apkspectrum.core.signer;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import com.android.apksig.ApkVerifier;
import com.android.apksig.ApkVerifier.Result;
import com.android.apksig.apk.ApkFormatException;
import com.apkspectrum.util.Log;

public class SignatureReportByApksig extends SignatureReport
{
	public SignatureReportByApksig(File file) {
		ApkVerifier verifier = new ApkVerifier.Builder(file).build();
		try {
			Result result = verifier.verify();
			if(result.isVerified()) {
				certificates = result.getSignerCertificates().toArray(new X509Certificate[0]);
				if(result.isVerifiedUsingV3Scheme()) {
					signScheme = SIGNATURE_SCHEME_V3;
				} else if(result.isVerifiedUsingV2Scheme()) {
					signScheme = SIGNATURE_SCHEME_V2;
				} else if(result.isVerifiedUsingV1Scheme()) {
					signScheme = SIGNATURE_SCHEME_V1;
				}
			} else {
				Log.w("Fail to verify signature");
			}
		} catch (NoSuchAlgorithmException | IllegalStateException | IOException | ApkFormatException e) {
			e.printStackTrace();
		}
	}
}
