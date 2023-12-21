/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * Testcases for {@link IdInventory}
 */
public class IdInventoryTest {

  protected IdInventory m_inventory = BEANS.get(IdInventory.class);

  @Test
  public void testGetIdClass() {
    assertEquals(FixtureUuId.class, m_inventory.getIdClass("scout.FixtureUuId"));
    assertNull(m_inventory.getIdClass("scout.FixtureUuIdUnknown"));
    assertNull(m_inventory.getIdClass(null));
  }

  @Test
  public void testGetTypeNameByClass() {
    assertEquals("scout.FixtureUuId", m_inventory.getTypeName(FixtureUuId.class));
    assertNull(m_inventory.getTypeName(FixtureLongId.class));
    assertNull(m_inventory.getTypeName((Class<? extends IId>) null));
  }

  @Test
  public void testGetTypeNameByInstance() {
    assertEquals("scout.FixtureUuId", m_inventory.getTypeName(FixtureUuId.of("3c5a66be-12b4-45c6-9e59-8c31cf92dcfb")));
    assertNull(m_inventory.getTypeName(FixtureLongId.of(100L)));
    assertNull(m_inventory.getTypeName((FixtureLongId) null));
  }

  @Test
  public void testRegisterIdTypeName() {
    assertNull(m_inventory.getTypeName(FixtureMockId.class));
    assertNull(m_inventory.getTypeName(new FixtureMockId("foo")));

    m_inventory.registerIdTypeName("scout.FixtureMockId", FixtureMockId.class);
    assertEquals("scout.FixtureMockId", m_inventory.getTypeName(FixtureMockId.class));
    assertEquals("scout.FixtureMockId", m_inventory.getTypeName(new FixtureMockId(("foo"))));

    assertThrows(AssertionException.class, () -> m_inventory.registerIdTypeName("mockIdName", FixtureMockId.class));
    assertThrows(AssertionException.class, () -> m_inventory.registerIdTypeName("scout.FixtureMockId", FixtureMockId.class));
  }

  @Test
  public void testIdSignature() {
    assertTrue(m_inventory.isIdSignature(ExplicitlyOverriddenSignedId.class));
    assertTrue(m_inventory.isIdSignature(ImplicitlyOverriddenSignedId.class));
    assertTrue(m_inventory.isIdSignature(ExplicitlySignedId.class));
    assertTrue(m_inventory.isIdSignature(ImplicitlySignedId.class));

    assertFalse(m_inventory.isIdSignature(ImplicitlyUnsignedId.class));
    assertFalse(m_inventory.isIdSignature(ExplicitlyUnsignedId.class));
    assertFalse(m_inventory.isIdSignature(ImplicitlyOverriddenUnsignedId.class));
    assertFalse(m_inventory.isIdSignature(ExplicitlyOverriddenUnsignedId.class));
  }

  @IgnoreBean
  protected static final class FixtureMockId extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    private FixtureMockId(String id) {
      super(id);
    }
  }

  @IdSignature
  protected abstract static class AbstractImplicitlySignedId extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    protected AbstractImplicitlySignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  protected static class ImplicitlySignedId extends AbstractImplicitlySignedId {
    private static final long serialVersionUID = 1L;

    protected ImplicitlySignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  @IdSignature
  protected static class ExplicitlySignedId extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    protected ExplicitlySignedId(String id) {
      super(id);
    }
  }

  @IdSignature(false)
  protected abstract static class AbstractImplicitlyUnsignedId extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    protected AbstractImplicitlyUnsignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  protected static class ImplicitlyUnsignedId extends AbstractImplicitlyUnsignedId {
    private static final long serialVersionUID = 1L;

    protected ImplicitlyUnsignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  @IdSignature(false)
  protected static class ExplicitlyUnsignedId extends AbstractStringId {
    private static final long serialVersionUID = 1L;

    protected ExplicitlyUnsignedId(String id) {
      super(id);
    }
  }

  @IdSignature
  protected abstract static class AbstractImplicitlyOverriddenSignedId extends AbstractImplicitlyUnsignedId {
    private static final long serialVersionUID = 1L;

    protected AbstractImplicitlyOverriddenSignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  protected static class ImplicitlyOverriddenSignedId extends AbstractImplicitlyOverriddenSignedId {
    private static final long serialVersionUID = 1L;

    protected ImplicitlyOverriddenSignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  @IdSignature
  protected static class ExplicitlyOverriddenSignedId extends AbstractImplicitlyUnsignedId {
    private static final long serialVersionUID = 1L;

    protected ExplicitlyOverriddenSignedId(String id) {
      super(id);
    }
  }

  @IdSignature(false)
  protected abstract static class AbstractImplicitlyOverriddenUnsignedId extends AbstractImplicitlySignedId {
    private static final long serialVersionUID = 1L;

    protected AbstractImplicitlyOverriddenUnsignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  protected static class ImplicitlyOverriddenUnsignedId extends AbstractImplicitlyOverriddenUnsignedId {
    private static final long serialVersionUID = 1L;

    protected ImplicitlyOverriddenUnsignedId(String id) {
      super(id);
    }
  }

  @IgnoreBean
  @IdSignature(false)
  protected static class ExplicitlyOverriddenUnsignedId extends AbstractImplicitlySignedId {
    private static final long serialVersionUID = 1L;

    protected ExplicitlyOverriddenUnsignedId(String id) {
      super(id);
    }
  }
}
