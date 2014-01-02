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
package org.eclipse.scout.rt.spec.client.gen.filter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.spec.client.gen.filter.column.DisplayableColumnFilter;
import org.junit.Test;

/**
 * Test for {@link DisplayableColumnFilter}
 */
public class DisplayableColumnFilterTest {

  /**
   * Tests, if a {@link IColumn} which is displayble is accepted by the {@link DisplayableColumnFilter}.
   */
  @Test
  public void testDisplayableColumnAccepted() {
    AbstractColumn mockColumn = mock(AbstractColumn.class);
    when(mockColumn.isDisplayable()).thenReturn(true);

    DisplayableColumnFilter filter = new DisplayableColumnFilter();
    boolean actualAccepted = filter.accept(mockColumn);
    assertTrue(actualAccepted);
  }

}
