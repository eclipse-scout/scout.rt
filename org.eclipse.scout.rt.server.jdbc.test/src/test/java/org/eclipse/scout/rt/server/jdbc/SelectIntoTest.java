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
package org.eclipse.scout.rt.server.jdbc;

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.fixture.SqlServiceMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ISqlService} (using the mock {@link SqlServiceMock}). Methods under test
 * {@link ISqlService#selectInto(String, Object...)}.
 */
@RunWith(PlatformTestRunner.class)
public class SelectIntoTest {

  private List<IBean<?>> m_beans;

  @Before
  public void before() throws Exception {
    SqlServiceMock sqlService = new SqlServiceMock();
    m_beans = TestingUtility.registerBeans(new BeanMetaData(ISqlService.class).withInitialInstance(sqlService).withApplicationScoped(true));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Test(expected = ProcessingException.class)
  public void testMissingOutputBind() throws Exception {
    BEANS.get(ISqlService.class).selectInto("SELECT A FROM T WHERE A = :a INTO :b", new NVPair("a", 1));
  }
}
