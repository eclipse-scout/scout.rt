/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.security.fixture.AFixturePermission;
import org.eclipse.scout.rt.security.fixture.DFixturePermission;
import org.eclipse.scout.rt.security.fixture.GFixturePermission;
import org.eclipse.scout.rt.security.fixture.JFixturePermission;
import org.eclipse.scout.rt.security.fixture.NFixturePermission;
import org.eclipse.scout.rt.security.fixture.TestPermissionLevels;
import org.eclipse.scout.rt.security.fixture.UFixturePermission;
import org.eclipse.scout.rt.security.fixture.UJFixturePermission;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for
 * <ul>
 * <li>{@link AllPermissionCollection}</li>
 * <li>{@link NonePermissionCollection}</li>
 * <li>{@link DefaultPermissionCollection}</li>
 * <li>{@link AccessSupport}</li>
 * <li>{@link AbstractPermission}</li>
 * </ul>
 */
@RunWith(PlatformTestRunner.class)
public class PermissionsTest {

  private AccessSupport m_support;
  private final List<IBean<?>> m_beans = new ArrayList<>();

  @Before
  public void before() {
    m_support = new AccessSupport();
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_beans.clear();
  }

  private void registerPermissionCollection(IPermissionCollection permissionCollection) {
    IAccessControlService mock = mock(IAccessControlService.class);
    when(mock.getPermissions()).thenReturn(permissionCollection);
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(IAccessControlService.class, mock)));
  }

  private IPermissionCollection prepareDefaultPermissionCollection() {
    IPermissionCollection permissions = BEANS.get(DefaultPermissionCollection.class);

    IPermission p;
    p = new AFixturePermission();
    permissions.add(p, PermissionLevel.ALL);
    p = new GFixturePermission();
    permissions.add(p, TestPermissionLevels.GRANTED);
    p = new DFixturePermission();
    permissions.add(p, TestPermissionLevels.DENIED);
    p = new NFixturePermission();
    permissions.add(p, PermissionLevel.NONE);

    permissions.add(new JFixturePermission());

    return permissions;
  }

  private IPermissionCollection createDefaultPermissionCollection() {
    IPermissionCollection permissions = prepareDefaultPermissionCollection();
    permissions.setReadOnly();
    return permissions;
  }

  @Test
  public void testCheckWithAll() {
    registerPermissionCollection(BEANS.get(AllPermissionCollection.class));
    assertFalse(m_support.check(null));
    assertTrue(m_support.check(new AFixturePermission()));
    assertTrue(m_support.check(new GFixturePermission()));
    assertTrue(m_support.check(new DFixturePermission()));
    assertTrue(m_support.check(new NFixturePermission()));
    assertTrue(m_support.check(new UFixturePermission()));
    assertTrue(m_support.check(new JFixturePermission()));
    assertTrue(m_support.check(new UJFixturePermission()));
  }

  @Test
  public void testCheckWithNone() {
    registerPermissionCollection(BEANS.get(NonePermissionCollection.class));
    assertFalse(m_support.check(null));
    assertFalse(m_support.check(new AFixturePermission()));
    assertFalse(m_support.check(new GFixturePermission()));
    assertFalse(m_support.check(new DFixturePermission()));
    assertFalse(m_support.check(new NFixturePermission()));
    assertFalse(m_support.check(new UFixturePermission()));
    assertFalse(m_support.check(new JFixturePermission()));
    assertFalse(m_support.check(new UJFixturePermission()));
  }

  @Test
  public void testCheck() {
    registerPermissionCollection(createDefaultPermissionCollection());
    assertFalse(m_support.check(null));
    assertTrue(m_support.check(new AFixturePermission()));
    assertTrue(m_support.check(new GFixturePermission()));
    assertFalse(m_support.check(new DFixturePermission()));
    assertFalse(m_support.check(new NFixturePermission()));
    assertFalse(m_support.check(new UFixturePermission()));
    assertTrue(m_support.check(new JFixturePermission()));
    assertFalse(m_support.check(new UJFixturePermission()));
  }

  @Test
  public void testCheckAllWithAll() {
    registerPermissionCollection(BEANS.get(AllPermissionCollection.class));
    assertFalse(m_support.checkAll((Permission[]) null));
    assertFalse(m_support.checkAll(new NFixturePermission(), null));
    assertFalse(m_support.checkAll(new AFixturePermission(), null));
    assertTrue(m_support.checkAll(new AFixturePermission()));
    assertTrue(m_support.checkAll(new GFixturePermission()));
    assertTrue(m_support.checkAll(new DFixturePermission()));
    assertTrue(m_support.checkAll(new NFixturePermission()));
    assertTrue(m_support.checkAll(new UFixturePermission()));
    assertTrue(m_support.checkAll(new JFixturePermission()));
    assertTrue(m_support.checkAll(new UJFixturePermission()));

    assertTrue(m_support.checkAll(new AFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAll(new GFixturePermission(), new DFixturePermission()));
    assertTrue(m_support.checkAll(new AFixturePermission(), new GFixturePermission()));
    assertTrue(m_support.checkAll(new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAll(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAll(new GFixturePermission(), new JFixturePermission()));
    assertTrue(m_support.checkAll(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAllWithNone() {
    registerPermissionCollection(BEANS.get(NonePermissionCollection.class));
    assertFalse(m_support.checkAll((Permission[]) null));
    assertFalse(m_support.checkAll(new NFixturePermission(), null));
    assertFalse(m_support.checkAll(new AFixturePermission(), null));
    assertFalse(m_support.checkAll(new AFixturePermission()));
    assertFalse(m_support.checkAll(new GFixturePermission()));
    assertFalse(m_support.checkAll(new DFixturePermission()));
    assertFalse(m_support.checkAll(new NFixturePermission()));
    assertFalse(m_support.checkAll(new UFixturePermission()));
    assertFalse(m_support.checkAll(new JFixturePermission()));
    assertFalse(m_support.checkAll(new UJFixturePermission()));

    assertFalse(m_support.checkAll(new AFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAll(new GFixturePermission(), new DFixturePermission()));
    assertFalse(m_support.checkAll(new AFixturePermission(), new GFixturePermission()));
    assertFalse(m_support.checkAll(new DFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAll(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAll(new GFixturePermission(), new JFixturePermission()));
    assertFalse(m_support.checkAll(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAll() {
    registerPermissionCollection(createDefaultPermissionCollection());
    assertFalse(m_support.checkAll((Permission[]) null));
    assertFalse(m_support.checkAll(new NFixturePermission(), null));
    assertFalse(m_support.checkAll(new AFixturePermission(), null));
    assertTrue(m_support.checkAll(new AFixturePermission()));
    assertTrue(m_support.checkAll(new GFixturePermission()));
    assertFalse(m_support.checkAll(new DFixturePermission()));
    assertFalse(m_support.checkAll(new NFixturePermission()));
    assertFalse(m_support.checkAll(new UFixturePermission()));
    assertTrue(m_support.checkAll(new JFixturePermission()));
    assertFalse(m_support.checkAll(new UJFixturePermission()));

    assertFalse(m_support.checkAll(new AFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAll(new GFixturePermission(), new DFixturePermission()));
    assertTrue(m_support.checkAll(new AFixturePermission(), new GFixturePermission()));
    assertFalse(m_support.checkAll(new DFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAll(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAll(new GFixturePermission(), new JFixturePermission()));
    assertFalse(m_support.checkAll(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAnyWithAll() {
    registerPermissionCollection(BEANS.get(AllPermissionCollection.class));
    assertFalse(m_support.checkAny((Permission[]) null));
    assertTrue(m_support.checkAny(new NFixturePermission(), null));
    assertTrue(m_support.checkAny(new AFixturePermission(), null));
    assertTrue(m_support.checkAny(new AFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission()));
    assertTrue(m_support.checkAny(new DFixturePermission()));
    assertTrue(m_support.checkAny(new NFixturePermission()));
    assertTrue(m_support.checkAny(new UFixturePermission()));
    assertTrue(m_support.checkAny(new JFixturePermission()));
    assertTrue(m_support.checkAny(new UJFixturePermission()));

    assertTrue(m_support.checkAny(new AFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission(), new DFixturePermission()));
    assertTrue(m_support.checkAny(new AFixturePermission(), new GFixturePermission()));
    assertTrue(m_support.checkAny(new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission(), new JFixturePermission()));
    assertTrue(m_support.checkAny(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAnyWithNone() {
    registerPermissionCollection(BEANS.get(NonePermissionCollection.class));
    assertFalse(m_support.checkAny((Permission[]) null));
    assertFalse(m_support.checkAny(new NFixturePermission(), null));
    assertFalse(m_support.checkAny(new AFixturePermission(), null));
    assertFalse(m_support.checkAny(new AFixturePermission()));
    assertFalse(m_support.checkAny(new GFixturePermission()));
    assertFalse(m_support.checkAny(new DFixturePermission()));
    assertFalse(m_support.checkAny(new NFixturePermission()));
    assertFalse(m_support.checkAny(new UFixturePermission()));
    assertFalse(m_support.checkAny(new JFixturePermission()));
    assertFalse(m_support.checkAny(new UJFixturePermission()));

    assertFalse(m_support.checkAny(new AFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAny(new GFixturePermission(), new DFixturePermission()));
    assertFalse(m_support.checkAny(new AFixturePermission(), new GFixturePermission()));
    assertFalse(m_support.checkAny(new DFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAny(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertFalse(m_support.checkAny(new GFixturePermission(), new JFixturePermission()));
    assertFalse(m_support.checkAny(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAny() {
    registerPermissionCollection(createDefaultPermissionCollection());
    assertFalse(m_support.checkAny((Permission[]) null));
    assertFalse(m_support.checkAny(new NFixturePermission(), null));
    assertTrue(m_support.checkAny(new AFixturePermission(), null));
    assertTrue(m_support.checkAny(new AFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission()));
    assertFalse(m_support.checkAny(new DFixturePermission()));
    assertFalse(m_support.checkAny(new NFixturePermission()));
    assertFalse(m_support.checkAny(new UFixturePermission()));
    assertTrue(m_support.checkAny(new JFixturePermission()));
    assertFalse(m_support.checkAny(new UJFixturePermission()));

    assertTrue(m_support.checkAny(new AFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission(), new DFixturePermission()));
    assertTrue(m_support.checkAny(new AFixturePermission(), new GFixturePermission()));
    assertFalse(m_support.checkAny(new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new AFixturePermission(), new GFixturePermission(), new DFixturePermission(), new NFixturePermission()));
    assertTrue(m_support.checkAny(new GFixturePermission(), new JFixturePermission()));
    assertTrue(m_support.checkAny(new JFixturePermission(), new UJFixturePermission()));
  }

  @Test
  public void testCheckAndThrow() {
    registerPermissionCollection(createDefaultPermissionCollection());

    // nothing thrown
    m_support.checkAndThrow(new GFixturePermission());
    m_support.checkAndThrow(new AFixturePermission());
    m_support.checkAndThrow(new JFixturePermission());

    Assert.assertThrows(AccessForbiddenException.class, () -> m_support.checkAndThrow(null));
    Assert.assertThrows(AccessForbiddenException.class, () -> m_support.checkAndThrow(new NFixturePermission()));
    Assert.assertThrows(AccessForbiddenException.class, () -> m_support.checkAndThrow(new DFixturePermission()));
    Assert.assertThrows(AccessForbiddenException.class, () -> m_support.checkAndThrow(new UJFixturePermission()));
  }

  @Test
  public void testGetGrantedPermissionLevelWithAll() {
    IPermissionCollection permissions = BEANS.get(AllPermissionCollection.class);

    assertSame(PermissionLevel.UNDEFINED, permissions.getGrantedPermissionLevel(null));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new AFixturePermission()));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new GFixturePermission()));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new DFixturePermission()));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new NFixturePermission()));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new UFixturePermission()));
  }

  @Test
  public void testGetGrantedPermissionLevelWithNone() {
    IPermissionCollection permissions = BEANS.get(NonePermissionCollection.class);

    assertSame(PermissionLevel.UNDEFINED, permissions.getGrantedPermissionLevel(null));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new AFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new GFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new DFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new NFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new UFixturePermission()));
  }

  @Test
  public void testGetGrantedPermissionLevel() {
    IPermissionCollection permissions = createDefaultPermissionCollection();

    assertSame(PermissionLevel.UNDEFINED, permissions.getGrantedPermissionLevel(null));
    assertSame(PermissionLevel.ALL, permissions.getGrantedPermissionLevel(new AFixturePermission()));
    assertSame(TestPermissionLevels.GRANTED, permissions.getGrantedPermissionLevel(new GFixturePermission()));
    assertSame(TestPermissionLevels.DENIED, permissions.getGrantedPermissionLevel(new DFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new NFixturePermission()));
    assertSame(PermissionLevel.NONE, permissions.getGrantedPermissionLevel(new UFixturePermission()));

    // now add two permission more into collection
    permissions = prepareDefaultPermissionCollection();

    IPermission p;
    p = new GFixturePermission();
    permissions.add(p, TestPermissionLevels.GRANTED);
    p = new DFixturePermission();
    permissions.add(p, TestPermissionLevels.GRANTED);

    permissions.setReadOnly();
    List<Permission> permissionList = Collections.list(((PermissionCollection) permissions).elements());
    assertEquals(7, permissionList.size());

    assertSame(TestPermissionLevels.GRANTED, permissions.getGrantedPermissionLevel(new GFixturePermission()));
    assertSame(PermissionLevel.UNDEFINED, permissions.getGrantedPermissionLevel(new DFixturePermission()));
  }

  @Test
  public void testStream() {
    assertEquals(Collections.emptySet(), BEANS.get(AllPermissionCollection.class).stream().collect(Collectors.toSet()));
    assertEquals(Collections.emptySet(), BEANS.get(NonePermissionCollection.class).stream().collect(Collectors.toSet()));

    IPermissionCollection permissions = createDefaultPermissionCollection();
    assertEquals(4, permissions.stream().collect(Collectors.toSet()).size());
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == AFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == GFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == DFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == NFixturePermission.class));

    Set<IPermission> set = permissions.stream(new GFixturePermission()).collect(Collectors.toSet());
    assertEquals(1, set.size());
    assertTrue(set.iterator().next().getClass() == GFixturePermission.class);
  }

  @Test
  public void testEnumerate() {
    assertEquals(CollectionUtility.hashSet(new AllPermission()), new HashSet<>(Collections.list(BEANS.get(AllPermissionCollection.class).elements())));
    assertEquals(Collections.emptySet(), new HashSet<>(Collections.list(BEANS.get(NonePermissionCollection.class).elements())));

    List<Permission> permissions = Collections.list(((PermissionCollection) createDefaultPermissionCollection()).elements());
    assertEquals(5, permissions.size());
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == AFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == GFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == DFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == NFixturePermission.class));
    assertTrue(permissions.stream().anyMatch(p -> p.getClass() == JFixturePermission.class));
  }

  @Test
  public void testSealing() {
    IPermissionCollection permissions = createDefaultPermissionCollection();
    Assert.assertThrows(AssertionException.class, () -> permissions.add(new UJFixturePermission()));

    List<Permission> permissionList = Collections.list(((PermissionCollection) permissions).elements());
    Assert.assertThrows(AssertionException.class, () -> ((IPermission) permissionList.get(0)).setLevelInternal(PermissionLevel.ALL));
  }
}
