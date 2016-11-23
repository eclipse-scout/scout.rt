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
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
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
  public void testGetDataByAll() throws Exception {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    List<? extends ILookupRow<Integer>> rows = lc.getDataByAll();
    assertEquals("rows lengh", 8, rows.size());
  }

  @Test
  public void testGetDataByText() throws Exception {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    List<? extends ILookupRow<Integer>> rows = lc.getDataByText();
    assertEquals("rows length", 8, rows.size());
  }

  @Test
  public void testGetDataByTextFiltered() throws Exception {
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
  public void testGetDataByTextFilteredCustomWildcard() throws Exception {
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
  public void testGetDataByKey() throws Exception {
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
  public void testGetDataByRec() throws Exception {
    runGetDataByRec(5, null);
    runGetDataByRec(2, ROW10_KEY);
    runGetDataByRec(0, ROW20_KEY);
    runGetDataByRec(1, ROW30_KEY);
    runGetDataByRec(0, ROW11_KEY);
    runGetDataByRec(0, 799999);

  }

  @Test
  public void testGetDataByTextHierachic() throws Exception {
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
      List<ILookupRow<Integer>> rows = new ArrayList<ILookupRow<Integer>>();
      rows.add(new LookupRow<Integer>(ROW10_KEY, ROW10_TEXT));
      rows.add(new LookupRow<Integer>(ROW20_KEY, ROW20_TEXT));
      rows.add(new LookupRow<Integer>(ROW30_KEY, ROW30_TEXT));
      rows.add(new LookupRow<Integer>(ROW11_KEY, ROW11_TEXT).withParentKey(ROW10_KEY));
      rows.add(new LookupRow<Integer>(ROW12_KEY, ROW12_TEXT).withParentKey(ROW10_KEY));
      rows.add(new LookupRow<Integer>(ROW31_KEY, ROW31_TEXT).withParentKey(ROW30_KEY));
      rows.add(new LookupRow<Integer>(ROW40_KEY, ROW40_TEXT));
      rows.add(new LookupRow<Integer>(ROW50_KEY, ROW50_TEXT));
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
