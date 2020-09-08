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
package org.eclipse.scout.rt.dataobject.lookup;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertThrows;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.eclipse.scout.rt.dataobject.lookup.fixture.FixtureData;
import org.eclipse.scout.rt.dataobject.lookup.fixture.FixtureDataLookupRestrictionDo;
import org.eclipse.scout.rt.dataobject.lookup.fixture.FixtureDataLookupRowDo;
import org.eclipse.scout.rt.dataobject.lookup.fixture.FixtureEnumLookupRestrictionDo;
import org.eclipse.scout.rt.dataobject.lookup.fixture.FixtureEnumLookupRowDo;

@RunWith(PlatformTestRunner.class)
public class LookupHelperTest {

  protected final Map<Long, FixtureData> m_testData = new LinkedHashMap<>();

  private LookupHelper m_helper;

  @Before
  public void before() {
    m_helper = new LookupHelper();
    m_testData.clear();
    m_testData.put(1L, new FixtureData(1L, "foo", "additional foo data", true));
    m_testData.put(2L, new FixtureData(2L, "bar", "additional bar data", false));
    m_testData.put(3L, new FixtureData(3L, "baz", "additional baz data", null));
  }

  @Test
  public void testFilterData() {
    LongLookupRestrictionDo restriction = BEANS.get(LongLookupRestrictionDo.class);

    // get all, no restrictions
    LookupResponse<LongLookupRowDo> lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L, 3L, 1L);

    // get by id
    restriction = BEANS.get(LongLookupRestrictionDo.class).withIds(1L, 3L);
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 3L, 1L);

    // get by text (full)
    restriction = BEANS.get(LongLookupRestrictionDo.class).withText("bar");
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L);

    // get by text (b*)
    restriction = BEANS.get(LongLookupRestrictionDo.class).withText("b*");
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L, 3L);

    // get by text (*a*)
    restriction = BEANS.get(LongLookupRestrictionDo.class).withText("*a*");
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L, 3L);

    // get by text (*o)
    restriction = BEANS.get(LongLookupRestrictionDo.class).withText("*o");
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 1L);

    // get by text (*)
    restriction = BEANS.get(LongLookupRestrictionDo.class).withText("*");
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L, 3L, 1L);

    // get by active true
    restriction = BEANS.get(LongLookupRestrictionDo.class).withActive(true);
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 1L);

    // get by active false
    restriction = BEANS.get(LongLookupRestrictionDo.class).withActive(false);
    lookupResponse = m_helper.filterData(restriction, m_testData.values().stream(), FixtureData::getId, FixtureData::getText, FixtureData::getActive, LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 2L);
  }

  @Test
  public void testFilterData_additionalFilter() {
    LookupHelper helper = new LookupHelper();
    FixtureDataLookupRestrictionDo restriction = BEANS.get(FixtureDataLookupRestrictionDo.class);
    restriction.withStartsWith("f");

    // get all, custom filter for text start with "f"
    LookupResponse<LongLookupRowDo> lookupResponse = helper.filterData(restriction,
        m_testData.values().stream(),
        FixtureData::getId,
        FixtureData::getText,
        FixtureData::getActive,
        data -> data.getText().startsWith(restriction.getStartsWith()),
        LongLookupRowDo.class);
    assertLookupResponse(lookupResponse, 1L);
  }

  @Test
  public void testFilterData_additionalMapperAndComparator() {
    LookupHelper helper = new LookupHelper();
    FixtureDataLookupRestrictionDo restriction = BEANS.get(FixtureDataLookupRestrictionDo.class);

    // get all, no restrictions, custom additional data mapper
    LookupResponse<FixtureDataLookupRowDo> lookupResponse = helper.filterData(restriction,
        m_testData.values().stream(),
        FixtureData::getId,
        FixtureData::getText,
        FixtureData::getActive,
        LookupHelper.truePredicate(),
        FixtureDataLookupRowDo.class,
        (row, data) -> row.withAdditionalData(data.getAdditionalData()),
        Comparator.comparing(AbstractLookupRowDo::getText, Comparator.reverseOrder())); // alphabetically in reverse order
    assertLookupResponse(lookupResponse, 1L, 3L, 2L);

    assertEquals("additional foo data", lookupResponse.getRows().get(0).getAdditionalData());
    assertEquals("additional baz data", lookupResponse.getRows().get(1).getAdditionalData());
    assertEquals("additional bar data", lookupResponse.getRows().get(2).getAdditionalData());
  }

  protected void assertLookupResponse(LookupResponse<LongLookupRowDo> lookupResponse, boolean expectAdditionalData, Long... expectedIds) {
    for (int i = 0; i < expectedIds.length; i++) {
      Long id = expectedIds[i];
      assertEquals(id, lookupResponse.getRows().get(i).getId());
      assertEquals(m_testData.get(id).getText(), lookupResponse.getRows().get(i).getText());
    }
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testLookupRowDoComparatorByText() {
    assertThrows(NullPointerException.class, () -> LookupHelper.lookupRowDoComparatorByText().compare(null, null));

    FixtureEnumLookupRowDo r1 = BEANS.get(FixtureEnumLookupRowDo.class);
    FixtureEnumLookupRowDo r2 = BEANS.get(FixtureEnumLookupRowDo.class);
    assertThrows(NullPointerException.class, () -> LookupHelper.lookupRowDoComparatorByText().compare(r1, r2));

    r1.withText("a");
    assertThrows(NullPointerException.class, () -> LookupHelper.lookupRowDoComparatorByText().compare(r1, r2));

    r2.withText("b");
    assertEquals(-1, LookupHelper.lookupRowDoComparatorByText().compare(r1, r2));
    assertEquals(0, LookupHelper.lookupRowDoComparatorByText().compare(r1, r1));
    assertEquals(1, LookupHelper.lookupRowDoComparatorByText().compare(r2, r1));
  }

  @Test
  public void testEnumTextResolver() {
    assertNull(LookupHelper.enumTextResolver().apply(null));
  }

  @Test
  public void testFilterEnum() {
    LookupHelper helper = new LookupHelper();

    // get all
    FixtureEnumLookupRestrictionDo restriction = BEANS.get(FixtureEnumLookupRestrictionDo.class);
    assertLookupResponse(helper.filterEnumKeepSorting(restriction, FixtureEnum.class, FixtureEnumLookupRowDo.class),
      FixtureEnum.ONE, FixtureEnum.TWO, FixtureEnum.THREE);

    // get by id
    restriction = BEANS.get(FixtureEnumLookupRestrictionDo.class).withIds(FixtureEnum.THREE);
    assertLookupResponse(helper.filterEnumKeepSorting(restriction, FixtureEnum.class, FixtureEnumLookupRowDo.class),
        FixtureEnum.THREE);

    // get by text
    restriction = BEANS.get(FixtureEnumLookupRestrictionDo.class).withText("non-existing fixture enum text");
    assertLookupResponse(helper.filterEnumKeepSorting(restriction, FixtureEnum.class, FixtureEnumLookupRowDo.class));

    // get by text using wildcard
    restriction = BEANS.get(FixtureEnumLookupRestrictionDo.class).withText("*");
    assertLookupResponse(helper.filterEnumKeepSorting(restriction, FixtureEnum.class, FixtureEnumLookupRowDo.class),
      FixtureEnum.ONE, FixtureEnum.TWO, FixtureEnum.THREE);
  }

  @Test
  public void testTextPatternPredicate() {
    Predicate<Object> nullTextPatternPredicate = m_helper.textPatternPredicate(null, null);
    assertTrue(nullTextPatternPredicate.test(null));
    assertTrue(nullTextPatternPredicate.test(new Object()));
    assertTrue(nullTextPatternPredicate.test("foo"));

    assertThrows(AssertionException.class, () -> m_helper.textPatternPredicate("a*", null));

    Predicate<String> textPatternPredicateWithIdentity = m_helper.textPatternPredicate("a*", Function.identity());
    assertFalse(textPatternPredicateWithIdentity.test(null));
    assertFalse(textPatternPredicateWithIdentity.test(""));
    assertFalse(textPatternPredicateWithIdentity.test("b"));
    assertTrue(textPatternPredicateWithIdentity.test("a"));
    assertTrue(textPatternPredicateWithIdentity.test("aa"));
    assertTrue(textPatternPredicateWithIdentity.test("ab"));

    Predicate<FixtureData> textPatternPredicateFixtureData = m_helper.textPatternPredicate("a*", FixtureData::getText);
    assertFalse(textPatternPredicateFixtureData.test(null));
    assertFalse(textPatternPredicateFixtureData.test(new FixtureData(1L, null, null, null)));
    assertFalse(textPatternPredicateFixtureData.test(new FixtureData(1L, "b", null, null)));
    assertTrue(textPatternPredicateFixtureData.test(new FixtureData(1L, "a", null, null)));
    assertTrue(textPatternPredicateFixtureData.test(new FixtureData(1L, "aa", null, null)));
    assertTrue(textPatternPredicateFixtureData.test(new FixtureData(1L, "ab", null, null)));
  }

  @SafeVarargs
  protected final <ID, LOOKUP_ROW extends AbstractLookupRowDo<LOOKUP_ROW, ID>> void assertLookupResponse(LookupResponse<LOOKUP_ROW> response, @SuppressWarnings("unchecked") ID... expectedIds) {
    assertNotNull(response);
    List<ID> actualIds = response.rows().stream()
        .map(AbstractLookupRowDo::getId)
        .collect(Collectors.toList());
    assertEquals(Arrays.asList(expectedIds), actualIds);
  }

  @Test
  public void testMaxRowCount() {
    assertEquals(LookupHelper.DEFAULT_MAX_ROWS, m_helper.maxRowCount(BEANS.get(FixtureEnumLookupRestrictionDo.class)));
    assertEquals(LookupHelper.DEFAULT_MAX_ROWS, m_helper.maxRowCount(BEANS.get(FixtureEnumLookupRestrictionDo.class).withMaxRowCount(null)));
    assertEquals(42, m_helper.maxRowCount(BEANS.get(FixtureEnumLookupRestrictionDo.class).withMaxRowCount(42)));
  }
}
