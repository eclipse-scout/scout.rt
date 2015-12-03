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
package org.eclipse.scout.rt.server.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheEntryFilter;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheKey;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

/**
 * Test for {@link ICodeService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class CodeServiceTest {
  @BeanMock
  private ClientNotificationRegistry m_clientNotificationReg;

  @Test
  public void testCodeTypeRemoteAccess() {
    Object codetype = BEANS.get(IRemoteCacheService.class).get(CodeService.class.getName(), new CodeTypeCacheKey(SomeCodeType.class));
    assertNotNull(codetype);
    assertEquals(BEANS.get(SomeCodeType.class), codetype);
  }

  /**
   * Tests that a client notification is created when reloading a code type {@link CodeService#reloadCodeType}
   */
  @Test
  public void testReloadCodeType() {
    ICodeService codeService = BEANS.get(ICodeService.class);
    codeService.reloadCodeType(SomeCodeType.class);

    ArgumentCaptor<InvalidateCacheNotification> notification = ArgumentCaptor.forClass(InvalidateCacheNotification.class);
    verify(m_clientNotificationReg).putTransactionalForAllNodes(notification.capture(), anyBoolean());

    Set<Class<? extends ICodeType<?, ?>>> codeTypeClasses = ((CodeTypeCacheEntryFilter) notification.getValue().getFilter()).getCodeTypeClasses();
    assertEquals("CodeType list in the notification size", 1, codeTypeClasses.size());
    assertEquals("CodeType list(0) class", SomeCodeType.class, codeTypeClasses.iterator().next());
  }

  @Test
  public void testReloadCodeTypes() {
    ICodeService codeService = BEANS.get(ICodeService.class);

    List<Class<? extends ICodeType<?, ?>>> list = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    list.add(SomeCodeType.class);
    list.add(DummyCodeType.class);
    codeService.reloadCodeTypes(list);

    ArgumentCaptor<InvalidateCacheNotification> notification = ArgumentCaptor.forClass(InvalidateCacheNotification.class);
    verify(m_clientNotificationReg).putTransactionalForAllNodes(notification.capture(), anyBoolean());

    Set<Class<? extends ICodeType<?, ?>>> codeTypeClasses = ((CodeTypeCacheEntryFilter) notification.getValue().getFilter()).getCodeTypeClasses();
    assertEquals("CodeType list in the notification size", 2, codeTypeClasses.size());
    for (Class<? extends ICodeType<?, ?>> codeTypeClass : codeTypeClasses) {
      assertTrue("CodeTypes not invalidated", codeTypeClass == SomeCodeType.class || codeTypeClass == DummyCodeType.class);
    }
  }

  public static class SomeCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 100L;
    }
  }

  public static class DummyCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 500L;
    }
  }
}
