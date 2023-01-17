/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBoxUIFacade;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class JsonTabBoxTest {
  private UiSessionMock m_uiSession;
  private JsonTabBox<ITabBox> m_tabBox;
  private ITabBox m_tabBoxModel;
  private ITabBoxUIFacade m_uiFacade;
  private IGroupBox m_groupBox1;
  private IGroupBox m_groupBox2;

  @Before
  public void setUp() {
    UiSessionMock uiSession = new UiSessionMock();
    m_tabBoxModel = Mockito.mock(ITabBox.class);
    m_groupBox1 = Mockito.mock(IGroupBox.class);
    Mockito.when(m_groupBox1.getContextMenu()).thenReturn(Mockito.mock(IFormFieldContextMenu.class));
    Mockito.when(m_groupBox1.isVisibleGranted()).thenReturn(true);
    m_groupBox2 = Mockito.mock(IGroupBox.class);
    Mockito.when(m_groupBox2.getContextMenu()).thenReturn(Mockito.mock(IFormFieldContextMenu.class));
    Mockito.when(m_groupBox2.isVisibleGranted()).thenReturn(true);
    m_uiFacade = Mockito.mock(ITabBoxUIFacade.class);
    Mockito.when(m_tabBoxModel.getGroupBoxes()).thenReturn(Arrays.asList(m_groupBox1, m_groupBox2));
    Mockito.when(m_tabBoxModel.getUIFacade()).thenReturn(m_uiFacade);
    m_tabBox = new JsonTabBox<>(m_tabBoxModel, uiSession, "123", null);
    m_tabBox.attachAdapters(m_tabBoxModel.getGroupBoxes());
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testHandleUiTabSelected() {
    // find adapter IDs of group-boxes / tab-items
    List<IJsonAdapter<?>> tabItemAdapters = new ArrayList<>(m_tabBox.getAdapters(m_tabBoxModel.getGroupBoxes()));
    String tabItemAdapterId = tabItemAdapters.get(0).getId(); // 1st tab
    handleUiTabSelected(tabItemAdapterId);
    Mockito.verify(m_uiFacade).setSelectedTabFromUI(m_groupBox1);
    tabItemAdapterId = tabItemAdapters.get(1).getId(); // 2nd tab
    handleUiTabSelected(tabItemAdapterId);
    Mockito.verify(m_uiFacade).setSelectedTabFromUI(m_groupBox2);
  }

  private void handleUiTabSelected(String selectedTabId) {
    JSONObject data = new JSONObject();
    data.put("selectedTab", selectedTabId);
    m_tabBox.handleUiPropertyChange("selectedTab", new JsonEvent("?", "?", data).toJson());
  }

  /**
   * Tests whether non displayable groups are sent.
   * <p>
   * This reduces response size and also leverages security because the fields are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableGroups() {
    ITabBox tabBox = new TabBoxWithNonDisplayableGroup();
    tabBox.init();

    JsonTabBox<ITabBox> jsonTabBox = UiSessionTestUtility.newJsonAdapter(m_uiSession, tabBox);
    JsonGroupBox<IGroupBox> jsonDisplayableGroup = m_uiSession.getJsonAdapter(tabBox.getFieldByClass(TabBoxWithNonDisplayableGroup.DisplayableGroup.class), jsonTabBox);
    JsonGroupBox<IGroupBox> jsonNonDisplayableGroup = m_uiSession.getJsonAdapter(tabBox.getFieldByClass(TabBoxWithNonDisplayableGroup.NonDisplayableGroup.class), jsonTabBox);

    // Adapter for NonDisplayableField must not exist
    assertNull(jsonNonDisplayableGroup);

    // Json response must not contain NonDisplayableField
    JSONObject json = jsonTabBox.toJson();
    JSONArray jsonGroupBoxes = json.getJSONArray("tabItems");
    assertEquals(1, jsonGroupBoxes.length());
    assertEquals(jsonDisplayableGroup.getId(), jsonGroupBoxes.get(0));
  }

  @ClassId("0aa70c81-ec8e-453f-9a6d-0726b81931fb")
  private class TabBoxWithNonDisplayableGroup extends AbstractTabBox {

    @Order(10)
    @ClassId("8cc5d9ad-7833-4b4b-8867-1bd5ffaddd51")
    public class DisplayableGroup extends AbstractGroupBox {

    }

    @Order(20)
    @ClassId("ce251da5-80d9-4651-9264-79e210070387")
    public class NonDisplayableGroup extends AbstractGroupBox {

      @Override
      protected void execInitField() {
        setVisibleGranted(false);
      }
    }
  }
}
