/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.services.lookup;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.fixture.FixtureHierarchicalLookupRowDo;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;
import org.eclipse.scout.rt.dataobject.lookup.LookupResponse;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractRestLookupCallTest {

  public static final FixtureUuId TEST_SETTING_ID_1 = FixtureUuId.of("06469416-545c-449b-8440-4acffcf69063");
  public static final FixtureUuId TEST_SETTING_ID_2 = FixtureUuId.of("0768a234-bbaf-47fa-a174-f25a92855f20");

  @Test
  public void testClone() {
    P_FixtureUuIdLookupCall call = new P_FixtureUuIdLookupCall();
    call.setText("ABC");
    P_FixtureUuIdLookupCall call2 = (P_FixtureUuIdLookupCall) call.copy();
    assertNotSame(call, call2);
    assertNotSame(call.getRestriction(), call2.getRestriction());
    assertEquals("ABC", call2.getText());
  }

  @Test
  public void testSetters() {
    P_FixtureUuIdLookupCall call = new P_FixtureUuIdLookupCall();

    call.setText("ABC");
    assertEquals("ABC", call.getText());

    call.setKey(TEST_SETTING_ID_1);
    assertEquals(1, call.getKeys().size());
    assertEquals(TEST_SETTING_ID_1, call.getKey());

    call.setKey(TEST_SETTING_ID_2);
    assertEquals(1, call.getKeys().size());
    assertEquals(TEST_SETTING_ID_2, call.getKey());

    call.setKeys(TEST_SETTING_ID_1, TEST_SETTING_ID_2);
    assertEquals(2, call.getKeys().size());
    assertEquals(TEST_SETTING_ID_1, call.getKey());

    call.setKey(TEST_SETTING_ID_2);
    assertEquals(1, call.getKeys().size());
    assertEquals(TEST_SETTING_ID_2, call.getKey());

    call.setKey(null);
    assertNull(call.getKey());
    assertEquals(0, call.getKeys().size());

    assertFalse(call.getRestriction().active().exists());
    call.setActive(null);
    assertEquals(TriState.UNDEFINED, call.getActive());
    assertTrue(call.getRestriction().active().exists());
    assertNull(call.getRestriction().active().get());
    call.setActive(TriState.TRUE);
    assertEquals(TriState.TRUE, call.getActive());
    assertTrue(call.getRestriction().active().exists());
    assertEquals(Boolean.TRUE, call.getRestriction().active().get());
    call.setActive(TriState.FALSE);
    assertEquals(TriState.FALSE, call.getActive());
    assertTrue(call.getRestriction().active().exists());
    assertEquals(Boolean.FALSE, call.getRestriction().active().get());
    call.setActive(TriState.UNDEFINED);
    assertEquals(TriState.UNDEFINED, call.getActive());
    assertTrue(call.getRestriction().active().exists());
    assertNull(call.getRestriction().active().get());
  }

  @Test
  public void testTransformRows() {
    P_FixtureUuIdLookupCall call = new P_FixtureUuIdLookupCall();
    FixtureHierarchicalLookupRowDo parentRow = createRow();
    FixtureHierarchicalLookupRowDo childRow = createRow().withParentId(parentRow.getId());

    ILookupRow<FixtureUuId> parentLookupRow = call.transformLookupRow(parentRow);
    assertLookupRow(parentRow, parentLookupRow, null);

    ILookupRow<FixtureUuId> childLookupRow = call.transformLookupRow(childRow);
    assertLookupRow(childRow, childLookupRow, childRow.getParentId());

    FixtureUuIdLookupRowDo fixtureRow = BEANS.get(FixtureUuIdLookupRowDo.class).withId(FixtureUuId.create());
    ILookupRow<FixtureUuId> nonHierarchicalRow = call.transformLookupRow(fixtureRow);
    assertLookupRow(fixtureRow, nonHierarchicalRow, null);
  }

  protected <ID> void assertLookupRow(AbstractLookupRowDo<?, ID> expected, ILookupRow<ID> actual, ID expectedParentId) {
    assertEquals(expected.getId(), actual.getKey());
    assertEquals(expected.getText(), actual.getText());
    assertEquals(expected.getActive(), actual.isActive());
    assertEquals(expected.getEnabled(), actual.isEnabled());
    assertEquals(expectedParentId, actual.getParentKey());
  }

  protected FixtureHierarchicalLookupRowDo createRow() {
    return BEANS.get(FixtureHierarchicalLookupRowDo.class)
        .withId(FixtureUuId.create())
        .withText("Mock");
  }

  @IgnoreBean
  static class P_FixtureUuIdLookupCall extends AbstractRestLookupCall<FixtureUuIdLookupRestrictionDo, FixtureUuId> {
    private static final long serialVersionUID = 1L;

    @Override
    protected Function<FixtureUuIdLookupRestrictionDo, LookupResponse<? extends AbstractLookupRowDo<?, FixtureUuId>>> remoteCall() {
      // Not used in this test
      return null;
    }
  }

  @IgnoreBean
  static class P_FixtureUuIdHierachicalLookupCall extends AbstractRestLookupCall<FixtureUuIdLookupRestrictionDo, FixtureUuId> {
    private static final long serialVersionUID = 1L;

    @Override
    protected Function<FixtureUuIdLookupRestrictionDo, LookupResponse<? extends AbstractLookupRowDo<?, FixtureUuId>>> remoteCall() {
      // Not used in this test
      return null;
    }
  }
}
