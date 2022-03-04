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

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TokenUtility;

public class SingleStringTokenVerifier implements ITokenVerifier {

  protected Predicate<char[]> m_verifier;

  public SingleStringTokenVerifier init(Predicate<char[]> verifier) {
    Assertions.assertNotNull(verifier, "Verifier must not be null");
    m_verifier = verifier;
    return this;
  }

  @Override
  public int verify(List<byte[]> tokenParts) {
    if (CollectionUtility.size(tokenParts) != 1) {
      return ITokenVerifier.AUTH_FAILED;
    }
    return m_verifier.test(TokenUtility.toChars(tokenParts.get(0))) ? ITokenVerifier.AUTH_OK : ITokenVerifier.AUTH_FORBIDDEN;
  }
}
