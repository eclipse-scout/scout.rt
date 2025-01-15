/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonFormMenu;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormMenu;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormMenuTest {
  private IUiSession m_uiSession;

  @Before
  public void before() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Form disposal is controlled by the model and must not be triggered by the parent
   */
  @Test
  public void testPreventFormDisposal() {
    FormMenu button = new FormMenu();
    FormWithOneField form = new FormWithOneField();
    form.start();
    button.setForm(form);
    button.setSelected(true);

    JsonFormMenu<IFormMenu<IForm>> jsonFormMenu = m_uiSession.createJsonAdapter(button, null);

    assertNotNull(jsonFormMenu.getAdapter(form));
    jsonFormMenu.dispose();

    // Form has not been closed yet -> must still be registered
    assertNotNull(jsonFormMenu.getAdapter(form));

    form.doClose();
    assertNull(jsonFormMenu.getAdapter(form));
  }
}
