/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Doc.Filtering;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityListConfig;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.junit.Test;

/**
 * Tests for {@link DocGenUtility}
 */
public class DocGenUtilityTest {

  private static final String TEST_DOC = "testDoc";

  /**
   * Test for {@link DocGenUtility#getHeaders(List)}
   */
  @Test
  public void testGetHeaders() {
    List<IDocTextExtractor<IFormField>> testProperties = getTestProperties();
    String[] headers = DocGenUtility.getHeaders(testProperties);
    assertEquals(2, headers.length);
    assertEquals(testProperties.get(0).getHeader(), headers[0]);
    assertEquals(testProperties.get(1).getHeader(), headers[1]);

  }

  /**
   * Test for {@link DocGenUtility#getHeaders(List)}
   */
  @Test
  public void testGetHeadersNull() {
    String[] headers = DocGenUtility.getHeaders(null);
    assertEquals(0, headers.length);
  }

  /**
   * Test for {@link DocGenUtility#getHeaders(List)}
   */
  @Test
  public void testGetHeadersEmpty() {
    String[] headers = DocGenUtility.getHeaders(new ArrayList<IDocTextExtractor<IFormField>>());
    assertEquals(0, headers.length);
  }

  /**
   * Test for {@link DocGenUtility#getTexts(Object, List)}
   */
  @Test
  public void testGetPropertyRow() {
    IFormField testField = new TestFormField();
    List<IDocTextExtractor<IFormField>> testProperties = getTestProperties();
    String[] texts = DocGenUtility.getTexts(testField, testProperties);
    assertEquals(2, texts.length);
    assertEquals(TEST_DOC, texts[0]);
  }

  /**
   * Test for {@link DocGenUtility#getTexts(Object, List)}
   */
  @Test
  public void testGetPropertyRowEmpty() {
    IFormField testField = new TestFormField();
    String[] texts = DocGenUtility.getTexts(testField, null);
    assertNull(texts);
  }

  /**
   * Test for {@link DocGenUtility#isAccepted(Object, List)} considering only a field
   */
  @Test
  public void testIsAccepted() {
    testFilterAcceptance(Filtering.REJECT, false);
    testFilterAcceptance(Filtering.TRANSPARENT, false);
    testFilterAcceptance(Filtering.ACCEPT, true);
    testFilterAcceptance(Filtering.ACCEPT_REJECT_CHILDREN, true);
  }

  private void testFilterAcceptance(Filtering filtering, boolean expectAccepted) {
    IFormField testField = mock(AbstractFormField.class);
    when(testField.getLabel()).thenReturn(filtering.name());
    List<IDocFilter<IFormField>> filters = getParseLabelTestFilters();
    boolean accepted = DocGenUtility.isAccepted(testField, filters);
    assertEquals("expected isAccepted() to return " + expectAccepted + " for " + filtering.name(), expectAccepted, accepted);
  }

  /**
   * Test for {@link DocGenUtility#isAccepted(Object, List)} considering a field in a hierachy with various filtering
   * options for the GroupBox1 and the Field (Filtering for GroupBox2 is always Default=ACCEPTED
   * <p>
   * GroupBox1<br>
   * --> GroupBox2<br>
   * --> -->Field<br>
   */
  @Test
  public void testIsAcceptedHierarchic() {
    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.REJECT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.REJECT, false);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.TRANSPARENT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.TRANSPARENT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.TRANSPARENT, true);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.TRANSPARENT, true);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.ACCEPT, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.ACCEPT, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.ACCEPT, true);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.ACCEPT, true);

    testFilterAcceptanceHierarchic(Filtering.REJECT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.TRANSPARENT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT, Filtering.ACCEPT_REJECT_CHILDREN, false);
    testFilterAcceptanceHierarchic(Filtering.ACCEPT_REJECT_CHILDREN, Filtering.ACCEPT_REJECT_CHILDREN, false);
  }

  private void testFilterAcceptanceHierarchic(Filtering filteringField, Filtering filteringParentParent, boolean expectAccepted) {
    AbstractGroupBox superSuperField = mock(AbstractGroupBox.class);
    when(superSuperField.getLabel()).thenReturn(filteringParentParent.name());

    AbstractGroupBox superField = mock(AbstractGroupBox.class);
    when(superField.getParentField()).thenReturn(superSuperField);

    IFormField testField = mock(AbstractFormField.class);
    when(testField.getParentField()).thenReturn(superField);
    when(testField.getLabel()).thenReturn(filteringField.name());

    List<IDocFilter<IFormField>> filters = getParseLabelTestFilters();
    assertEquals("expected isAccepted() to return " + expectAccepted, expectAccepted, DocGenUtility.isAccepted(testField, filters));
  }

  /**
   * Test for {@link DocGenUtility#isAccepted(Object, List)} without filters
   */
  @Test
  public void testAcceptedNoFilters() {
    IFormField testField = new TestFormField();
    boolean accepted = DocGenUtility.isAccepted(testField, null);
    assertTrue(accepted);
  }

  /**
   * Test for {@link DocPropertyUtility#createDoc}
   */
  @Test
  public void createDoc() {
    IFormField testField = new TestFormField();
    List<IFormField> fields = new ArrayList<IFormField>();
    fields.add(testField);
    @SuppressWarnings("unchecked")
    IDocEntityListConfig<IFormField> config = mock(IDocEntityListConfig.class);
    ArrayList<IDocTextExtractor<IFormField>> ex = new ArrayList<IDocTextExtractor<IFormField>>();
    ex.add(new DescriptionExtractor<IFormField>());
    when(config.getTextExtractors()).thenReturn(ex);
    IDocSection section = DocGenUtility.createDocSection(fields, config);
    String[][] cellTexts = section.getTable().getCellTexts();
    String cellText = cellTexts[0][0];
    assertEquals(TEST_DOC, cellText);
  }

//
//  /**
//   * {@link DocPropertyUtility#createTableDesc(Object[], List, List)} with a filter accepting no properties.
//   */
//  @Test
//  public void createTableDescWithFilter() {
//    IFormField testField = new TestFormField();
//    IFormField[] fields = new IFormField[1];
//    fields[0] = testField;
//    List<IDocProperty<IFormField>> testProperties = getTestProperties();
//    List<IDocFilter<IFormField>> testFilters = getTestFilters(false);
//    IDocTable testTable = DocPropertyUtility.createTableDesc(fields, null, testProperties, testFilters);
//    String[][] cellTexts = testTable.getCellTexts();
//    assertEquals(0, cellTexts.length);
//  }

  /**
   * A {@link AbstractFormField} for testing
   **/
  class TestFormField extends AbstractFormField {

    /**
     * test documentation
     */
    @Override
    public String getConfiguredDoc() {
      return TEST_DOC;
    }
  }

  private List<IDocTextExtractor<IFormField>> getTestProperties() {
    List<IDocTextExtractor<IFormField>> testProperties = new ArrayList<IDocTextExtractor<IFormField>>();
    testProperties.add(new DescriptionExtractor<IFormField>());
    testProperties.add(new SimpleTypeTextExtractor<IFormField>());
    return testProperties;
  }

  private List<IDocFilter<IFormField>> getParseLabelTestFilters() {
    List<IDocFilter<IFormField>> filters = new ArrayList<IDocFilter<IFormField>>();
    IDocFilter<IFormField> testFilter = new IDocFilter<IFormField>() {
      @Override
      public Filtering accept(IFormField field) {
        String label = field.getLabel();
        if (Filtering.ACCEPT.name().equals(label)) {
          return Filtering.ACCEPT;
        }
        if (Filtering.REJECT.name().equals(label)) {
          return Filtering.REJECT;
        }
        if (Filtering.TRANSPARENT.name().equals(label)) {
          return Filtering.TRANSPARENT;
        }
        if (Filtering.ACCEPT_REJECT_CHILDREN.name().equals(label)) {
          return Filtering.ACCEPT_REJECT_CHILDREN;
        }
        return Filtering.ACCEPT;
      }
    };

    filters.add(testFilter);
    return filters;
  }

}
