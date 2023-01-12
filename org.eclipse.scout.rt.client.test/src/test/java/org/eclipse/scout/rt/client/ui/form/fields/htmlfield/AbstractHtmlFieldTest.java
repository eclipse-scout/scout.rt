/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import static org.junit.Assert.*;

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
  public void testExecHyperlinkAction() {
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
