/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.htmlfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing: {@link AbstractHtmlField}
 */
public class JsonHtmlFieldTest extends BaseFormFieldTest {

  private AbstractHtmlField m_model = new AbstractHtmlField() {
  };

  private JsonHtmlField m_htmlField = new JsonHtmlField<IHtmlField>(m_model, m_session, m_session.createUniqueId(), null);

  @Before
  public void setUp() {
    m_htmlField.init();
  }

  @Test
  public void testToImageByIconIdReplacement() throws JSONException {
    m_model.setDisplayText(HTML.imgByIconId("test_icon").toEncodedHtml());
    JSONObject json = m_htmlField.toJson();
    // currently no icon provider services are available during html ui test, therefore any icon string should be replaced by null
    assertEquals("<img src=\"null\">", json.get(IHtmlField.PROP_DISPLAY_TEXT));
  }

  @Test
  public void testToImageByBinaryResourceReplacement() throws JSONException {
    m_model.setDisplayText(HTML.imgByBinaryResource("test_resource").toEncodedHtml());
    JSONObject json = m_htmlField.toJson();
    assertEquals("<img src=\"dynamic/" + m_session.getUiSessionId() + "/" + m_htmlField.getId() + "/test_resource\">", json.get(IHtmlField.PROP_DISPLAY_TEXT));
  }

}
