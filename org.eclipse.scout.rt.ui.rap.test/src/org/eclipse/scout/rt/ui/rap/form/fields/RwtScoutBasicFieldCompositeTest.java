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
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * JUnit test for {@link RwtScoutBasicFieldComposite}
 */
public class RwtScoutBasicFieldCompositeTest {

  /**
   * Test method for {@link RwtScoutBasicFieldComposite#setDisplayTextFromScout(java.lang.String)}.
   */
  @Test
  public void testSetDisplayTextFromScout() {
    //validate -> remove digits
    runSetDisplayTextFromScout("lorem2", 6, 6, "lorem", 5, 5);
    runSetDisplayTextFromScout("lo3rem", 3, 3, "lorem", 2, 2);
    runSetDisplayTextFromScout("9lorem", 1, 1, "lorem", 0, 0);

    //validate -> convert to upper-case
    runSetDisplayTextFromScout("LOREm", 5, 5, "LOREM", 5, 5);
    runSetDisplayTextFromScout("LOrEM", 3, 3, "LOREM", 3, 3);
    runSetDisplayTextFromScout("lOREM", 1, 1, "LOREM", 1, 1);

    //validate -> remove consecutive duplicate characters (keep the first one)
    runSetDisplayTextFromScout("loremm", 6, 6, "lorem", 5, 5);
    runSetDisplayTextFromScout("loorem", 3, 3, "lorem", 2, 2);
    runSetDisplayTextFromScout("loorem", 2, 2, "lorem", 1, 1);
    runSetDisplayTextFromScout("llorem", 1, 1, "lorem", 0, 0);
    runSetDisplayTextFromScout("llorem", 2, 2, "lorem", 1, 1);

    //validate -> remove "aaa"
    runSetDisplayTextFromScout("loraaa", 6, 6, "lor", 3, 3);
    runSetDisplayTextFromScout("loraaaem", 6, 6, "lorem", 3, 3);
    //nice to have: runSetDisplayTextFromScout("loraaaem", 4, 4, "lorem", 3, 3);

    //validate -> Replace the digits by the last character before it, repeated as many time as the number built by the replaced digit
    runSetDisplayTextFromScout("lorem5", 6, 6, "loremmmmmm", 10, 10);
    runSetDisplayTextFromScout("lo4rem", 3, 3, "looooorem", 6, 6);
    runSetDisplayTextFromScout("3lorem", 1, 1, "lorem", 0, 0);
  }

  private void runSetDisplayTextFromScout(String oldText, int oldStartIndex, int oldEndIndex, String newText, int newStartIndex, int newEndIndex) {
    P_RwtScoutBasicFieldComposite field = new P_RwtScoutBasicFieldComposite();

    Text mock = Mockito.mock(Text.class);
    Mockito.when(mock.getText()).thenReturn(oldText, newText);
    Mockito.when(mock.getSelection()).thenReturn(new Point(oldStartIndex, oldEndIndex));
    field.setUiFieldMock(mock);

    field.setDisplayTextFromScout(newText);

    Mockito.verify(mock).setText(newText);
    Mockito.verify(mock).setSelection(newStartIndex, newEndIndex);
  }

  private static class P_RwtScoutBasicFieldComposite extends RwtScoutBasicFieldComposite<IBasicField<?>> {

    private Text m_uiFieldMock;

    @Override
    public Text getUiField() {
      return m_uiFieldMock;
    }

    public void setUiFieldMock(Text uiFieldMock) {
      m_uiFieldMock = uiFieldMock;
    }
  }
}
