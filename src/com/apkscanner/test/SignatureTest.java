package com.apkscanner.test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import sun.security.pkcs.PKCS7;

public class SignatureTest {
	static Signature[] mSignatures;
	
	public static void main(String[] args) throws Exception {
		
		mSignatures = new Signature[1];
		
		
		mSignatures[0] = new Signature("30820246308201af02044b6965cc300d06092a864886f70d01010405003069310b3009060355040613024b52310e300c0603550408130553454f554c310e300c0603550407130553454f554c310d300b060355040a13044c4f454e311b3019060355040b13124c4f454e20456e7465727461696e6d656e74310e300c060355040313054d656c4f6e3020170d3130303230333132303232305a180f33303039303630363132303232305a3069310b3009060355040613024b52310e300c0603550408130553454f554c310e300c0603550407130553454f554c310d300b060355040a13044c4f454e311b3019060355040b13124c4f454e20456e7465727461696e6d656e74310e300c060355040313054d656c4f6e30819f300d06092a864886f70d010101050003818d0030818902818100a7d662ce0d5fd629e909a70484af40cb86c5cc68e6aa41f7e84cba93ef039b5052e48358d3a80f91bef249709227eabc8456b21e16ac8f07d5dbd27f5020aab312b83c77444c993867d5d2482eeff11eb3ba08d4151d790e3bfcd119f891ca9c83cc5fee2d740634c5c18847d0a7cc42dd6d94924e7828dce2f430cf294e2d6f0203010001300d06092a864886f70d010104050003818100610d7baf8e9b066568c2a7ef5f96137a594bb0f311af251774738f89ddf70fa4f7cdd16177e6c2c3d8b0b614283a5f70171c668968cb0b30d077f79aeb04b9566acc23b29f41286ce114f2742ad60287c2e3e5e33a1d18ac22fd13dc82bf28d16e844021f019ce237846bab5049bd9522e73482931decd5710e7b06d2cb26053");
		//X509Certificate x509 = convertToX509Cert("MIICRjCCAa8CBEtpZcwwDQYJKoZIhvcNAQEEBQAwaTELMAkGA1UEBhMCS1IxDjAMBgNVBAgTBVNFT1VMMQ4wDAYDVQQHEwVTRU9VTDENMAsGA1UEChMETE9FTjEbMBkGA1UECxMSTE9FTiBFbnRlcnRhaW5tZW50MQ4wDAYDVQQDEwVNZWxPbjAgFw0xMDAyMDMxMjAyMjBaGA8zMDA5MDYwNjEyMDIyMFowaTELMAkGA1UEBhMCS1IxDjAMBgNVBAgTBVNFT1VMMQ4wDAYDVQQHEwVTRU9VTDENMAsGA1UEChMETE9FTjEbMBkGA1UECxMSTE9FTiBFbnRlcnRhaW5tZW50MQ4wDAYDVQQDEwVNZWxPbjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAp9Zizg1f1inpCacEhK9Ay4bFzGjmqkH36Ey6k+8Dm1BS5INY06gPkb7ySXCSJ+q8hFayHhasjwfV29J/UCCqsxK4PHdETJk4Z9XSSC7v8R6zugjUFR15Djv80Rn4kcqcg8xf7i10BjTFwYhH0KfMQt1tlJJOeCjc4vQwzylOLW8CAwEAATANBgkqhkiG9w0BAQQFAAOBgQBhDXuvjpsGZWjCp+9flhN6WUuw8xGvJRd0c4+J3fcPpPfN0WF35sLD2LC2FCg6X3AXHGaJaMsLMNB395rrBLlWaswjsp9BKGzhFPJ0KtYCh8Lj5eM6HRisIv0T3IK/KNFuhEAh8BnOI3hGurUEm9lS");
		//System.out.println(x509.getPublicKey());

		PKCS7 p7 = new PKCS7(new FileInputStream("C:\\Users\\admin\\Desktop\\어치\\CERT.RSA"));
		//PKCS7 p7 = new PKCS7(new FileInputStream("C:\\CERT.RSA"));
		X509Certificate[] ttt = p7.getCertificates();
		System.out.println(ttt.length);
		
		//st = new SignatureTest(ttt);
		
		
		
		
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		X509Certificate X509Certificatecertificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(mSignatures[0].toByteArray()));
		System.out.println(X509Certificatecertificate.getPublicKey());
		System.out.println(X509Certificatecertificate.toString());
		System.out.println(X509Certificatecertificate.getSigAlgName());

		System.out.println(mSignatures[0].toCharsString());
		

		for(int i = 0; i < ttt.length; i++) {
			System.out.println("-------------- " + i);
			System.out.println(ttt[i].toString());
			System.out.println(Integer.toHexString(ttt[i].hashCode()));
			System.out.println(Integer.toHexString(System.identityHashCode(mSignatures[0])));
			System.out.println(Integer.toHexString(System.identityHashCode(mSignatures)));
		}

	}

}
