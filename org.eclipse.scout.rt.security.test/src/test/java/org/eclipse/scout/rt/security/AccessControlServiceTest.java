/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import static org.junit.Assert.*;

import java.io.Serial;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link AbstractAccessControlService}
 */
@RunWith(PlatformTestRunner.class)
@RunWithSubject("john")
public class AccessControlServiceTest {
  private static final User TEST_USER = BEANS.get(User.class).withUserId("john").setReadOnly();
  private P_SharedAccessControlService m_accessControlService;
  private List<IBean<?>> m_registerServices;

  @Before
  public void setUp() {
    m_accessControlService = BeanInstanceUtil.createBean(P_SharedAccessControlService.class);
    BeanInstanceUtil.initializeBeanInstance(m_accessControlService);

    //Register this IAccessControlService with an higher priority than AllAccessControlService registered in CustomServerTestEnvironment
    m_registerServices = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(IAccessControlService.class)
            .withInitialInstance(m_accessControlService)
            .withApplicationScoped(true));
  }

  @After
  public void tearDown() {
    BeanTestingHelper.get().unregisterBeans(m_registerServices);
  }

  @Test
  public void testGetUser() {
    assertNull(m_accessControlService.getUser(null));

    Subject s = new Subject();
    assertNull(m_accessControlService.getUser(s).getUserId());

    s.getPrincipals().add(new DummyPrincipal());
    assertEquals("dummy", m_accessControlService.getUser(s).getUserId());

    // first principal wins
    s.getPrincipals().add(new SimplePrincipal("simple"));
    assertEquals("dummy", m_accessControlService.getUser(s).getUserId());

    s = new Subject();
    s.getPrincipals().add(new SimplePrincipal("simple"));
    s.getPrincipals().add(new SimplePrincipal("other"));
    s.getPrincipals().add(new DummyPrincipal());
    assertEquals("simple", m_accessControlService.getUser(s).getUserId());
  }

  @Test
  public void testGetUserIdOfCurrentSubjectNoSubject() {
    assertNull(Subject.doAs(null, (PrivilegedAction<String>) () -> m_accessControlService.extractUserId(Subject.current())));
  }

  @Test
  public void testGetUserIdOfCurrentSubject() {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("username"));

    assertEquals("username", Subject.doAs(subject, (PrivilegedAction<String>) () -> m_accessControlService.extractUserId(Subject.current())));
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.security.AbstractAccessControlService#getPermissions()}.
   */
  @Test
  public void testGetPermissions() {
    IPermissionCollection permissions = m_accessControlService.getPermissions();
    assertNotNull(permissions);
  }

  @Test
  public void testGetPermissions_userMustBeReadOnly() {
    RunContexts.empty()
        // directly set thread local because withUser already enforces that the user is read-only
        .withThreadLocal(User.CURRENT, BEANS.get(User.class).withUserId("alice"))
        .run(() -> assertThrows(AssertionException.class, () -> m_accessControlService.getPermissions()));
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.security.AbstractAccessControlService#clearCache()}.
   */
  @Test
  public void testClearCache() throws ProcessingException {
    IPermissionCollection p1 = m_accessControlService.getPermissions();
    IPermissionCollection p2 = m_accessControlService.getPermissions();
    assertSame(p1, p2);
    m_accessControlService.clearCache();
    IPermissionCollection p3 = m_accessControlService.getPermissions();
    assertNotSame(p1, p3);
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.security.AbstractAccessControlService#clearCache(Collection)} .
   */
  @Test
  public void testClearCacheOfUserIds() throws ProcessingException {
    IPermissionCollection p1 = m_accessControlService.getPermissions();
    IPermissionCollection p2 = m_accessControlService.getPermissions();
    assertSame(p1, p2);
    m_accessControlService.clearCache(Collections.emptyList());
    IPermissionCollection p3 = m_accessControlService.getPermissions();
    assertSame(p1, p3);
    m_accessControlService.clearCache(Collections.singletonList(null));
    IPermissionCollection p4 = m_accessControlService.getPermissions();
    assertSame(p1, p4);
    m_accessControlService.clearCache(Collections.singletonList(TEST_USER));
    IPermissionCollection p5 = m_accessControlService.getPermissions();
    IPermissionCollection p6 = m_accessControlService.getPermissions();
    assertNotSame(p1, p5);
    assertNotSame(p1, p6);
    assertSame(p5, p6);
  }

  @IgnoreBean
  private static class P_SharedAccessControlService extends AbstractAccessControlService {

    @Override
    protected IPermissionCollection execLoadPermissions(User user) {
      DefaultPermissionCollection permissions = BEANS.get(DefaultPermissionCollection.class);
      permissions.add(new SomePermission1(), PermissionLevel.ALL);
      permissions.setReadOnly();
      return permissions;
    }

    @Override
    protected ICacheBuilder<User, IPermissionCollection> createCacheBuilder() {
      return super.createCacheBuilder()
          .withCacheId(ACCESS_CONTROL_SERVICE_CACHE_ID + ".for.test")
          .withReplaceIfExists(true);
    }
  }

  private static class SomePermission1 extends AbstractPermission {
    @Serial
    private static final long serialVersionUID = 1L;

    public SomePermission1() {
      super(PermissionId.of("some.permission.1"));
    }
  }

  private static class DummyPrincipal implements Principal {
    @Override
    public String getName() {
      return "dummy";
    }
  }
}
