package com.nextlabs.rms.services.manager.ssl;

import com.nextlabs.rms.services.crypt.ReversibleEncryptor;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.SecureSocketFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.restlet.engine.io.IoUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

public class AxisSocketFactory implements SecureSocketFactory {

    static final Logger logger = LogManager.getLogger(AxisSocketFactory.class.getName());

    private static final String SOCKET_PROTOCOL = "TLS";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String SSL_ALGORITHM = "SunX509";
    private static final String KEYSTORE_FILE_UNSECURE = "C://NextLabs//data//temp_agent-keystore.jks";
    private static final String KEYSTORE_PASSWORD_UNSECURE = "password";
    private static final String KEYSTORE_FILE_SECURE = "C://NextLabs//data//agent-keystore.jks";
    private static final String KEYSTORE_FILE_PASSWORD = "C://NextLabs//data//agent-key.jks";
    private static final String TRUSTSTORE_FILE_SECURE = "C://NextLabs//data//agent-truststore.jks";

    private boolean isAuth;

    private KeyManagerFactory kmfUnsecure, kmfSecure;
    private TrustManager[] tmUnsecure, tmSecure;

    private KeyStore ksSecure;

    private KeyStore tsSecure;

    private String ksSecurePassword;

    protected SSLSocketFactory sslFactory = null;

    private static AxisSocketFactory instance = null;

    public AxisSocketFactory(Hashtable attributes) {
        isAuth = false;
        kmfUnsecure = null;
        kmfSecure = null;
        tmUnsecure = null;
        tmSecure = null;
        ksSecure = null;
        ksSecurePassword = null;

        try {
            setupSocketFactory();
        } catch (GeneralSecurityException e) {
            logger.error("Error initializing Axis socket.");
        } catch (IOException e) {
            logger.error("Error initializing Axis socket.");
        }

        AxisSocketFactory.instance = this;
    }

    public KeyStore getKsSecure() {
        return ksSecure;
    }

    public KeyStore getTsSecure() {
        return tsSecure;
    }

    public String getKsSecurePassword() {
        return ksSecurePassword;
    }

    public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL)
            throws Exception {

        return sslFactory.createSocket(host, port);
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    private void initKeystoreUnsecure() throws IOException, GeneralSecurityException {
        File file = new File(KEYSTORE_FILE_UNSECURE);
        InputStream istream = new FileInputStream(file);
        KeyStore kstore = KeyStore.getInstance(KEYSTORE_TYPE);
        kstore.load(istream, KEYSTORE_PASSWORD_UNSECURE.toCharArray());
        kmfUnsecure = KeyManagerFactory.getInstance(SSL_ALGORITHM);
        kmfUnsecure.init(kstore, KEYSTORE_PASSWORD_UNSECURE.toCharArray());
    }

    private void initTrustStoreUnsecure() {
        tmUnsecure = new TrustManager[ ] {
                new X509TrustManager() {
                    public X509Certificate[ ] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[ ] certs, String t) { }
                    public void checkServerTrusted(X509Certificate[ ] certs, String t) { }
                }
        };
    }

    private void initKeystoreSecure() throws IOException, GeneralSecurityException {
        InputStream istream = null;
        String password = null;
        try {
            File passwordFile = new File(KEYSTORE_FILE_PASSWORD);
            istream = new FileInputStream(passwordFile);
            password = IoUtils.toString(istream);
        } catch(IOException ie) {
            logger.error("Error while trying to read password file for client certificate.");
        } finally {
        	if (istream != null) {
            try {
                istream.close();
            } catch(IOException ioe) {
                logger.error("Error while trying t o close password file.");
            }
        	}
        }

        ReversibleEncryptor decryptor = new ReversibleEncryptor();
        try {
            password = decryptor.decrypt(password);
            password = password.substring(5);
            ksSecurePassword = password;
        } catch (Exception e) {
            // could be problem with decryption or a null password
            logger.error("Error while decrypting password.");
        }

        KeyStore kstore = KeyStore.getInstance(KEYSTORE_TYPE);

        try {
            File certFile = new File(KEYSTORE_FILE_SECURE);
            istream = new FileInputStream(certFile);
            kstore.load(istream, password.toCharArray());
            ksSecure = kstore;
        } catch(IOException ie) {
            logger.error("Error while trying to read password file for client certificate.");
        } finally {
            try {
                istream.close();
            } catch(IOException ioe) {
                logger.error("Error while trying to close password file.");
            }
        }

        kmfSecure = KeyManagerFactory.getInstance(SSL_ALGORITHM);
        kmfSecure.init(kstore, password.toCharArray());
    }

    private void initTrustStoreSecure() throws NoSuchAlgorithmException, KeyStoreException, CertificateException {
        InputStream istream = null;
        KeyStore kstore = KeyStore.getInstance(KEYSTORE_TYPE);

        try {
            File certFile = new File(TRUSTSTORE_FILE_SECURE);
            istream = new FileInputStream(certFile);
            kstore.load(istream, ksSecurePassword.toCharArray());
            tsSecure = kstore;
        } catch(IOException ie) {
            logger.error("Error while trying to read truststore file.");
        } finally {
        	if (istream != null) {
            try {
                istream.close();
            } catch(IOException ioe) {
                logger.error("Error while trying to close truststore file.");
            }
        	}
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(kstore);
        tmSecure = tmf.getTrustManagers();
    }

    private void setupSocketFactoryUnsecure() throws GeneralSecurityException, IOException {
        SSLContext sslContext = SSLContext.getInstance(SOCKET_PROTOCOL);
        if (kmfUnsecure == null) {
            initKeystoreUnsecure();
        }
        if (tmUnsecure == null) {
            initTrustStoreUnsecure();
        }
        sslContext.init(kmfUnsecure.getKeyManagers(), tmUnsecure, new SecureRandom());
        this.sslFactory = sslContext.getSocketFactory();
    }

    private void setupSocketFactorySecure() throws GeneralSecurityException, IOException {
        SSLContext sslContext = SSLContext.getInstance(SOCKET_PROTOCOL);
        if (kmfSecure == null) {
            initKeystoreSecure();
        }
        if (tmSecure == null) {
            initTrustStoreSecure();
        }
        sslContext.init(kmfSecure.getKeyManagers(), tmSecure, new SecureRandom());
        this.sslFactory = sslContext.getSocketFactory();
    }

    public void setupSocketFactory() throws GeneralSecurityException, IOException {
        if (!isAuth) {
            setupSocketFactoryUnsecure();
        } else {
            setupSocketFactorySecure();
        }
    }

    public static AxisSocketFactory getInstance() {
        return AxisSocketFactory.instance;
    }
}
