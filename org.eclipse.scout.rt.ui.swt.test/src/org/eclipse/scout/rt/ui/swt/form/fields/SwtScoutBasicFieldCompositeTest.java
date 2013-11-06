/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.graphics.Point;
import org.junit.Test;

/**
 * JUnit test for {@link SwtScoutBasicFieldComposite}.
 */
public class SwtScoutBasicFieldCompositeTest {

  /**
   * Test method for {@link SwtScoutBasicFieldComposite#setDisplayTextFromScout(java.lang.String)}.
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
    P_SwtScoutBasicFieldComposite field = new P_SwtScoutBasicFieldComposite();

    field.setSelection(oldStartIndex, oldEndIndex);
    field.setText(oldText);
    field.setCaretOffset(oldStartIndex);
    field.setStartIndex(oldStartIndex);
    field.setEndIndex(oldEndIndex);

    field.setDisplayTextFromScout(newText);

    assertEquals("Text", newText, field.getText());
    assertEquals("StartIndex", newStartIndex, field.getStartIndex());
    assertEquals("EndIndex", newEndIndex, field.getEndIndex());
  }

  private static class P_SwtScoutBasicFieldComposite extends SwtScoutBasicFieldComposite<IBasicField<?>> {

    private String m_text;
    private int m_startIndex;
    private int m_endIndex;
    private int m_caretOffset;

    @Override
    protected String getText() {
      return m_text;
    }

    @Override
    protected void setText(String text) {
      m_text = text;
    }

    @Override
    protected Point getSelection() {
      return new Point(m_startIndex, m_endIndex);
    }

    @Override
    protected void setSelection(int startIndex, int endIndex) {
      m_startIndex = startIndex;
      m_endIndex = endIndex;
    }

    public int getStartIndex() {
      return m_startIndex;
    }

    public void setStartIndex(int startIndex) {
      m_startIndex = startIndex;
    }

    public int getEndIndex() {
      return m_endIndex;
    }

    public void setEndIndex(int endIndex) {
      m_endIndex = endIndex;
    }

    @Override
    protected int getCaretOffset() {
      return m_caretOffset;
    }

    @Override
    protected void setCaretOffset(int caretPosition) {
      m_caretOffset = caretPosition;
    }

    @Override
    protected TextFieldEditableSupport createEditableSupport() {
      return null;
    }

  }

}
