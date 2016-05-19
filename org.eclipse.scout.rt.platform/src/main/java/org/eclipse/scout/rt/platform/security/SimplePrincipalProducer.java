/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.security.Principal;

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
public class SimplePrincipalProducer implements IPrincipalProducer {

  @Override
  public Principal produce(String username) {
    return new SimplePrincipal(username);
  }
}
