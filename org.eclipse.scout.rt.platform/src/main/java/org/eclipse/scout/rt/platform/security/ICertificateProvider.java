/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Bean
public interface ICertificateProvider {

  /**
   * Approximately 5 years.
   */
  int DEFAULT_CERTIFICATE_VALID_DAYS = 5 * 365;

  /**
   * Create a self-signed X509 certificate with public key and private key in a Java {@link KeyStore}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN).
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param validDays
   *     Days until expiration.
   * @param subjectAlternativeNames
   *     additional dns names for which the certificate should be valid. The common name (CN of the x500Name) is always automatically included.
   */
  KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int validDays, String[] subjectAlternativeNames);

  /**
   * Create a self-signed X509 certificate with public key and private key in a Java {@link KeyStore}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN).
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param validDays
   *     Days until expiration.
   */
  default KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int validDays) {
    return createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, validDays, (String[]) null);
  }

  /**
   * Create a self-signed X509 certificate with public key and private key in a Java {@link KeyStore}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN).
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param subjectAlternativeNames
   *     additional dns names for which the certificate should be valid. The common name (CN of the x500Name) is always automatically included.
   */
  default KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, String[] subjectAlternativeNames) {
    return createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, DEFAULT_CERTIFICATE_VALID_DAYS, subjectAlternativeNames);
  }

  /**
   * Create a self-signed X509 certificate with public key and private key in a Java {@link KeyStore}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN).
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   */
  default KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass) {
    return createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, DEFAULT_CERTIFICATE_VALID_DAYS);
  }

  /**
   * Create a self-signed X509 certificate in a JKS keystore. The Keystore will be written to the given {@link OutputStream}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param validDays
   *     Days until expiration.
   * @param subjectAlternativeNames
   *     additional dns names for which the certificate should be valid. The common name (CN of the x500Name) is always automatically included.
   * @param out
   *     where to write the generated keystore to. The result is written in java key store file format.
   */
  default void createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int validDays, String[] subjectAlternativeNames, OutputStream out) {
    try {
      KeyStore ks = createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, validDays, subjectAlternativeNames);
      ks.store(out, storePass);
    }
    catch (IOException | GeneralSecurityException e) {
      throw new ProcessingException("Error creating self signed certificate with alias '{}'.", certificateAlias, e);
    }
  }

  /**
   * Create a self-signed X509 certificate in a JKS keystore. The Keystore will be written to the given {@link OutputStream}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param subjectAlternativeNames
   *     additional dns names for which the certificate should be valid. The common name (CN of the x500Name) is always automatically included.
   * @param out
   *     where to write the generated keystore to. The result is written in java key store file format.
   */
  default void createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, String[] subjectAlternativeNames, OutputStream out) {
    createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, DEFAULT_CERTIFICATE_VALID_DAYS, subjectAlternativeNames, out);
  }

  /**
   * Create a self-signed X509 certificate in a JKS keystore. The Keystore will be written to the given {@link OutputStream}.
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:<br>
   *     <code>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *     </code>
   * @param storePass
   *     the password used to unlock the keystore.
   * @param keyPass
   *     the password to protect the key.
   * @param validDays
   *     Days until expiration.
   * @param out
   *     where to write the generated keystore to. The result is written in java key store file format.
   */
  default void createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int validDays, OutputStream out) {
    createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, validDays, null, out);
  }

  /**
   * If the keyStorePath given already exists, this method does nothing. Otherwise, a new keystore with a self-signed X509 certificate is created in this file.
   *
   * @param keyStorePath
   *     must be a valid URI pointing to a file on the local file system. E.g. file:///C:/Users/usr/Desktop/my-store.jks
   * @see #createSelfSignedCertificate(String, String, char[], char[], int, String[], OutputStream)
   */
  default void autoCreateSelfSignedCertificate(String keyStorePath, char[] storePass, char[] keyPass, String certificateAlias, String x500Name) {
    autoCreateSelfSignedCertificate(keyStorePath, storePass, keyPass, certificateAlias, x500Name, null);
  }

  /**
   * If the keyStorePath given already exists, this method does nothing. Otherwise, a new keystore with a self-signed X509 certificate is created in this file.
   *
   * @param keyStorePath
   *     must be a valid URI pointing to a file on the local file system. E.g. file:///C:/Users/usr/Desktop/my-store.jks
   * @see #createSelfSignedCertificate(String, String, char[], char[], int, String[], OutputStream)
   */
  default void autoCreateSelfSignedCertificate(String keyStorePath, char[] storePass, char[] keyPass, String certificateAlias, String x500Name, String[] subjectAlternativeNames) {
    if (!StringUtility.hasText(x500Name)) {
      return;
    }
    try {
      Path path = Paths.get(URI.create(keyStorePath));
      if (Files.exists(path)) {
        return;
      }
      Files.createDirectories(path.getParent());
      try (OutputStream jks = new BufferedOutputStream(Files.newOutputStream(path))) {
        createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, DEFAULT_CERTIFICATE_VALID_DAYS, subjectAlternativeNames, jks);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Could not create self-signed certificate '{}' with X500 name '{}' in '{}'.", certificateAlias, x500Name, keyStorePath, e);
    }
  }
}
