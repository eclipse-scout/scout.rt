/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic.document;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.PlainDocument;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link BasicDocumentFilter}
 */
public class BasicDocumentFilterTest {

  private BasicDocumentFilter m_filter;
  private FilterBypass m_bypass;

  @Before
  public void setup() {
    m_filter = new BasicDocumentFilter(4);
    m_bypass = mock(FilterBypass.class);
    when(m_bypass.getDocument()).thenReturn(new PlainDocument());
  }

  /**
   * {@link BasicDocumentFilter#insertString(FilterBypass, int, String, javax.swing.text.AttributeSet)}
   */
  @Test
  public void testInsertEmptyString() throws Exception {
    m_filter.insertString(m_bypass, 0, null, null);
    verify(m_bypass).insertString(0, "", null);
  }

  /**
   * {@link BasicDocumentFilter#insertString(FilterBypass, int, String, javax.swing.text.AttributeSet)}
   */
  @Test
  public void testInsertOKString() throws Exception {
    m_filter.insertString(m_bypass, 0, "test", null);
    verify(m_bypass).insertString(0, "test", null);
  }

  /**
   * {@link BasicDocumentFilter#testInsertStringTooLong(FilterBypass, int, String, javax.swing.text.AttributeSet)}
   */
  @Test
  public void testInsertStringTooLong() throws Exception {
    m_filter.insertString(m_bypass, 0, "longText", null);
    verify(m_bypass).insertString(0, "long", null);
  }

  /**
   * {@link BasicDocumentFilter#replace(FilterBypass, int, int, String, javax.swing.text.AttributeSet)}
   */
  @Test
  public void testReplaceStringTooLong() throws Exception {
    m_filter.replace(m_bypass, 0, 0, "longText", null);
    verify(m_bypass).replace(0, 0, "long", null);
  }

}
