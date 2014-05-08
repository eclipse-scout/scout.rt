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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
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
   * Test for {@link DocPropertyUtility#createDoc}
   */
  @Test
  public void createDoc() {
    IFormField testField = new TestFormField();
    List<IFormField> fields = new ArrayList<IFormField>();
    fields.add(testField);
    @SuppressWarnings("unchecked")
    IDocEntityTableConfig<IFormField> config = mock(IDocEntityTableConfig.class);
    ArrayList<IDocTextExtractor<IFormField>> ex = new ArrayList<IDocTextExtractor<IFormField>>();
    ex.add(new DescriptionExtractor<IFormField>());
    when(config.getTextExtractors()).thenReturn(ex);
    IDocSection section = DocGenUtility.createDocSection(fields, config, false);
    String[][] cellTexts = section.getTable().getCellTexts();
    String cellText = cellTexts[0][0];
    assertEquals(TEST_DOC, cellText);
  }

  /**
   * A {@link AbstractFormField} for testing
   **/
  class TestFormField extends AbstractFormField {

    /**
     * test documentation
     */
    @SuppressWarnings("deprecation")
    @Override
    protected String getConfiguredDoc() {
      return TEST_DOC;
    }
  }

  private List<IDocTextExtractor<IFormField>> getTestProperties() {
    List<IDocTextExtractor<IFormField>> testProperties = new ArrayList<IDocTextExtractor<IFormField>>();
    testProperties.add(new DescriptionExtractor<IFormField>());
    testProperties.add(new SimpleTypeTextExtractor<IFormField>());
    return testProperties;
  }

}
