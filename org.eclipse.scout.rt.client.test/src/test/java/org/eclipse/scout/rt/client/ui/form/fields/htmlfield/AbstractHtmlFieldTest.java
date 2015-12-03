/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractHtmlField}
 *
 * @since 4.1.0
 */
@RunWith(PlatformTestRunner.class)
public class AbstractHtmlFieldTest {
  @Test
  public void testExecHyperlinkAction() throws MalformedURLException {
    P_HtmlField htmlField = new P_HtmlField();
    String ref = "data";
    htmlField.getUIFacade().fireAppLinkActionFromUI(ref);
    assertTrue(htmlField.m_execAppLinkActionCalled);
    assertEquals(ref, htmlField.m_ref);
  }

  private class P_HtmlField extends AbstractHtmlField {

    public boolean m_execAppLinkActionCalled;
    public String m_ref;

    @Override
    protected void execAppLinkAction(String ref) {
      super.execAppLinkAction(ref);
      m_execAppLinkActionCalled = true;
      m_ref = ref;
    }
  }
}
