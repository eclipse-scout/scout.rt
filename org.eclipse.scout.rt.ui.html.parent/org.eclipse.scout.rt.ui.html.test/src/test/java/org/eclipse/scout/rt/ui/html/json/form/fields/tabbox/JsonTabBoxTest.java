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
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBoxUIFacade;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class JsonTabBoxTest {
  private JsonSessionMock m_jsonSession;
  private JsonTabBox<ITabBox> m_tabBox;
  private ITabBox m_tabBoxModel;
  private ITabBoxUIFacade m_uiFacade;
  private IGroupBox m_groupBox1;
  private IGroupBox m_groupBox2;

  @Before
  public void setUp() {
    JsonSessionMock jsonSession = new JsonSessionMock();
    m_tabBoxModel = Mockito.mock(ITabBox.class);
    m_groupBox1 = Mockito.mock(IGroupBox.class);
    m_groupBox2 = Mockito.mock(IGroupBox.class);
    m_uiFacade = Mockito.mock(ITabBoxUIFacade.class);
    Mockito.when(m_tabBoxModel.getGroupBoxes()).thenReturn(Arrays.asList(m_groupBox1, m_groupBox2));
    Mockito.when(m_tabBoxModel.getUIFacade()).thenReturn(m_uiFacade);
    m_tabBox = new JsonTabBox<ITabBox>(m_tabBoxModel, jsonSession, "123", null);
    m_jsonSession = new JsonSessionMock();
  }

  @Test
  public void testHandleUiTabSelected() {
    handleUiTabSelected(0);
    Mockito.verify(m_uiFacade).setSelectedTabFromUI(m_groupBox1);
    handleUiTabSelected(1);
    Mockito.verify(m_uiFacade).setSelectedTabFromUI(m_groupBox2);
  }

  private void handleUiTabSelected(int tabIndex) {
    JSONObject data = new JSONObject();
    JsonObjectUtility.putProperty(data, "tabIndex", tabIndex);
    m_tabBox.handleUiTabSelected(new JsonEvent("?", "?", data));
  }

  /**
   * Tests whether non displayable groups are sent.
   * <p>
   * This limits response size and also leverages security because the fields are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNotDisplayableGroups() throws Exception {
    ITabBox tabBox = new TabBoxWithNotDisplayableGroup();
    JsonTestUtility.initField(tabBox);

    JsonTabBox<ITabBox> jsonTabBox = m_jsonSession.newJsonAdapter(tabBox, null, null);
    JsonGroupBox<IGroupBox> jsonDisplayableGroup = m_jsonSession.getJsonAdapter(tabBox.getFieldByClass(TabBoxWithNotDisplayableGroup.DisplayableGroup.class), jsonTabBox);
    JsonGroupBox<IGroupBox> jsonNotDisplayableGroup = m_jsonSession.getJsonAdapter(tabBox.getFieldByClass(TabBoxWithNotDisplayableGroup.NotDisplayableGroup.class), jsonTabBox);

    // Adapter for NotDisplayableField must not exist
    assertNull(jsonNotDisplayableGroup);

    // Json response must not contain NotDisplayableField
    JSONObject json = jsonTabBox.toJson();
    JSONArray jsonGroupBoxes = json.getJSONArray("tabItems");
    assertEquals(1, jsonGroupBoxes.length());
    assertEquals(jsonDisplayableGroup.getId(), jsonGroupBoxes.get(0));
  }

  private class TabBoxWithNotDisplayableGroup extends AbstractTabBox {

    @Order(10)
    public class DisplayableGroup extends AbstractGroupBox {

    }

    @Order(20)
    public class NotDisplayableGroup extends AbstractGroupBox {

      @Override
      protected void execInitField() throws ProcessingException {
        setVisibleGranted(false);
      }
    }
  }

}
