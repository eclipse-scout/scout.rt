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
package org.eclipse.scout.rt.spec.client.gen.extract;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;
import org.junit.Test;

/**
 * Test form {@link SearchFormExtractor}
 */
public class SearchFormExtractorTest {

  @Test
  public void testGetText() {
    SearchFormExtractor ex = new SearchFormExtractor();
    @SuppressWarnings("unchecked")
    AbstractPageWithTable<ITable> page = mock(AbstractPageWithTable.class);

    when(page.getSearchFormInternal()).thenReturn(null);
    assertNull(ex.getText(page));

    AbstractSearchForm searchForm = mock(AbstractSearchForm.class);
    when(page.getSearchFormInternal()).thenReturn(searchForm);
    String extractedText = ex.getText(page);
    assertTrue(extractedText.startsWith("[["));
    assertTrue(extractedText.endsWith("]]"));
    assertTrue(extractedText.contains("AbstractSearchForm"));
  }

}
