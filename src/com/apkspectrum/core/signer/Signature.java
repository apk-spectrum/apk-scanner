package com.apkspectrum.core.signer;
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Opaque, immutable representation of a signing certificate associated with an
 * application package.
 * <p>
 * This class name is slightly misleading, since it's not actually a signature.
 */
public class Signature {
    private final byte[] mSignature;
    private int mHashCode;
    private boolean mHaveHashCode;
    private SoftReference<String> mStringRef;
    private Certificate[] mCertificateChain;

    /**
     * Create Signature from an existing raw byte array.
     */
    public Signature(byte[] signature) {
        mSignature = signature.clone();
        mCertificateChain = null;
    }

    /**
     * Create signature from a certificate chain. Used for backward
     * compatibility.
     *
     * @throws CertificateEncodingException
     * @hide
     */
    public Signature(Certificate[] certificateChain) throws CertificateEncodingException {
        mSignature = certificateChain[0].getEncoded();
        if (certificateChain.length > 1) {
            mCertificateChain = Arrays.copyOfRange(certificateChain, 1, certificateChain.length);
        }
    }

    private static final int parseHexDigit(int nibble) {
        if ('0' <= nibble && nibble <= '9') {
            return nibble - '0';
        } else if ('a' <= nibble && nibble <= 'f') {
            return nibble - 'a' + 10;
        } else if ('A' <= nibble && nibble <= 'F') {
            return nibble - 'A' + 10;
        } else {
            throw new IllegalArgumentException("Invalid character " + nibble + " in hex string");
        }
    }

    /**
     * Create Signature from a text representation previously returned by
     * {@link #toChars} or {@link #toCharsString()}. Signatures are expected to
     * be a hex-encoded ASCII string.
     *
     * @param text hex-encoded string representing the signature
     * @throws IllegalArgumentException when signature is odd-length
     */
    public Signature(String text) {
        final byte[] input = text.getBytes();
        final int N = input.length;

        if (N % 2 != 0) {
            throw new IllegalArgumentException("text size " + N + " is not even");
        }

        final byte[] sig = new byte[N / 2];
        int sigIndex = 0;

        for (int i = 0; i < N;) {
            final int hi = parseHexDigit(input[i++]);
            final int lo = parseHexDigit(input[i++]);
            sig[sigIndex++] = (byte) ((hi << 4) | lo);
        }

        mSignature = sig;
    }

    /**
     * Encode the Signature as ASCII text.
     */
    public char[] toChars() {
        return toChars(null, null);
    }

    /**
     * Encode the Signature as ASCII text in to an existing array.
     *
     * @param existingArray Existing char array or null.
     * @param outLen Output parameter for the number of characters written in
     * to the array.
     * @return Returns either <var>existingArray</var> if it was large enough
     * to hold the ASCII representation, or a newly created char[] array if
     * needed.
     */
    public char[] toChars(char[] existingArray, int[] outLen) {
        byte[] sig = mSignature;
        final int N = sig.length;
        final int N2 = N*2;
        char[] text = existingArray == null || N2 > existingArray.length
                ? new char[N2] : existingArray;
        for (int j=0; j<N; j++) {
            byte v = sig[j];
            int d = (v>>4)&0xf;
            text[j*2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v&0xf;
            text[j*2+1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        if (outLen != null) outLen[0] = N;
        return text;
    }

    /**
     * Return the result of {@link #toChars()} as a String.
     */
    public String toCharsString() {
        String str = mStringRef == null ? null : mStringRef.get();
        if (str != null) {
            return str;
        }
        str = new String(toChars());
        mStringRef = new SoftReference<String>(str);
        return str;
    }

    /**
     * @return the contents of this signature as a byte array.
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[mSignature.length];
        System.arraycopy(mSignature, 0, bytes, 0, mSignature.length);
        return bytes;
    }

    /**
     * Returns the public key for this signature.
     *
     * @throws CertificateException when Signature isn't a valid X.509
     *             certificate; shouldn't happen.
     * @hide
     */
    public PublicKey getPublicKey() throws CertificateException {
        final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        final ByteArrayInputStream bais = new ByteArrayInputStream(mSignature);
        final Certificate cert = certFactory.generateCertificate(bais);
        return cert.getPublicKey();
    }

    /**
     * Used for compatibility code that needs to check the certificate chain
     * during upgrades.
     *
     * @throws CertificateEncodingException
     * @hide
     */
    public Signature[] getChainSignatures() throws CertificateEncodingException {
        if (mCertificateChain == null) {
            return new Signature[] { this };
        }

        Signature[] chain = new Signature[1 + mCertificateChain.length];
        chain[0] = this;

        int i = 1;
        for (Certificate c : mCertificateChain) {
            chain[i++] = new Signature(c.getEncoded());
        }

        return chain;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                Signature other = (Signature)obj;
                return this == other || Arrays.equals(mSignature, other.mSignature);
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (mHaveHashCode) {
            return mHashCode;
        }
        mHashCode = Arrays.hashCode(mSignature);
        mHaveHashCode = true;
        return mHashCode;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Bounce the given {@link Signature} through a decode/encode cycle.
     *
     * @throws CertificateException if the before/after length differs
     *             substantially, usually a signal of something fishy going on.
     * @hide
     */
    public static Signature bounce(CertificateFactory cf, Signature s) throws CertificateException {
        final InputStream is = new ByteArrayInputStream(s.mSignature);
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        final Signature sPrime = new Signature(cert.getEncoded());

        if (Math.abs(sPrime.mSignature.length - s.mSignature.length) > 2) {
            throw new CertificateException("Bounced cert length looks fishy; before "
                    + s.mSignature.length + ", after " + sPrime.mSignature.length);
        }

        return sPrime;
    }
}
