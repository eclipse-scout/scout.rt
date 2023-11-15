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
   * Create a self-signed X509 certificate with public key and private key in a JKS keystore.
   * <p>
   * Similar to: openssl req -nodes -newkey rsa:4096 -days 3650 -x509 -keyout cert_private.key -out cert_public.pem
   *
   * @param certificateAlias
   *          is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *          or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *          format is:
   *
   *          <pre>
  CN: CommonName: host.domain.com<br>
  C: CountryName: CH<br>
  S: StateOrProvinceName: ZH<br>
  L: Locality: Zurich<br>
  O: Organization: My Company<br>
  OU: OrganizationalUnit<br>
   *          </pre>
   *
   * @param storePass
   *          the password used to unlock the keystore, or {@code null}.
   * @param keyPass
   *          the password to protect the key, or {@code null}.
   * @param keyBits
   *          typically 4096
   * @param validDays
   *          typically 365 days
   * @since 22.0
   */
  KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int keyBits, int validDays);

  /**
   * Create a self-signed X509 certificate with public key and private key in a JKS keystore.
   * <p>
   * Similar to: openssl req -nodes -newkey rsa:4096 -days 3650 -x509 -keyout cert_private.key -out cert_public.pem
   *
   * @param certificateAlias
   *     is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *     or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *     format is:
   *
   *     <pre>
   *     CN: CommonName: host.domain.com<br>
   *     C: CountryName: CH<br>
   *     S: StateOrProvinceName: ZH<br>
   *     L: Locality: Zurich<br>
   *     O: Organization: My Company<br>
   *     OU: OrganizationalUnit<br>
   *              </pre>
   * @param storePass
   *     the password used to unlock the keystore, or {@code null}.
   * @param keyPass
   *     the password to protect the key, or {@code null}.
   * @since 22.0
   */
  default KeyStore createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass) {
    return createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, 4096, 365);
  }

  /**
   * Create a self-signed X509 certificate with public key and private key in a JKS keystore. The Keystore will be
   * written to the given {@link OutputStream}.
   * <p>
   * Similar to: openssl req -nodes -newkey rsa:4096 -days 3650 -x509 -keyout cert_private.key -out cert_public.pem
   *
   * @param certificateAlias
   *          is the alias used in the keystore for accessing the certificate, this is not the certificate name (DN)
   * @param x500Name
   *          or Subject DN or Issuer DN. For example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company". X.500 name
   *          format is:
   *
   *          <pre>
  CN: CommonName: host.domain.com<br>
  C: CountryName: CH<br>
  S: StateOrProvinceName: ZH<br>
  L: Locality: Zurich<br>
  O: Organization: My Company<br>
  OU: OrganizationalUnit<br>
   *          </pre>
   *
   * @param storePass
   *          the password used to unlock the keystore, or {@code null}.
   * @param keyPass
   *          the password to protect the key, or {@code null}.
   * @param keyBits
   *          typically 4096
   * @param validDays
   *          typically 365 days
   * @param out
   *          where to write the generated keystore to. The result is written in java key store file format.
   * @since 22.0
   */
  default void createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int keyBits, int validDays, OutputStream out) {
    try {
      KeyStore ks = createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, keyBits, validDays);
      ks.store(out, storePass);
    }
    catch (IOException | GeneralSecurityException e) {
      throw new ProcessingException("Error creating self signed certificate with alias '{}'.", certificateAlias, e);
    }
  }

  /**
   * If the keyStorePath given already exists, this method does nothing. Otherwise, a new keystore with a self-signed
   * X509 certificate is created in this file.
   *
   * @param keyStorePath
   *          must be a valid URI pointing to a file on the local file system. E.g.
   *          file:///C:/Users/usr/Desktop/my-store.jks
   * @param x500Name
   *          Must be a valid x500 name (see {@link #createSelfSignedCertificate(String, String, char[], char[])} for
   *          details).
   * @see #createSelfSignedCertificate(String, String, char[], char[], int, int, OutputStream)
   * @since 22.0
   */
  default void autoCreateSelfSignedCertificate(String keyStorePath, char[] storePass, char[] keyPass, String certificateAlias, String x500Name) {
    if (!StringUtility.hasText(x500Name)) {
      return;
    }
    try {
      Path path = Paths.get(URI.create(keyStorePath));
      if (Files.exists(path)) {
        return;
      }
      try (OutputStream jks = new BufferedOutputStream(Files.newOutputStream(path))) {
        createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, 4096, 365, jks);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Could not create self-signed certificate '{}' with X500 name '{}' in '{}'.", certificateAlias, x500Name, keyStorePath, e);
    }
  }
}
