/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheEntryFilter;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheKey;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
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

  /**
   * Tests that a client notification is created when invalidating a code type
   * {@link CodeService#invalidateCodeType(Class)}
   */
  @Test
  public void testInvlidateCodeType() {
    ICodeService codeService = BEANS.get(ICodeService.class);
    codeService.getCodeType(SomeCodeType.class);
    // verify that execLoadCodes has been invoked and reset flag, so that next execLoadCodes can be detected
    assertTrue(SomeCodeType.EXEC_LOAD_CODES_INVOKED.getAndSet(false));
    codeService.invalidateCodeType(SomeCodeType.class);
    assertFalse(SomeCodeType.EXEC_LOAD_CODES_INVOKED.get());

    // check notification
    ArgumentCaptor<InvalidateCacheNotification> notification = ArgumentCaptor.forClass(InvalidateCacheNotification.class);
    verify(m_clientNotificationReg).putTransactionalForAllNodes(notification.capture(), anyBoolean());

    Set<Class<? extends ICodeType<?, ?>>> codeTypeClasses = ((CodeTypeCacheEntryFilter) notification.getValue().getFilter()).getCodeTypeClasses();
    assertEquals("CodeType list in the notification size", 1, codeTypeClasses.size());
    assertEquals("CodeType list(0) class", SomeCodeType.class, codeTypeClasses.iterator().next());

    // get codetype manually
    codeService.getCodeType(SomeCodeType.class);
    assertTrue(SomeCodeType.EXEC_LOAD_CODES_INVOKED.get());
  }

  @Test
  public void testInvlidateCodeTypeInDifferentTransactions() {
    final ICodeService codeService = BEANS.get(ICodeService.class);

    // make sure code type is loaded
    codeService.getCodeType(SomeCodeType.class);
    // verify that execLoadCodes has been invoked and reset flag, so that next execLoadCodes can be detected
    assertTrue(SomeCodeType.EXEC_LOAD_CODES_INVOKED.getAndSet(false));

    // invalidate code type in another transaction
    ServerRunContexts.copyCurrent().run(new IRunnable() {
      @Override
      public void run() throws Exception {
        // invalidate code type
        codeService.invalidateCodeType(SomeCodeType.class);
        // verify that code type has not been loaded yet
        assertFalse(SomeCodeType.EXEC_LOAD_CODES_INVOKED.get());
      }
    });

    // verify that code type has not been loaded yet
    assertFalse(SomeCodeType.EXEC_LOAD_CODES_INVOKED.get());

    // load code type in another transaction
    ServerRunContexts.copyCurrent().run(new IRunnable() {
      @Override
      public void run() throws Exception {
        // invalidate code type
        assertNotNull(codeService.getCodeType(SomeCodeType.class));
        // verify that execLoadCodes has been invoked and reset flag, so that next execLoadCodes can be detected
        assertTrue(SomeCodeType.EXEC_LOAD_CODES_INVOKED.getAndSet(false));
      }
    });

    assertNotNull(codeService.getCodeType(SomeCodeType.class));
    assertFalse(SomeCodeType.EXEC_LOAD_CODES_INVOKED.get());
  }

  /**
   * Tests that a client notification is created when invalidating a list of code types
   * {@link CodeService#invalidateCodeTypes(List)}
   */
  @Test
  public void testInvlidateCodeTypes() {
    ICodeService codeService = BEANS.get(ICodeService.class);

    List<Class<? extends ICodeType<?, ?>>> list = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    list.add(SomeCodeType.class);
    list.add(DummyCodeType.class);
    codeService.invalidateCodeTypes(list);

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
    public final static AtomicBoolean EXEC_LOAD_CODES_INVOKED = new AtomicBoolean();

    @Override
    protected List<? extends ICodeRow<String>> execLoadCodes(Class<? extends ICodeRow<String>> codeRowType) {
      EXEC_LOAD_CODES_INVOKED.set(true);
      return super.execLoadCodes(codeRowType);
    }

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
