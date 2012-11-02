/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.test;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.services.common.test.AbstractTest;
import org.eclipse.scout.rt.shared.services.common.test.ITest;

/**
 * Abstract client test implementation.
 * <p>
 * {@link #getConfiguredProduct()} has special default value calling session symbolic name
 */
public abstract class AbstractClientTest extends AbstractTest implements ITest {

  @Override
  protected void initConfig() {
    super.initConfig();
    if (getProduct() == null) {
      IClientSession session = ClientTestUtility.getClientSession();
      if (session != null) {
        setProduct(session.getBundle().getSymbolicName());
      }
    }
  }
}
