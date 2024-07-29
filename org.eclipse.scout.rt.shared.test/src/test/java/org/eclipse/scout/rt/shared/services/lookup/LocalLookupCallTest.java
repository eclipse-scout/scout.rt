/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.TriState;
import org.junit.Test;

/**
 * JUnit test for {@link LocalLookupCall}
 */
public class LocalLookupCallTest {
  private static final Integer ROW10_KEY = Integer.valueOf(10);
  private static final String ROW10_TEXT = "lorem";
  private static final Integer ROW11_KEY = Integer.valueOf(21);
  private static final String ROW11_TEXT = "for";
  private static final Integer ROW12_KEY = Integer.valueOf(22);
  private static final String ROW12_TEXT = "ipi";
  private static final Integer ROW20_KEY = Integer.valueOf(12);
  private static final String ROW20_TEXT = "ipsum";
  private static final Integer ROW30_KEY = Integer.valueOf(13);
  private static final String ROW30_TEXT = "dolor";
  private static final Integer ROW31_KEY = Integer.valueOf(43);
  private static final String ROW31_TEXT = "mor";
  private static final Integer ROW40_KEY = Integer.valueOf(140);
  // with regex meta-chars
  private static final String ROW40_TEXT = "text with a '*' in the middle";
  private static final Integer ROW50_KEY = Integer.valueOf(150);
  private static final String ROW50_TEXT = "all regex meta-chars: ^[.${*(\\+)|?<>";

  @Test
  public void testGetDataByAll() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    assertEquals("default value for 'active' property", TriState.UNDEFINED, lc.getActive());
    List<? extends ILookupRow<Integer>> rows = lc.getDataByAll();
    assertEquals("rows length", 8, rows.size());
  }

  @Test
  public void testGetDataByAll_FilterActive() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setActive(TriState.TRUE);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByAll();
    assertEquals("rows length", 6, rows.size());
  }

  /**
   * Key lookup must return row even when it is inactive (no filtering).
   */
  @Test
  public void testGetDataByKey_InactiveRow() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setActive(TriState.TRUE);
    lc.setKey(ROW40_KEY);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByKey();
    assertEquals("rows length", 1, rows.size());
  }

  @Test
  public void testGetDataByAll_FilterInactive() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setActive(TriState.FALSE);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByAll();
    assertEquals("rows length", 2, rows.size());
  }

  @Test
  public void testGetDataByText() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    List<? extends ILookupRow<Integer>> rows = lc.getDataByText();
    assertEquals("rows length", 8, rows.size());
  }

  @Test
  public void testGetDataByTextFiltered() {
    runGetDataByTextFiltered(8, null, null);
    runGetDataByTextFiltered(4, "*or*", null);
    runGetDataByTextFiltered(4, "*or*", "*");
    runGetDataByTextFiltered(3, "*or", null);
    runGetDataByTextFiltered(2, "ip", null);
    runGetDataByTextFiltered(0, "foo", null);
    runGetDataByTextFiltered(1, "ipi", null);
    runGetDataByTextFiltered(8, "*", null);
    runGetDataByTextFiltered(8, "", null);
    runGetDataByTextFiltered(0, "°", null);
    runGetDataByTextFiltered(1, "*?*", null);
    runGetDataByTextFiltered(1, "*\\*", null);
  }

  @Test
  public void testGetDataByTextFilteredCustomWildcard() {
    runGetDataByTextFiltered(8, null, "°");
    runGetDataByTextFiltered(4, "°or°", "°");
    runGetDataByTextFiltered(3, "°or", "°");
    runGetDataByTextFiltered(2, "ip", "°");
    runGetDataByTextFiltered(0, "foo", "°");
    runGetDataByTextFiltered(1, "ipi", "°");
    runGetDataByTextFiltered(8, "°", "°");
    runGetDataByTextFiltered(8, "", "°");
    runGetDataByTextFiltered(0, "*", "°");
    runGetDataByTextFiltered(2, "°*°", "°");
    runGetDataByTextFiltered(1, "text with°*°", "°");
    runGetDataByTextFiltered(1, "°?°", "°");
    runGetDataByTextFiltered(1, "°\\°", "°");
  }

  private void runGetDataByTextFiltered(int expectedLength, String text, String wildcard) {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    if (wildcard != null) {
      lc.setWildcard(wildcard);
    }
    lc.setText(text);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByText();

    assertEquals("rows length", expectedLength, rows.size());
  }

  @Test
  public void testGetDataByTexOrder() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setWildcard("*");
    lc.setText("*");
    List<? extends ILookupRow<Integer>> rows = lc.getDataByText();
    assertEquals("lorem", rows.get(0).getText());
    assertEquals("ipsum", rows.get(1).getText());
    assertEquals("dolor", rows.get(2).getText());
  }

  @Test
  public void testGetDataByKey() {
    runGetDataByKey(0, null);
    runGetDataByKey(1, ROW10_KEY);
    runGetDataByKey(1, ROW31_KEY);
    runGetDataByKey(0, 9999999);
    runGetDataByKey(0, Integer.valueOf(0));
  }

  private void runGetDataByKey(int expectedLength, Integer key) {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setKey(key);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByKey();

    assertEquals("rows length", expectedLength, rows.size());
  }

  @Test
  public void testGetDataByRec() {
    runGetDataByRec(5, null);
    runGetDataByRec(2, ROW10_KEY);
    runGetDataByRec(0, ROW20_KEY);
    runGetDataByRec(1, ROW30_KEY);
    runGetDataByRec(0, ROW11_KEY);
    runGetDataByRec(0, 799999);
  }

  @Test
  public void testGetDataByTextHierachic() {
    P_LocalLookupCallHierarchic lc = new P_LocalLookupCallHierarchic();
    lc.setText("F");
    lc.setHierarchicalLookup(true);
    List<? extends ILookupRow<String>> rows = lc.getDataByText();

    assertEquals(5, rows.size());
    // create a simple map to test the hierarchy:
    // key -> node, value -> parent, root nodes have as parents themselves
    List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(new ImmutablePair<>("F", "A"));
    expected.add(new ImmutablePair<>("G", "F"));
    expected.add(new ImmutablePair<>("H", "F"));
    expected.add(new ImmutablePair<>("I", "H"));
    expected.add(new ImmutablePair<>("J", "H"));

    List<Pair<String, String>> actual = new ArrayList<>();
    for (ILookupRow<String> r : rows) {
      actual.add(new ImmutablePair<>(r.getKey(), r.getParentKey()));
    }

    assertTrue("compare lookup call result with expected result", CollectionUtility.equalsCollection(expected, actual, false));
  }

  @Test
  public void testRepeatingWildcards() {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    Pattern p = lc.createSearchPattern("***hello*world**test*");
    assertFalse("no repeating wildcard should be contained in the search pattern.", p.toString().contains(".*.*"));
  }

  private void runGetDataByRec(int expectedLength, Integer parent) {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setRec(parent);
    List<? extends ILookupRow<Integer>> rows = lc.getDataByRec();

    assertEquals("rows length", expectedLength, rows.size());
  }

  private class P_LocalLookupCall extends LocalLookupCall<Integer> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Integer>> execCreateLookupRows() {
      List<ILookupRow<Integer>> rows = new ArrayList<>();
      rows.add(new LookupRow<>(ROW10_KEY, ROW10_TEXT));
      rows.add(new LookupRow<>(ROW20_KEY, ROW20_TEXT));
      rows.add(new LookupRow<>(ROW30_KEY, ROW30_TEXT));
      rows.add(new LookupRow<>(ROW11_KEY, ROW11_TEXT).withParentKey(ROW10_KEY));
      rows.add(new LookupRow<>(ROW12_KEY, ROW12_TEXT).withParentKey(ROW10_KEY));
      rows.add(new LookupRow<>(ROW31_KEY, ROW31_TEXT).withParentKey(ROW30_KEY));
      rows.add(new LookupRow<>(ROW40_KEY, ROW40_TEXT).withActive(false));
      rows.add(new LookupRow<>(ROW50_KEY, ROW50_TEXT).withActive(false));
      return rows;
    }
  }

  private class P_LocalLookupCallHierarchic extends LocalLookupCall<String> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<? extends ILookupRow<String>> execCreateLookupRows() {
      return CollectionUtility.arrayList(
          new LookupRow<>("A", "A"),
          new LookupRow<>("B", "B").withParentKey("A"),
          new LookupRow<>("C", "C").withParentKey("B"),
          new LookupRow<>("D", "D").withParentKey("B"),
          new LookupRow<>("E", "E").withParentKey("C"),
          new LookupRow<>("F", "F").withParentKey("A"),
          new LookupRow<>("G", "G").withParentKey("F"),
          new LookupRow<>("H", "H").withParentKey("F"),
          new LookupRow<>("I", "I").withParentKey("H"),
          new LookupRow<>("J", "J").withParentKey("H"));
    }
  }
}
