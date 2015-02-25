/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

/**
 * Tests for {@link AbstractHtmlField}
 *
 * @since 4.1.0
 */
public class AbstractHtmlFieldTest {
  @Test
  public void testExecHyperlinkAction() throws MalformedURLException, ProcessingException {
    P_HtmlField htmlField = new P_HtmlField();
    URL google = new URL("http://www.google.de");
    htmlField.doHyperlinkAction(google);
    assertTrue(htmlField.m_execHyperlinkActionCalled);
    assertEquals(google, htmlField.m_url);
    assertEquals(google.getPath(), htmlField.m_path);
    assertFalse(htmlField.m_local);

    htmlField.reset();
    URL localUrl = new URL("http://local/item");
    htmlField.doHyperlinkAction(localUrl);
    assertTrue(htmlField.m_execHyperlinkActionCalled);
    assertEquals(localUrl, htmlField.m_url);
    assertEquals(localUrl.getPath(), htmlField.m_path);
    assertTrue(htmlField.m_local);

    htmlField.reset();
    URL nullUrl = null;
    htmlField.doHyperlinkAction(nullUrl);
    assertFalse(htmlField.m_execHyperlinkActionCalled);
    assertNull(htmlField.m_url);
    assertNull(htmlField.m_path);
    assertFalse(htmlField.m_local);
  }

  private class P_HtmlField extends AbstractHtmlField {

    public boolean m_execHyperlinkActionCalled;
    public URL m_url;
    public String m_path;
    public boolean m_local;

    @Override
    protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
      super.execHyperlinkAction(url, path, local);
      m_execHyperlinkActionCalled = true;
      m_url = url;
      m_path = path;
      m_local = local;
    }

    public void reset() {
      m_execHyperlinkActionCalled = false;
      m_url = null;
      m_path = null;
      m_local = false;
    }
  }
}
