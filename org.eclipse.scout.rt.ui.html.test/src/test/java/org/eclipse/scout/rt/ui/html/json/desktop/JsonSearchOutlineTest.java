/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractSearchOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchState;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonSearchOutlineTest.MyPageWithTable.Table;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonSearchOutlineTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @After
  public void tearDown() {
    UiSessionTestUtility.getJsonAdapterRegistry(m_uiSession).disposeAdapters();
  }

  private JsonSearchOutline<ISearchOutline> createJsonSearchOutline() {
    var outline = new AbstractSearchOutline() {
    };
    return UiSessionTestUtility.newJsonAdapter(m_uiSession, outline);
  }

  @Test
  public void testSearchStateAdapters() {
    var jsonSearchOutline = createJsonSearchOutline();
    var outline = jsonSearchOutline.getModel();

    var page0 = new MyPageWithTable();
    var page1 = new MyPageWithTable();

    outline.addChildNodes(outline.getRootPage(), List.of(page0, page1));

    // There are two json adapters created, one for each search state
    IJsonAdapter<ISearchState> jsonSearchState0 = m_uiSession.getJsonAdapter(page0.getSearchState(), jsonSearchOutline);
    assertNotNull(jsonSearchState0);
    IJsonAdapter<ISearchState> jsonSearchState1 = m_uiSession.getJsonAdapter(page1.getSearchState(), jsonSearchOutline);
    assertNotNull(jsonSearchState1);

    // Adapters are no longer needed when child page is removed
    outline.removeChildNode(outline.getRootPage(), page1);

    assertFalse(jsonSearchState0.isDisposed());
    assertTrue(jsonSearchState1.isDisposed());
    assertNull(m_uiSession.getJsonAdapter(page1.getSearchState(), jsonSearchOutline));

    outline.removeAllChildNodes(outline.getRootPage());

    assertTrue(jsonSearchState0.isDisposed());
    assertNull(m_uiSession.getJsonAdapter(page0.getSearchState(), jsonSearchOutline));
  }

  public static class MyPageWithTable extends AbstractPageWithTable<Table> implements ISearchPage<Table> {

    private final LazyValue<ISearchState> m_searchState = new LazyValue<>(this::createSearchState);

    @Override
    public ISearchState getSearchState() {
      return m_searchState.get();
    }

    public class Table extends AbstractTable {
    }
  }
}
