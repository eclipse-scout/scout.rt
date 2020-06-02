/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.security;

import java.security.Principal;
import java.util.List;

/**
 * JSON web token principal producer
 *
 * @since 10.0
 */
public class JwtPrincipalProducer implements IPrincipalProducer, IPrincipalProducer2 {

  @Override
  public Principal produce(String username) {
    return new JwtPrincipal(username, null);
  }

  /**
   * @param username
   *          or userId
   * @param params
   *          <br/>
   *          [0] = id_token resp. jwtTokenString<br/>
   *          [1] = access_token, Optional [2] = refresh_token, Optional
   * @return the new {@link Principal}
   */
  @Override
  public Principal produce(String username, List<String> params) {
    String jwtTokenString = params != null && params.size() > 0 ? params.get(0) : null;
    String accessToken = params != null && params.size() > 1 ? params.get(1) : null;
    String refreshToken = params != null && params.size() > 2 ? params.get(2) : null;
    return produceJwt(username, jwtTokenString, accessToken, refreshToken);
  }

  /**
   * @param username
   *          or userId
   * @param jwtTokenString
   *          id_token
   * @param accessToken
   *          access_token
   * @param refreshToken
   *          refresh_token
   * @return a new {@link JwtPrincipal}
   */
  public JwtPrincipal produceJwt(String username, String jwtTokenString, String accessToken, String refreshToken) {
    JwtPrincipal principal = new JwtPrincipal(username, jwtTokenString);
    principal.setAccessToken(accessToken);
    principal.setRefreshToken(refreshToken);
    return principal;
  }
}
