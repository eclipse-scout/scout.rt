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
public class JsonFormTest {
  private JsonSessionMock m_jsonSession;

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
    m_jsonSession.newJsonAdapter(form, m_jsonSession.getRootJsonAdapter(), null);

    form.start();
    assertNotNull(m_jsonSession.getJsonAdapter(form, m_jsonSession.getRootJsonAdapter()));

    form.doClose();
    m_jsonSession.flush();
    assertNull(m_jsonSession.getJsonAdapter(form, m_jsonSession.getRootJsonAdapter()));
  }

}
