/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.snapbox.button;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutMinimizedButton extends RwtScoutComposite<IButton> {

  private OptimisticLock m_selectionLock;

  public RwtScoutMinimizedButton() {
    super();
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Button button = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.TOGGLE);
    setUiField(button);
    // listeners
    button.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleUiSelection(getUiField().getSelection());
      }
    });
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setIconIdFromScout(getScoutObject().getIconId());
    setSelectionFormScout(getScoutObject().isSelected());
    getUiField().setToolTipText(getScoutObject().getLabel());
  }

  @Override
  public Button getUiField() {
    return (Button) super.getUiField();
  }

  protected void setIconIdFromScout(String iconId) {
    getUiField().setImage(getUiEnvironment().getIcon(iconId));
  }

  private void handleUiSelection(final boolean selected) {
    try {
      if (m_selectionLock.acquire()) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().setSelectedFromUI(selected);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
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
        getUiField().setSelection(selection);
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
