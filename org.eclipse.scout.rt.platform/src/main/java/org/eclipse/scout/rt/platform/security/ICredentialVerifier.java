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

import java.io.IOException;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Verifies user's credentials against a data source like database, config.properties, Active Directory, or others.
 *
 * @since 5.2
 */
@FunctionalInterface
@ApplicationScoped
public interface ICredentialVerifier {

  /**
   * Valid credentials provided.
   */
  int AUTH_OK = 1 << 0;

  /**
   * Invalid credentials provided.
   */
  int AUTH_FORBIDDEN = 1 << 1;

  /**
   * Failed to verify credentials.
   */
  int AUTH_FAILED = 1 << 2;

  /**
   * No credentials provided.
   */
  int AUTH_CREDENTIALS_REQUIRED = 1 << 3;

  /**
   * Require second factor.
   */
  int AUTH_2FA_REQUIRED = 1 << 4;

  /**
   * Attempts to verify the given credentials.
   *
   * @param username
   *          the user to verify
   * @param password
   *          the password to verify
   * @return Result of the verification; one of {@link #AUTH_OK}, {@link #AUTH_FORBIDDEN},
   *         {@link #AUTH_CREDENTIALS_REQUIRED}, {@link #AUTH_FAILED}
   * @throws IOException
   */
  int verify(String username, char[] password) throws IOException;
}
