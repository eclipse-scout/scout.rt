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
package org.eclipse.scout.rt.ui.swt.form.fields.snapbox.button;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutMinimizedButton extends SwtScoutComposite<IButton> {

  private OptimisticLock m_selectionLock;

  public SwtScoutMinimizedButton() {
    super();
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Button button = getEnvironment().getFormToolkit().createButton(parent, "", SWT.TOGGLE);
    setSwtField(button);
    // listeners
    button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSwtSelection(getSwtField().getSelection());
      }
    });
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setIconIdFromScout(getScoutObject().getIconId());
    setSelectionFormScout(getScoutObject().isSelected());
    getSwtField().setToolTipText(getScoutObject().getLabel());
  }

  @Override
  public Button getSwtField() {
    return (Button) super.getSwtField();
  }

  protected void setIconIdFromScout(String iconId) {
    getSwtField().setImage(getEnvironment().getIcon(iconId));
  }

  private void handleSwtSelection(final boolean selected) {
    try {
      if (m_selectionLock.acquire()) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().setSelectedFromUI(selected);
          }
        };
        getEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  protected void setSelectionFormScout(boolean selection) {
    try {
      if (m_selectionLock.acquire()) {
        getSwtField().setSelection(selection);
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IButton.PROP_SELECTED.equals(name)) {
      setSelectionFormScout(((Boolean) newValue).booleanValue());
    }
    else if (IButton.PROP_ICON_ID.equals(name)) {
      setIconIdFromScout((String) newValue);
    }
    else {
      super.handleScoutPropertyChange(name, newValue);
    }

  }

}
