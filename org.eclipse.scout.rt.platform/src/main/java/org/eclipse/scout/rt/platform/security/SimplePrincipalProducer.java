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
 * Producer for {@link SimplePrincipal} objects to represent authenticated users.
 * <p>
 * There are exactly two scenarios for user principals
 * <ol>
 * <li>Container manager security with container Subject and Principal: Then this facility is not used at all</li>
 * <li>Scout based principals: Then this facility is used and by default creates {@link SimplePrincipal} objects</li>
 * </ol>
 *
 * @since 5.2
 */
public class SimplePrincipalProducer implements IPrincipalProducer, IPrincipalProducer2 {

  @Override
  public Principal produce(String username) {
    return new SimplePrincipal(username);
  }

  @Override
  public Principal produce(String username, List<String> params) {
    return new SimplePrincipal(username);
  }
}
