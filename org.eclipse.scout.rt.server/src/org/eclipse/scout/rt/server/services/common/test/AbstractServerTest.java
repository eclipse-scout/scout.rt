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
package org.eclipse.scout.rt.server.services.common.test;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.services.common.test.AbstractTest;
import org.eclipse.scout.rt.shared.services.common.test.ITest;

/**
 * 
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 *
 * Abstract client test implementation.
 * <p>
 * {@link #getConfiguredProduct()} has special default value calling session symbolic name
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractServerTest extends AbstractTest implements ITest {

  @Override
  protected void initConfig() {
    super.initConfig();
    if (getProduct() == null) {
      IServerSession session = ServerTestUtility.getServerSession();
      if (session != null && session.getBundle() != null) {
        setProduct(session.getBundle().getSymbolicName());
      }
    }
  }
}
