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
package org.eclipse.scout.rt.shared.services.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.security.BasicPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link AbstractAccessControlService}
 */
@RunWith(PlatformTestRunner.class)
public class SharedAccessControlServiceTest {
  private static final String TEST_USER = "user372";
  private P_SharedAccessControlService m_accessControlService;
  private List<IBean<?>> m_registerServices;

  @Before
  public void setUp() throws Exception {
    m_accessControlService = BeanInstanceUtil.createAndInitializeBean(P_SharedAccessControlService.class);

    //Register this IAccessControlService with an higher priority than AllAccessControlService registered in CustomServerTestEnvironment
    m_registerServices = TestingUtility.registerBeans(
        new BeanMetaData(IAccessControlService.class)
            .withInitialInstance(m_accessControlService)
            .withApplicationScoped(true));
  }

  @After
  public void tearDown() throws Exception {
    TestingUtility.unregisterBeans(m_registerServices);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#getUserIdOfCurrentSubject()}
   * .
   */
  @Test
  public void testGetUserIdOfCurrentSubject() {
    String userIdOfCurrentSubject = m_accessControlService.getUserIdOfCurrentSubject();
    assertEquals("UserIdOfCurrentSubject", TEST_USER, userIdOfCurrentSubject);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#checkPermission(java.security.Permission)}
   * .
   */
  @Test
  public void testCheckPermission() {
    boolean check1 = m_accessControlService.checkPermission(new SomePermission1());
    assertEquals("SomePermission1", true, check1);
    boolean check2 = m_accessControlService.checkPermission(new OtherPermission2());
    assertEquals("OtherPermission2", false, check2);
    boolean check3 = m_accessControlService.checkPermission(null);
    assertEquals("checkPermission(null)", true, check3);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#getPermissionLevel(java.security.Permission)}
   * .
   */
  @Test
  public void testGetPermissionLevel() {
    int level1 = m_accessControlService.getPermissionLevel(new SomePermission1());
    assertEquals("SomePermission1", BasicHierarchyPermission.LEVEL_ALL, level1);
    int level2 = m_accessControlService.getPermissionLevel(new OtherPermission2());
    assertEquals("OtherPermission2", BasicHierarchyPermission.LEVEL_NONE, level2);
    int level3 = m_accessControlService.getPermissionLevel(null);
    assertEquals("checkPermission(null)", BasicHierarchyPermission.LEVEL_NONE, level3);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#getPermissions()}.
   */
  @Test
  public void testGetPermissions() {
    PermissionCollection permissions = m_accessControlService.getPermissions();
    assertNotNull(permissions);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#clearCache()}.
   */
  @Test
  public void testClearCache() throws ProcessingException {
    PermissionCollection p1 = m_accessControlService.getPermissions();
    PermissionCollection p2 = m_accessControlService.getPermissions();
    assertSame(p1, p2);
    m_accessControlService.clearCache();
    PermissionCollection p3 = m_accessControlService.getPermissions();
    assertNotSame(p1, p3);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService#clearCacheOfUserIds(java.util.Collection)}
   * .
   */
  @Test
  public void testClearCacheOfUserIds() throws ProcessingException {
    PermissionCollection p1 = m_accessControlService.getPermissions();
    PermissionCollection p2 = m_accessControlService.getPermissions();
    assertSame(p1, p2);
    m_accessControlService.clearCache(Collections.<String> emptyList());
    PermissionCollection p3 = m_accessControlService.getPermissions();
    assertSame(p1, p3);
    m_accessControlService.clearCache(Collections.<String> singletonList(null));
    PermissionCollection p4 = m_accessControlService.getPermissions();
    assertSame(p1, p4);
    m_accessControlService.clearCache(Collections.singletonList(TEST_USER));
    PermissionCollection p5 = m_accessControlService.getPermissions();
    PermissionCollection p6 = m_accessControlService.getPermissions();
    assertNotSame(p1, p5);
    assertNotSame(p1, p6);
    assertSame(p5, p6);
  }

  @IgnoreBean
  private static class P_SharedAccessControlService extends UserIdAccessControlService {

    @Override
    protected PermissionCollection execLoadPermissions(String userId) {
      Permissions permissions = new Permissions();
      permissions.add(new SomePermission1());
      return permissions;
    }

    @Override
    public String getUserIdOfCurrentSubject() {
      // bypass AccessController.getContext() for this test:
      return TEST_USER;
    }
  }

  private static class SomePermission1 extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public SomePermission1() {
      super("some.permission.1");
    }
  }

  private static class OtherPermission2 extends BasicPermission {
    private static final long serialVersionUID = 1L;

    public OtherPermission2() {
      super("other.permission.2");
    }
  }

}
