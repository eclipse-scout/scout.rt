/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.SearchOutlineTest.MyPageWithTable.Table;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractOutline}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SearchOutlineTest {

  @Test
  public void testSearchStatesStatic() {
    var page0 = new MyPageWithTable();
    var page1 = new MyPageWithTable();
    var page2 = new MyPageWithTable();

    var outline = new AbstractSearchOutline() {
      @Override
      protected void execCreateChildPages(List<IPage<?>> pageList) {
        super.execCreateChildPages(pageList);
        pageList.add(page0);
        pageList.add(page1);
        pageList.add(page2);
      }
    };

    assertEquals(
        Map.of(
            page0, page0.getSearchState(),
            page1, page1.getSearchState(),
            page2, page2.getSearchState()
        ),
        outline.getSearchStates()
    );
  }

  @Test
  public void testSearchStatesDynamic() {
    var outline = new AbstractSearchOutline() {
    };

    var page0 = new MyPageWithTable();
    var page1 = new MyPageWithTable();
    var page10 = new MyPageWithTable();
    var page2 = new MyPageWithTable();

    assertEquals(Map.of(), outline.getSearchStates());

    outline.addChildNode(outline.getRootPage(), page0);

    assertEquals(
        Map.of(page0, page0.getSearchState()),
        outline.getSearchStates()
    );

    outline.addChildNodes(outline.getRootPage(), List.of(page1, page2));

    assertEquals(
        Map.of(
            page0, page0.getSearchState(),
            page1, page1.getSearchState(),
            page2, page2.getSearchState()
        ),
        outline.getSearchStates()
    );

    outline.addChildNode(page1, page10);

    assertEquals(
        Map.of(
            page0, page0.getSearchState(),
            page1, page1.getSearchState(),
            page2, page2.getSearchState()
        ),
        outline.getSearchStates()
    );

    outline.removeChildNode(outline.getRootPage(), page0);

    assertEquals(
        Map.of(
            page1, page1.getSearchState(),
            page2, page2.getSearchState()
        ),
        outline.getSearchStates()
    );

    outline.removeAllChildNodes(outline.getRootPage());

    assertEquals(Map.of(), outline.getSearchStates());
  }

  public static class MyPageWithTable extends AbstractPageWithTable<Table> implements ISearchPage<Table> {

    private final LazyValue<ISearchState> m_searchState = new LazyValue<>(ISearchState.class);

    @Override
    public ISearchState getSearchState() {
      return m_searchState.get();
    }

    public class Table extends AbstractTable {
    }
  }
}
