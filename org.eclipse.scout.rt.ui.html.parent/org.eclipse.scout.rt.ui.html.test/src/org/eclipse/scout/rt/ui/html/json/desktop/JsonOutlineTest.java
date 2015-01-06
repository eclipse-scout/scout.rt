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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NodePageWithForm;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonOutlineTest {
  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Tests whether the adapters for the detail forms get created
   */
  @Test
  public void testChildAdaptersCreated() throws ProcessingException {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    NodePageWithForm nodePage = new NodePageWithForm();

    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(nodePage);
    pages.add(tablePage);
    IOutline outline = new Outline(pages);

    // Activate nodes (forms get created lazily on activation)
    outline.selectNode(nodePage);
    outline.selectNode(tablePage);

    IPage rowPage = (IPage) tablePage.getTreeNodeFor(tablePage.getTable().getRow(0));
    rowPage = (IPage) tablePage.resolveVirtualChildNode((rowPage));
    outline.selectNode(rowPage);

    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);

    Assert.assertNotNull(jsonOutline.getAdapter(nodePage.getDetailForm()));
    Assert.assertNotNull(jsonOutline.getAdapter(rowPage.getDetailForm()));
  }

  /**
   * Node.detailTable must not be sent if node.tableVisible is set to false to reduce response size.
   */
  @Test
  public void testTableNotSentIfInvisible() throws ProcessingException, JSONException {
    NodePageWithForm nodePage = new NodePageWithForm();
    nodePage.setTableVisible(false);

    List<IPage<?>> pages = new ArrayList<IPage<?>>();
    pages.add(nodePage);
    IOutline outline = new Outline(pages);
    JsonOutline<IOutline> jsonOutline = m_jsonSession.newJsonAdapter(outline, null, null);

    JSONObject jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));

    nodePage.setTableVisible(true);
    jsonNode = jsonOutline.toJson().getJSONArray("nodes").getJSONObject(0);
    Assert.assertNotNull(jsonNode.opt(IOutline.PROP_DETAIL_TABLE));
  }

  @Test
  public void testDispose() {
    ITree tree = new OutlineWithOneNode();
    JsonTree<ITree> object = m_jsonSession.newJsonAdapter(tree, null, null);
    WeakReference<JsonTree> ref = new WeakReference<JsonTree>(object);

    object.dispose();
    m_jsonSession.flush();
    m_jsonSession = null;
    object = null;
    JsonTestUtility.assertGC(ref);
  }

}
