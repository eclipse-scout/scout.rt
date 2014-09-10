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
package org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link RwtScoutFormButtonBar}
 *
 * @since 5.0-M2
 */
public class RwtScoutFormButtonBarTest {

  @Test
  public void testTitlesInButtonBar() throws ProcessingException {
    final String title = "FormTitle";
    final String title2 = "FormTitle2";
    RwtScoutFormButtonBar bar = new P_RwtScoutFormButtonBar();
    IForm form = mock(IForm.class);
    when(form.getTitle()).thenReturn(title);
    bar.addFormButton(form);
    assertEquals("1 formButton should be installed", 1, bar.getFormButtonBarCount());
    assertTrue(bar.getFormButtons().containsKey(form));
    assertEquals("Title for the Button in the ButtonBar should be " + title, title, bar.getFormButtons().get(form).getScoutObject().getText());

    IForm form2 = mock(IForm.class);
    when(form2.getTitle()).thenReturn(title2);
    bar.addFormButton(form2);
    assertEquals("2 formButtons should be installed", 2, bar.getFormButtonBarCount());
    assertTrue(bar.getFormButtons().containsKey(form2));
    assertEquals("Title for the Button in the ButtonBar should be " + title2, title2, bar.getFormButtons().get(form2).getScoutObject().getText());
  }

  private class P_RwtScoutFormButtonBar extends RwtScoutFormButtonBar {

    @Override
    public IRwtEnvironment getUiEnvironment() {
      IRwtEnvironment env = mock(IRwtEnvironment.class);
      ScoutFormToolkit toolkit = mock(ScoutFormToolkit.class);
      when(toolkit.createButton(any(Composite.class), anyString(), anyInt())).thenAnswer(new Answer<Button>() {

        @Override
        public Button answer(InvocationOnMock invocation) throws Throwable {
          Button btn = mock(Button.class);
          Composite comp = mock(Composite.class);
          when(comp.getChildren()).thenReturn(new Control[0]);
          when(btn.getParent()).thenReturn(comp);
          return btn;
        }
      });

      when(env.getFormToolkit()).thenReturn(toolkit);
      return env;
    }
  }
}
