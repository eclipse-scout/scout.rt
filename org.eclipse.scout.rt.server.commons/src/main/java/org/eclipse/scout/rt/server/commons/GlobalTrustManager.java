/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons;

import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PropertiesHelper;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.TrustedCertificatesProperty;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create a global {@link TrustManager}. If a certificate is not trusted by the manager, the request is
 * delegates to other installed {@link TrustManager}'s, e.g. the JAVA default keystore cacerts. Trusted certificates
 * should be placed in <i>/externalfiles/certificates/</i>. Please consider that only *.DER certificate files are
 * considered.
 */
@ApplicationScoped
public class GlobalTrustManager {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalTrustManager.class);

  private static final String PATH_CERTS = "/certificates";

  /**
   * Installs the global trustmanager for 'TLS' server socket protocol and the default trustmanager algorithm as
   * specified in the java security properties, or an implementation-specific default if no such property exists.
   */
  public void installGlobalTrustManager() {
    installGlobalTrustManager("TLS", TrustManagerFactory.getDefaultAlgorithm());
  }

  /**
   * Installs the global trustmanager for the given server socket protocol and trustmanager algorithm.
   *
   * @param protocol
   *          the server socket protocol (e.g. TLS or SSL) used to identify the protocol implementing {@link SSLContext}
   *          . This context is injected with a custom {@link TrustManager} to further check server identity with
   *          certificates located not only in keystore but also in 'externalfiles/certificates' folder.
   * @param tmAlgorithm
   *          {@link TrustManager} algorithm (e.g. SunX509 or IbmX509)
   */
  public void installGlobalTrustManager(String protocol, String tmAlgorithm) {
    X509TrustManager globalTrustManager;
    try {
      globalTrustManager = createGlobalTrustManager(tmAlgorithm, getAllTrustedCertificates());

      SSLContext sslContext = SSLContext.getInstance(protocol);
      sslContext.init(null, new TrustManager[]{globalTrustManager}, SecurityUtility.createSecureRandom());
      SSLContext.setDefault(sslContext);
    }
    catch (Exception e) {
      throw new ProcessingException("could not install global trust manager.", e);
    }
  }

  /**
   * Creates a new {@link X509TrustManager} instance but does not install it as default {@link SSLContext}. This
   * instance can be used where the default {@link SSLContext} is not used.
   * <p>
   * The default trustmanager algorithm is specified in the java security properties, or an implementation-specific
   * default if no such property exists.
   *
   * @return new {@link X509TrustManager}
   * @see #installGlobalTrustManager()
   */
  public X509TrustManager createTrustManager() {
    return createTrustManager(TrustManagerFactory.getDefaultAlgorithm());
  }

  /**
   * Creates a new {@link X509TrustManager} instance but does not install it as default {@link SSLContext}. This
   * instance can be used where the default {@link SSLContext} is not used.
   *
   * @param tmAlgorithm
   *          {@link TrustManager} algorithm (e.g. SunX509 or IbmX509)
   * @return new {@link X509TrustManager}
   * @see #installGlobalTrustManager()
   */
  public X509TrustManager createTrustManager(String tmAlgorithm) {
    try {
      return createGlobalTrustManager(tmAlgorithm, getAllTrustedCertificates());
    }
    catch (Exception e) {
      throw new ProcessingException("could not create trust manager.", e);
    }
  }

  protected List<X509Certificate> getAllTrustedCertificates() {
    List<X509Certificate> trustedCerts = new ArrayList<>();
    trustedCerts.addAll(getTrustedCertificatesInRemoteFiles());
    trustedCerts.addAll(getConfiguredTrustedCertificates());
    return trustedCerts;
  }

  protected List<X509Certificate> getConfiguredTrustedCertificates() {
    List<String> certsNames = CONFIG.getPropertyValue(TrustedCertificatesProperty.class);
    if (CollectionUtility.isEmpty(certsNames)) {
      return Collections.emptyList();
    }

    List<X509Certificate> trustedCerts = new ArrayList<>(certsNames.size());
    for (String certName : certsNames) {
      try {
        URL url = PropertiesHelper.getResourceUrl(certName);
        if (url == null) {
          LOG.warn("Configured trusted certificate '{}' could not be found.", certName);
        }
        else {
          LOG.info("Trusted certificate '{}' found.", certName);
          try (InputStream in = url.openStream()) {
            trustedCerts.add(readX509Cert(in));
          }
          LOG.info("Trusted certificate '{}' successfully installed.", certName);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to install trusted certificate '{}'.", certName, e);
      }
    }
    return trustedCerts;
  }

  protected List<X509Certificate> getTrustedCertificatesInRemoteFiles() {
    FilenameFilter certFilter = (file, name) -> name.toLowerCase().endsWith(".der");
    try {
      RemoteFile[] certRemoteFiles = BEANS.get(IRemoteFileService.class).getRemoteFiles(PATH_CERTS, certFilter, null);
      if (certRemoteFiles == null || certRemoteFiles.length < 1) {
        LOG.info("No certificates to trust in folder '{}' could be found.", PATH_CERTS);
        return Collections.emptyList();
      }
      return remoteFilesToCertificates(certRemoteFiles);
    }
    catch (RuntimeException e) {
      LOG.error("Could not access folder '{}' to import trusted certificates.", PATH_CERTS, e);
      return Collections.emptyList();
    }
  }

  protected List<X509Certificate> remoteFilesToCertificates(RemoteFile[] certRemoteFiles) {
    List<X509Certificate> trustedCerts = new ArrayList<>(certRemoteFiles.length);
    for (RemoteFile rf : certRemoteFiles) {
      try {
        LOG.info("Trusted certificate '{}' found.", rf.getName());
        try (InputStream in = rf.getDecompressedInputStream()) {
          trustedCerts.add(readX509Cert(in));
        }
        LOG.info("Trusted certificate '{}' successfully installed.", rf.getName());
      }
      catch (Exception e) {
        LOG.info("Failed to install trusted certificate '{}'.", rf.getName(), e);
      }
    }
    return trustedCerts;
  }

  protected X509TrustManager createGlobalTrustManager(String tmAlgorithm, List<X509Certificate> trustedCerts) throws NoSuchAlgorithmException, KeyStoreException {
    return new P_GlobalTrustManager(trustedCerts, tmAlgorithm);
  }

  protected X509Certificate readX509Cert(InputStream inputStream) throws CertificateException {
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) certFactory.generateCertificate(inputStream);
  }

  protected static class P_GlobalTrustManager implements X509TrustManager {
    private static final String CERTIFICATE_NOT_TRUSTED = "certificate not trusted.";

    private TrustManager[] m_installedTrustManagers;
    private final List<X509Certificate> m_trustedCerts;

    protected P_GlobalTrustManager(List<X509Certificate> trustedCerts, String tmAlgorithm) throws NoSuchAlgorithmException, KeyStoreException {
      m_trustedCerts = new ArrayList<>(trustedCerts);

      // Get default truststore. As no argument is provided for keystore, the precedence is as follows:
      // 1. evaluation of system property 'javax.net.ssl.trustStore' and check whether a truststore exists at the given location
      // 2. check, whether the following truststore exists: %JAVA_HOME%/lib/security/jssecacerts
      // 3. check, whether the following truststore exists: %JAVA_HOME%/lib/security/cacerts
      // 4. empty truststore with no certificates in it is used
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmAlgorithm);
      trustManagerFactory.init((KeyStore) null);
      m_installedTrustManagers = trustManagerFactory.getTrustManagers();

      if (m_installedTrustManagers == null) {
        m_installedTrustManagers = new TrustManager[0];
      }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
      // only ask default truststore (e.g. cacerts) for trusted certificates. Depending on the authType, different managers are possible.
      for (TrustManager installedTrustManager : m_installedTrustManagers) {
        if (installedTrustManager instanceof X509TrustManager) {
          try {
            ((X509TrustManager) installedTrustManager).checkClientTrusted(certs, authType);
          }
          catch (CertificateException e) {
            LOG.error(CERTIFICATE_NOT_TRUSTED, e);
            throw e;
          }
        }
      }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
      // check whether certificate is stored as trusted certificate in external files

      // iterates from most specific to root certificate
      for (X509Certificate candidateCert : certs) {
        if (candidateCert == null) {
          continue;
        }

        for (X509Certificate trustedCert : m_trustedCerts) {
          if (trustedCert == null) {
            continue;
          }

          try {
            // Verifies that this certificate was signed using the private key that corresponds to the specified public key.
            candidateCert.verify(trustedCert.getPublicKey());
            // check whether certificate is still valid (time)
            candidateCert.checkValidity();
            // certificate accepted
            return;
          }
          catch (GeneralSecurityException e) { // NOSONAR
            // Security exception is thrown if a certificate is not valid.
          }
        }
      }

      // ask default truststore (e.g. cacerts) whether certificate is trusted. Depending on the authType, different managers are possible.
      for (TrustManager trustManager : m_installedTrustManagers) {
        if (trustManager instanceof X509TrustManager) {
          try {
            ((X509TrustManager) trustManager).checkServerTrusted(certs, authType);
          }
          catch (CertificateException e) {
            LOG.error(CERTIFICATE_NOT_TRUSTED, e);
            throw e;
          }
        }
      }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      List<X509Certificate> trustedCertsAll = new ArrayList<>(m_trustedCerts);

      // ask default truststore (e.g. cacerts) for trusted certificates. Depending on the authType, different managers are possible.
      for (TrustManager trustManager : m_installedTrustManagers) {
        if (trustManager instanceof X509TrustManager) {
          X509Certificate[] certs = ((X509TrustManager) trustManager).getAcceptedIssuers();
          if (certs != null && certs.length > 0) {
            trustedCertsAll.addAll(Arrays.asList(certs));
          }
        }
      }
      return trustedCertsAll.toArray(new X509Certificate[0]);
    }
  }
}
