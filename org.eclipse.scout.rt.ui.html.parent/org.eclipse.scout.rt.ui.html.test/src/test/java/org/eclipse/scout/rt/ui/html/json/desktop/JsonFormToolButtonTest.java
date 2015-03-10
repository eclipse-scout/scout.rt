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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.FormToolButton;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormToolButtonTest {
  private IJsonSession m_jsonSession;

  @Before
  public void before() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Form disposal is controlled by the model and must not be triggered by the parent
   */
  @Test
  public void testPreventFormDisposal() throws ProcessingException {
    FormToolButton button = new FormToolButton();
    FormWithOneField form = new FormWithOneField();
    form.start();
    button.setForm(form);

    JsonFormToolButton<IFormToolButton<IForm>> jsonFormToolButton = m_jsonSession.createJsonAdapter(button, null);

    assertNotNull(jsonFormToolButton.getAdapter(form));
    jsonFormToolButton.dispose();
    m_jsonSession.flush();

    // Form has not been closed yet -> must still be registered
    assertNotNull(jsonFormToolButton.getAdapter(form));

    form.doClose();
    m_jsonSession.flush();
    assertNull(jsonFormToolButton.getAdapter(form));
  }

}
