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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 5.2
 */
public class PageWithTableGroupingTestHelper {

  public static void assertGroupingState(ITable table, List<IColumn> expectedGroupingColumns, List<IColumn> expectedSortColumns) {

    //the expected grouping columns (and only those) are actually grouped
    assertTrue("grouped columns match", CollectionUtility.equalsCollection(table.getColumnSet().getGroupedColumns(), expectedGroupingColumns));

    //the expected grouping columns are the first sort columns, only sort columns follow.
    expectedGroupingColumns.addAll(expectedSortColumns);

    assertTrue("sorted columns match", CollectionUtility.equalsCollection(table.getColumnSet().getSortColumns(), expectedGroupingColumns));

    //sanity check: grouped implies sorted
    //sanity check: grouped implies visible
    for (IColumn<?> c : table.getColumns()) {
      if (c.isGroupingActive()) {
        assertTrue(c.isSortActive());
        assertTrue(c.isVisible());
      }
    }
  }

  public static ITable setupDesktop(IOutline outline) {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(outline));
    desktop.activateOutline(outline);
    desktop.activateFirstPage();
    if (desktop.getOutline().getActivePage() instanceof IPageWithTable) {
      return ((IPageWithTable) (desktop.getOutline().getActivePage())).getTable();
    }
    return null;
  }

}
