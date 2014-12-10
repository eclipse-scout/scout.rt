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
package org.eclipse.scout.rt.ui.html.json.form;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonFormTest {
  private IJsonSession m_jsonSession;

  @Before
  public void before() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Form disposal is controlled by the model and must not be triggered by the parent
   */
  @Test
  public void testFormDisposalOnClose() throws ProcessingException {
    FormWithOneField form = new FormWithOneField();
    createJsonForm(form, m_jsonSession);

    form.start();
    assertNotNull(m_jsonSession.getJsonAdapter(form, m_jsonSession.getRootJsonAdapter()));

    form.doClose();
    m_jsonSession.flush();
    assertNull(m_jsonSession.getJsonAdapter(form, m_jsonSession.getRootJsonAdapter()));
  }

  public static JsonForm<IForm> createJsonForm(IForm model, IJsonSession jsonSession) {
    JsonForm<IForm> jsonAdapter = new JsonForm<IForm>(model, jsonSession, jsonSession.createUniqueIdFor(null), jsonSession.getRootJsonAdapter());
    jsonAdapter.attach();
    return jsonAdapter;
  }

}
