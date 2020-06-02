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
 * SAML token principal producer
 *
 * @since 10.0
 */
public class SamlPrincipalProducer implements IPrincipalProducer, IPrincipalProducer2 {

  @Override
  public Principal produce(String username) {
    return new SamlPrincipal(username, null);
  }

  /**
   * @param username
   *          or userId
   * @param params
   *          <br/>
   *          [0] = sessionIndex
   * @return the new {@link Principal}
   */
  @Override
  public Principal produce(String username, List<String> params) {
    String sessionIndex = params != null && params.size() > 0 ? params.get(0) : null;
    return produceSaml(username, sessionIndex);
  }

  /**
   * @param username
   *          or userId
   * @param sessionIndex
   *          session_index
   * @return a new {@link SamlPrincipal}
   */
  public SamlPrincipal produceSaml(String username, String sessionIndex) {
    return new SamlPrincipal(username, sessionIndex);
  }
}
