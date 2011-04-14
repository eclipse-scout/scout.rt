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
package org.eclipse.scout.rt.ui.swt.internal;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TextFieldEditableSupport {

  private StyledText m_styledText;
  private Text m_text;
  private Control[] m_tabListBackup;
  private Listener m_readOnlyListener = new P_ReadOnlyListener();
  private final Object m_lock = new Object();

  public TextFieldEditableSupport(StyledText styledText) {
    m_styledText = styledText;
  }

  public TextFieldEditableSupport(Text text) {
    m_text = text;
  }

  public Control getTextField() {
    if (m_styledText != null) {
      return m_styledText;
    }
    else if (m_text != null) {
      return m_text;
    }
    throw new IllegalStateException("StyledText and Text is null");
  }

  public void setEditable(boolean editable) {
    synchronized (m_lock) {
      if (editable) {
        getTextField().removeListener(SWT.FocusIn, m_readOnlyListener);
        getTextField().removeListener(SWT.FocusOut, m_readOnlyListener);
      }
      else {
        getTextField().addListener(SWT.FocusIn, m_readOnlyListener);
        getTextField().addListener(SWT.FocusOut, m_readOnlyListener);
      }
      setEditableInternal(editable);
      setFieldInTabList(editable);
    }
  }

  private void setEditableInternal(boolean editable) {
    if (m_styledText != null) {
      m_styledText.setEditable(editable);
    }
    else if (m_text != null) {
      m_text.setEditable(editable);
    }
  }

  private void setFieldInTabList(boolean inTablist) {
    Control textField = getTextField();
    Composite parent = textField.getParent();
    if (inTablist) {
      parent.setTabList(m_tabListBackup);
    }
    else {
      m_tabListBackup = parent.getTabList();
      ArrayList<Control> tabList = new ArrayList<Control>(Arrays.asList(m_tabListBackup));
      if (tabList.remove(textField)) {
        parent.setTabList(tabList.toArray(new Control[tabList.size()]));
      }
    }
  }

  private class P_ReadOnlyListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      synchronized (m_lock) {

        switch (event.type) {

          case SWT.FocusIn:
            setFieldInTabList(true);
            break;
          case SWT.FocusOut:
            setFieldInTabList(false);
            break;
        }
      }
    }
  } // end class P_ReadOnlyListener

}
