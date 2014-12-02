/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.code;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType2;
import org.eclipse.scout.rt.server.services.common.security.AbstractClusterNotificationCoalesceTest;
import org.eclipse.scout.rt.server.services.common.security.AccessControlCacheChangedClusterNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.junit.BeforeClass;

/**
 * Tests the coalesce functionality of {@link UnloadCodeTypeCacheClusterNotification}
 */
public class UnloadCodeTypeCacheClusterNotificationCoalesceTest extends AbstractClusterNotificationCoalesceTest<UnloadCodeTypeCacheClusterNotification> {

  private static List<Class<? extends ICodeType<?, ?>>> m_types1;
  private static List<Class<? extends ICodeType<?, ?>>> m_types2;

  @BeforeClass
  public static void beforeClass() {
    m_types1 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    m_types1.add(TestCodeType1.class);
    m_types2 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    m_types2.add(TestCodeType2.class);
  }

  @Override
  protected UnloadCodeTypeCacheClusterNotification createExistingNotification() {
    return new UnloadCodeTypeCacheClusterNotification(new ArrayList<Class<? extends ICodeType<?, ?>>>(m_types1));
  }

  @Override
  protected UnloadCodeTypeCacheClusterNotification createNewNotification() {
    return new UnloadCodeTypeCacheClusterNotification(new ArrayList<Class<? extends ICodeType<?, ?>>>(m_types2));
  }

  @Override
  protected IClusterNotification createDifferentNotification() {
    return new AccessControlCacheChangedClusterNotification();
  }

  @Override
  protected boolean isCoalesceExpected() {
    return true;
  }

  @Override
  protected void checkCoalesceResultSuccess(UnloadCodeTypeCacheClusterNotification notificationToCheck) {
    List<Class<? extends ICodeType<?, ?>>> types = new ArrayList<Class<? extends ICodeType<?, ?>>>(m_types1);
    types.addAll(m_types2);
    assertEquals(types, notificationToCheck.getTypes());
  }

  @Override
  protected void checkCoalesceResultFail(UnloadCodeTypeCacheClusterNotification notificationToCheck) {
    assertEquals(m_types1, notificationToCheck.getTypes());
  }
}
