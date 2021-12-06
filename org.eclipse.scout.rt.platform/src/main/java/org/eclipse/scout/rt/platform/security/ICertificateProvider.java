/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.security;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
   *          or Subject DN or Issuer DN for example "CN=host.domain.com,C=CH,ST=ZH,L=Zurich,O=My Company",
   *
   *          <pre>
  X.500 name format is
  CN: CommonName: host.domain.com
  C: CountryName: CH
  S: StateOrProvinceName: ZH
  L: Locality: Zurich
  O: Organization: My Company
  OU: OrganizationalUnit:
   *          </pre>
   *
   * @param storePass
   *          keystore password
   * @param keyPass
   *          private key password
   * @param keyBits
   *          typically 4096
   * @param validDays
   *          typically 365 days
   * @param out
   *          where to write the generated keystore to. The result is written in java key store file format.
   * @since 22.0
   */
  void createSelfSignedCertificate(String certificateAlias, String x500Name, char[] storePass, char[] keyPass, int keyBits, int validDays, OutputStream out);

  /**
   * Auto-generate a self-signed X509 certificate with public key and private key in a JKS keystore. If the keystore
   * file already exists, then it is re-used and not created.
   *
   * @param keyStorePath
   *          must be a valid and writable file path (see {@link Paths#get(String, String...)})
   * @see #createSelfSignedCertificate(String, String, char[], char[], int, int, OutputStream)
   * @since 22.0
   */
  default void autoCreateSelfSignedCertificate(String keyStorePath, char[] storePass, char[] keyPass, String certificateAlias, String x500Name) {
    if (!StringUtility.hasText(x500Name)) {
      return;
    }
    try {
      Path path = Paths.get(keyStorePath);
      if (!Files.exists(path)) {
        try (OutputStream jks = Files.newOutputStream(path)) {
          createSelfSignedCertificate(certificateAlias, x500Name, storePass, keyPass, 4096, 365, jks);
        }
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Could not create self-signed certificate '{}' with X500 name '{}' in {}", certificateAlias, x500Name, keyStorePath, e);
    }
  }
}
