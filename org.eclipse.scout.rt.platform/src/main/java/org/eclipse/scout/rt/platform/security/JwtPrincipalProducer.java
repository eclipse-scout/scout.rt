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
   *     or userId
   * @param params
   *     <br/>
   *     [0] = id_token resp. jwtTokenString<br/>
   *     [1] = access_token, Optional<br/>
   *     [2] = refresh_token, Optional<br/>
   *     [3] = oid, Optional
   * @return the new {@link Principal}
   */
  @Override
  public Principal produce(String username, List<String> params) {
    String jwtTokenString = params != null && params.size() > 0 ? params.get(0) : null;
    String accessToken = params != null && params.size() > 1 ? params.get(1) : null;
    String refreshToken = params != null && params.size() > 2 ? params.get(2) : null;
    String oid = params != null && params.size() > 3 ? params.get(3) : null;
    return produceJwt(username, jwtTokenString, accessToken, refreshToken, oid);
  }

  /**
   * @param username
   *     or userId
   * @param jwtTokenString
   *     id_token
   * @param accessToken
   *     access_token
   * @param refreshToken
   *     refresh_token
   * @param oid
   *     unique object id, UUID
   * @return a new {@link JwtPrincipal}
   */
  public JwtPrincipal produceJwt(String username, String jwtTokenString, String accessToken, String refreshToken, String oid) {
    JwtPrincipal principal = new JwtPrincipal(username, jwtTokenString);
    principal.setAccessToken(accessToken);
    principal.setRefreshToken(refreshToken);
    principal.setOid(oid);
    return principal;
  }
}
