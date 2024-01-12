/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Provider class supporting legacy features for encryption & decryption, hashing and creation of random numbers,
 * message authentication codes and digital signatures. This class exists only for backward compatibility reasons.
 * <p>
 * <b>Important:</b> Do not use this class anymore. Try to use {@link ISecurityProvider} wherever possible.
 * </p>
 *
 * @since 24.1
 */
@ApplicationScoped
public interface ILegacySecurityProvider {

  /**
   * @see #createHash(InputStream, byte[], int)
   */
  default byte[] createHash(byte[] data, byte[] salt) {
    Assertions.assertNotNull(data, "no data provided");
    return createHash(new ByteArrayInputStream(data), salt, 3557 /* number of default cycles for backwards compatibility */);
  }

  /**
   * Creates a hash for the given data using the given salt.
   * <p>
   * <b>Important:</b> For hashing of passwords use {@link ISecurityProvider#createPasswordHash(char[], byte[])}!
   * </p>
   * <p><b>Important 2:</b> For "normal" hashing use {@link SecurityUtility#hash(byte[])}</p>
   *
   * @param data
   *     The {@link InputStream} providing the data to hash.
   * @param salt
   *     the salt to use or {@code null} if not salt should be used (not recommended!). Use
   *     {@link ISecurityProvider#createSecureRandomBytes(int)} to generate a random salt per instance.
   * @param iterations
   *     the number of hashing iterations. There is always at least one cycle executed.
   * @return the hash
   * @throws AssertionException
   *     If data is {@code null}.
   * @throws ProcessingException
   *     If there is an error creating the hash
   */
  byte[] createHash(InputStream data, byte[] salt, int iterations);
}
