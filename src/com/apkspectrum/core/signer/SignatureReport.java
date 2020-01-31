package com.apkspectrum.core.signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.CodeSigner;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.apkspectrum.resource._RStr;
import com.apkspectrum.util.Base64;
import com.apkspectrum.util.Log;

public class SignatureReport {

	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERT = "-----END CERTIFICATE-----";

	public static final String SIGNATURE_SCHEME_V1 = "v1";
	public static final String SIGNATURE_SCHEME_V2 = "v2";
	public static final String SIGNATURE_SCHEME_V3 = "v3";

	protected X509Certificate[] certificates;
	protected X509Certificate[] timestamp;
	protected String signScheme;

    //private static final DisabledAlgorithmConstraints DISABLED_CHECK =
    //        new DisabledAlgorithmConstraints(
    //                DisabledAlgorithmConstraints.PROPERTY_CERTPATH_DISABLED_ALGS);

    //private static final Set<CryptoPrimitive> SIG_PRIMITIVE_SET = Collections
    //        .unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));

	protected boolean rfc = false;

	public SignatureReport() {

	}

	public SignatureReport(X509Certificate cert) {
		certificates = new X509Certificate[1];
		certificates[0] = cert;
	}

	public SignatureReport(Signature[] signatures) {
		certificates = new X509Certificate[signatures.length];
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			for(int i = 0; i < signatures.length; i++) {
				certificates[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(signatures[i].toByteArray()));
			}
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

	public SignatureReport(File file) throws Exception {
		this(new JarFile(file));
	}

	public SignatureReport(InputStream inputStream) {
		this(inputStream, null);
	}

	public SignatureReport(InputStream inputStream, String signScheme) {
		this.signScheme = signScheme;
		//PKCS7 p7;
		try {
			//p7 = new PKCS7(inputStream);
			//certificates = p7.getCertificates();
			certificates = new X509Certificate[1];
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			certificates = cf.generateCertificates(inputStream).toArray(new X509Certificate[0]);
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

	public SignatureReport(JarFile jf) throws Exception {
		//JarFile jf = new JarFile(jarfile, true);
		Enumeration<JarEntry> entries = jf.entries();
		Set<CodeSigner> ss = new HashSet<>();
		byte[] buffer = new byte[8192];
		ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
		ArrayList<X509Certificate> timestampList = new ArrayList<X509Certificate>();
		while (entries.hasMoreElements()) {
			JarEntry je = entries.nextElement();
			try (InputStream is = jf.getInputStream(je)) {
				while (is.read(buffer) != -1) {
					// we just read. this will throw a SecurityException
					// if a signature/digest check fails. This also
					// populate the signers
				}
			}
			CodeSigner[] signers = je.getCodeSigners();
			if (signers != null) {
				for (CodeSigner signer: signers) {
					if (!ss.contains(signer)) {
						ss.add(signer);
						for (Certificate cert: signer.getSignerCertPath().getCertificates()) {
							certList.add((X509Certificate)cert);
						}
						Timestamp ts = signer.getTimestamp();
						if (ts != null) {
							for (Certificate cert: ts.getSignerCertPath().getCertificates()) {
								timestampList.add((X509Certificate)cert);
							}
						}
					}
				}
			} else {
				String entryName = je.getName();
				if(entryName.startsWith("META-INF/")){
					if(entryName.toUpperCase().endsWith(".RSA") || entryName.toUpperCase().endsWith(".DSA") || entryName.toUpperCase().endsWith(".EC")) {
						try {
							//certificates = new PKCS7(jf.getInputStream(je)).getCertificates();
							//certificates = new X509Certificate[1];
							CertificateFactory cf = CertificateFactory.getInstance("X509");
							certificates = cf.generateCertificates(jf.getInputStream(je)).toArray(new X509Certificate[0]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		jf.close();
		if (ss.isEmpty() && certificates == null) {
			Log.w(_RStr.NOT_A_SINGED_JAR_FILE.get());
		}
		if(!certList.isEmpty()) {
			certificates = certList.toArray(new X509Certificate[certList.size()]);
		}
		if(!timestampList.isEmpty()) {
			timestamp = timestampList.toArray(new X509Certificate[timestampList.size()]);
		}
	}

	public String getSignatureScheme() {
		return signScheme;
	}

	public interface Length { public int length(); }

    /**
     * Returns the key size of the given key object in bits.
     *
     * @param key the key object, cannot be null
     * @return the key size of the given key object in bits, or -1 if the
     *       key size is not accessible
     */
    public static final int getKeySize(Key key) {
        int size = -1;

        if (key instanceof Length) {
            try {
                Length ruler = (Length)key;
                size = ruler.length();
            } catch (UnsupportedOperationException usoe) {
                // ignore the exception
            }

            if (size >= 0) {
                return size;
            }
        }

        // try to parse the length from key specification
        /*
        if (key instanceof SecretKey) {
            SecretKey sk = (SecretKey)key;
            String format = sk.getFormat();
            if ("RAW".equals(format) && sk.getEncoded() != null) {
                size = (sk.getEncoded().length * 8);
            }   // Otherwise, it may be a unextractable key of PKCS#11, or
                // a key we are not able to handle.
        } else
        */
        if (key instanceof RSAKey) {
            RSAKey pubk = (RSAKey)key;
            size = pubk.getModulus().bitLength();
        } else if (key instanceof ECKey) {
            ECKey pubk = (ECKey)key;
            size = pubk.getParams().getOrder().bitLength();
        } else if (key instanceof DSAKey) {
            DSAKey pubk = (DSAKey)key;
            DSAParams params = pubk.getParams();    // params can be null
            size = (params != null) ? params.getP().bitLength() : -1;
        }
        /*
        else if (key instanceof DHKey) {
            DHKey pubk = (DHKey)key;
            size = pubk.getParams().getP().bitLength();
        }   // Otherwise, it may be a unextractable key of PKCS#11, or
            // a key we are not able to handle.
         */
        return size;
    }

    private static String withWeak(String alg) {
        //if (DISABLED_CHECK.permits(SIG_PRIMITIVE_SET, alg, null)) {
            return alg;
        //} else {
        //    return String.format(RStr.WITH_WEAK.get(), alg);
        //}
    }

    private static String withWeak(PublicKey key) {
        //if (DISABLED_CHECK.permits(SIG_PRIMITIVE_SET, key)) {
            return String.format(_RStr.KEY_BIT.get(),
                    getKeySize(key), key.getAlgorithm());
        //} else {
        //    return String.format(RStr.KEY_BIT_WEAK.get(),
        //            KeyUtil.getKeySize(key), key.getAlgorithm());
        //}
    }

	/**
	 * Prints a certificate in a human readable format.
	 */
	private static void printX509Cert(X509Certificate cert, PrintStream out)
			throws Exception
	{
		String pattern = null;
		Object[] source = null;

		pattern = _RStr.PATTERN_PRINT_X509_CERT.get();
        PublicKey pkey = cert.getPublicKey();
        String sigName = cert.getSigAlgName();
        // No need to warn about sigalg of a trust anchor
        //if (!isTrustedCert(cert)) {
            sigName = withWeak(sigName);
        //}
		source = new Object[] {cert.getSubjectDN().toString(),
				cert.getIssuerDN().toString(),
				cert.getSerialNumber().toString(16),
				cert.getNotBefore().toString(),
				cert.getNotAfter().toString(),
				getCertFingerPrint("MD5", cert),
				getCertFingerPrint("SHA-1", cert),
				getCertFingerPrint("SHA-256", cert),
				sigName,
				withWeak(pkey),
				cert.getVersion()
		};

		MessageFormat form = new MessageFormat(pattern);
		out.println(form.format(source));

		/*
		if (cert instanceof X509CertImpl) {
			X509CertImpl impl = (X509CertImpl)cert;
			X509CertInfo certInfo = (X509CertInfo)impl.get(X509CertImpl.NAME
					+ "." +
					X509CertImpl.INFO);
			// sun package
			CertificateExtensions exts = (CertificateExtensions)
					certInfo.get(X509CertInfo.EXTENSIONS);
			if (exts != null) {
				printExtensions(RStr.EXTENSIONS.get(), exts, out);
			}
		}
		*/
	}
/*
	private static void printExtensions(String title, CertificateExtensions exts, PrintStream out)
			throws Exception {
		int extnum = 0;
		Iterator<Extension> i1 = exts.getAllExtensions().iterator();
		Iterator<Extension> i2 = exts.getUnparseableExtensions().values().iterator();
		while (i1.hasNext() || i2.hasNext()) {
			Extension ext = i1.hasNext()?i1.next():i2.next();
			if (extnum == 0) {
				out.println();
				out.println(title);
				out.println();
			}
			out.print("#"+(++extnum)+": "+ ext);
			if (ext.getClass() == Extension.class) {
				byte[] v = ext.getExtensionValue();
				if (v.length == 0) {
					out.println(RStr.EMPTY_VALUE.get());
				} else {
					printHexDump(ext.getExtensionValue(), out);
					out.println();
				}
			}
			out.println();
		}
	}

	private static void printHexDump(byte[] dump, OutputStream out) {
		Class<?> clazz = null;
		Object instance = null;
		try {
			clazz = Class.forName("sun.security.util.HexDumpEncoder");
			instance = clazz.getConstructor().newInstance();
		} catch (Exception e) { }
		if(instance == null) {
			try {
				clazz = Class.forName("sun.misc.HexDumpEncoder");
				instance = clazz.getConstructor().newInstance();
			} catch (Exception e) { }
		}
		if(instance == null) {
			Log.e("No such any encoder : sun.misc.HexDumpEncoder, sun.security.util.HexDumpEncoder");
			return;
		}

		try {
			clazz.getMethod("encodeBuffer", byte[].class, OutputStream.class).invoke(instance, dump, out);
		} catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
*/

	/**
	 * Writes an X.509 certificate in base64 or binary encoding to an output
	 * stream.
	 */
	private void dumpCert(Certificate cert, PrintStream out)
			throws IOException, CertificateException
	{
		if (rfc) {
			out.println(BEGIN_CERT);
			out.println(Base64.getMimeEncoder().encodeToString(cert.getEncoded()));
			out.println(END_CERT);
		} else {
			out.write(cert.getEncoded()); // binary
		}
	}

	private static String getCertFingerPrint(String mdAlg, Certificate cert) throws Exception {
        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
	}


    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }

	public String getReport(X509Certificate cert) {
		if(cert == null) return "Unknown certificate format";
		Log.v(Integer.toHexString(cert.hashCode()));

		ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bufferStream);
		try {
			printX509Cert(cert, ps);
			if(signScheme != null) {
				ps.println("\n* APK Signature Scheme " + signScheme);
			}
			ps.println();
			if(rfc) {
				dumpCert(cert, ps);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bufferStream.toString();
	}

	public void setWithRCF(boolean rcf) {
		this.rfc = rcf;
	}

	public int getSize() {
		return certificates != null ? certificates.length : 0;
	}

	public int getTimestampSize() {
		return timestamp != null ? timestamp.length : 0;
	}

	public String getReport(int idx) {
		if(idx < 0 || idx >= certificates.length) return null;
		return getReport(certificates[idx]);
	}

	public String getTimeStampReport(int idx) {
		if(idx < 0 || idx >= timestamp.length) return null;
		return getReport(timestamp[idx]);
	}

	public String toByteString(Certificate cert) {
		byte[] sig = null;
		try {
			sig = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		if(sig == null) return null;

		final int N = sig.length;
		final int N2 = N*2;
		char[] text = new char[N2];
		for (int j=0; j<N; j++) {
			byte v = sig[j];
			int d = (v>>4)&0xf;
			text[j*2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
			d = v&0xf;
			text[j*2+1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
		}
		return new String(text);
	}

	public boolean contains(String algorithm, String data) {
		if((certificates == null && timestamp == null) || data == null || data.isEmpty()) {
			return false;
		}

		if(algorithm != null && !algorithm.isEmpty()
				&& !algorithm.equalsIgnoreCase("RAWDATA")) {
			for(X509Certificate cert: certificates) {
				String fingerPrint = null;
				try {
					fingerPrint = getCertFingerPrint(algorithm, cert);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(fingerPrint != null && fingerPrint.equals(data)) {
					return true;
				}
			}

			if(timestamp != null) {
				for(X509Certificate cert: timestamp) {
					String fingerPrint = null;
					try {
						fingerPrint = getCertFingerPrint(algorithm, cert);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(fingerPrint != null && fingerPrint.equals(data)) {
						return true;
					}
				}
			}
		} else {
			for(X509Certificate cert: certificates) {
				String byteStr = toByteString(cert);
				if(byteStr != null && byteStr.equals(data)) {
					return true;
				}
			}

			if(timestamp != null) {
				for(X509Certificate cert: timestamp) {
					String byteStr = toByteString(cert);
					if(byteStr != null && byteStr.equals(data)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if(certificates == null && timestamp == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if(certificates != null) {
			for(X509Certificate cert: certificates) {
				sb.append(getReport(cert)).append("\n");
			}
		}
		if(timestamp != null) {
			sb.append("\n");
			sb.append(_RStr.TIMESTAMP.get());
			sb.append("\n");
			for(X509Certificate cert: timestamp) {
				sb.append(getReport(cert)).append("\n");
			}
		}
		return sb.toString();
	}
}
