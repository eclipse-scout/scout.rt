/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication.token;

import java.io.IOException;
import java.util.List;

/**
 * Verifies a token against a data source like database, config.properties or others.
 */
@FunctionalInterface
public interface ITokenVerifier {

  /**
   * Valid token provided.
   */
  int AUTH_OK = 1;

  /**
   * Invalid token provided.
   */
  int AUTH_FORBIDDEN = 1 << 1;

  /**
   * Failed to verify token.
   */
  int AUTH_FAILED = 1 << 2;

  /**
   * No token provided.
   */
  int AUTH_CREDENTIALS_REQUIRED = 1 << 3;

  /**
   * Attempts to verify the given token.
   *
   * @param tokenParts
   *     The decoded tokens to verify. Multiple parts are split by "-" / "." / "_" / "~" (see
   *     https://datatracker.ietf.org/doc/html/rfc6750#section-2.1)
   * @return Result of the verification; one of {@link #AUTH_OK}, {@link #AUTH_FORBIDDEN},
   * {@link #AUTH_CREDENTIALS_REQUIRED}, {@link #AUTH_FAILED}
   */
  int verify(List<byte[]> tokenParts) throws IOException;
}
