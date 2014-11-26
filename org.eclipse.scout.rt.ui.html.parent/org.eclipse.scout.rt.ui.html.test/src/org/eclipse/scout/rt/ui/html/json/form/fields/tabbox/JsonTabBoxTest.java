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

import java.util.Arrays;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBoxUIFacade;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonTabBoxTest {

  JsonTabBox<ITabBox> tabBox;
  ITabBox tabBoxModel;
  ITabBoxUIFacade m_uiFacade;
  IGroupBox m_groupBox1;
  IGroupBox m_groupBox2;

  @Before
  public void setUp() {
    JsonSessionMock jsonSession = new JsonSessionMock();
    tabBoxModel = Mockito.mock(ITabBox.class);
    m_groupBox1 = Mockito.mock(IGroupBox.class);
    m_groupBox2 = Mockito.mock(IGroupBox.class);
    m_uiFacade = Mockito.mock(ITabBoxUIFacade.class);
    Mockito.when(tabBoxModel.getGroupBoxes()).thenReturn(Arrays.asList(m_groupBox1, m_groupBox2));
    Mockito.when(tabBoxModel.getUIFacade()).thenReturn(m_uiFacade);
    tabBox = new JsonTabBox<ITabBox>(tabBoxModel, jsonSession, "123");
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
    tabBox.handleUiTabSelected(new JsonEvent("?", "?", data));
  }

}
