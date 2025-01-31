/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.security.Principal;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer2;
import org.eclipse.scout.rt.platform.security.JwtPrincipal;
import org.eclipse.scout.rt.platform.security.JwtPrincipalProducer;
import org.eclipse.scout.rt.platform.security.SamlPrincipal;
import org.eclipse.scout.rt.platform.security.SamlPrincipalProducer;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.security.SimplePrincipalProducer;

/**
 * Producer for {@link Principal} objects to represent authenticated users.
 * <p>
 * Based on the custom params {@link DefaultAuthToken#getCustomArgs()} produces a {@link JwtPrincipal},
 * {@link SamlPrincipal} or as default fallback a {@link SimplePrincipal}
 *
 * @since 10.0
 */
public class DefaultAuthTokenPrincipalProducer implements IPrincipalProducer2 {

  /**
   * @param username
   *     or userId
   * @param params
   *     The {@link DefaultAuthToken} by default adds as first custom param the type of principal, 'jwt' or 'saml'.
   * @return the created principal
   */
  @Override
  public Principal produce(String username, List<String> params) {
    if (params != null && params.size() > 0 && DefaultAuthTokenSigner.JWT_IDENTIFIER.equals(params.get(0))) {
      return BEANS.get(JwtPrincipalProducer.class).produce(username, params.subList(1, params.size()));
    }
    if (params != null && params.size() > 0 && DefaultAuthTokenSigner.SAML_IDENTIFIER.equals(params.get(0))) {
      return BEANS.get(SamlPrincipalProducer.class).produce(username, params.subList(1, params.size()));
    }
    return BEANS.get(SimplePrincipalProducer.class).produce(username, params);
  }
}
