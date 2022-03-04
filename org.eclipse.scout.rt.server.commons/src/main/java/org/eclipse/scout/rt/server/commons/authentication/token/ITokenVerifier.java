/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.authentication.token;

import java.io.IOException;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Verifies a token against a data source like database, config.properties or others.
 */
@FunctionalInterface
@ApplicationScoped
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
   *          The decoded tokens to verify. Multiple parts are split by "-" / "." / "_" / "~" (see
   *          https://datatracker.ietf.org/doc/html/rfc6750#section-2.1)
   * @return Result of the verification; one of {@link #AUTH_OK}, {@link #AUTH_FORBIDDEN},
   *         {@link #AUTH_CREDENTIALS_REQUIRED}, {@link #AUTH_FAILED}
   */
  int verify(List<byte[]> tokenParts) throws IOException;
}
