/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.authentication.token;

import java.security.Principal;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TokenUtility;

@Bean
public class SingleStringTokenPrincipalProducer implements ITokenPrincipalProducer {

  protected Function<char[], String> m_tokenToPrincipalMapper;

  public SingleStringTokenPrincipalProducer init(Function<char[], String> tokenToPrincipalMapper) {
    Assertions.assertNotNull(tokenToPrincipalMapper, "Principal mapper must not be null");
    m_tokenToPrincipalMapper = tokenToPrincipalMapper;
    return this;
  }

  @Override
  public Principal produce(List<byte[]> tokenParts) {
    if (CollectionUtility.size(tokenParts) != 1) {
      return null;
    }

    return new SimplePrincipal(m_tokenToPrincipalMapper.apply(TokenUtility.toChars(tokenParts.get(0))));
  }
}
