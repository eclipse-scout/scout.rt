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
package org.eclipse.scout.rt.ui.swt.form.fields.groupbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.form.fields.groupbox.layout.ButtonBarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtScoutGroupBoxButtonbar implements ISwtScoutGroupBoxButtonbar {
  private Composite m_swtContainer;
  private Composite m_leftPart;
  private Composite m_rightPart;
  private ISwtEnvironment m_environment;
  private IGroupBox m_scoutGroupBox;
  private PropertyChangeListener m_scoutButtonVisibleListener;

  public SwtScoutGroupBoxButtonbar() {
  }

  public void createField(Composite parent, IGroupBox scoutGroupBox, ISwtEnvironment environment) {
    m_scoutGroupBox = scoutGroupBox;
    m_environment = environment;
    m_swtContainer = getEnvironment().getFormToolkit().createComposite(parent);
    m_leftPart = getEnvironment().getFormToolkit().createComposite(m_swtContainer);
    m_rightPart = getEnvironment().getFormToolkit().createComposite(m_swtContainer);

    // layout
    GridLayout buttonBarLayout = new GridLayout(3, false);
    buttonBarLayout.horizontalSpacing = 0;
    buttonBarLayout.verticalSpacing = 0;
    buttonBarLayout.marginTop = 6;
    buttonBarLayout.marginHeight = 0;
    buttonBarLayout.marginWidth = 0;
    m_swtContainer.setLayout(buttonBarLayout);

    m_leftPart.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
    m_rightPart.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));

    m_leftPart.setLayout(new ButtonBarLayout(SWT.LEFT));
    m_rightPart.setLayout(new ButtonBarLayout(SWT.RIGHT));

    // init buttons
    for (IFormField f : getScoutGroupBox().getFields()) {
      if (f instanceof IButton) {
        IButton b = (IButton) f;
        if (b.isProcessButton()) {
          // alignment
          if (b.getGridData().horizontalAlignment <= 0) {
            getEnvironment().createFormField(m_leftPart, b);
          }
          else {
            getEnvironment().createFormField(m_rightPart, b);
          }
        }
      }
    }
  }

  protected void attachScout() {
    detachScout();
    //
    m_scoutButtonVisibleListener = new P_ScoutButtonVisiblePropertyListener();
    for (IFormField f : getScoutGroupBox().getFields()) {
      if (f instanceof IButton) {
        IButton b = (IButton) f;
        if (b.isProcessButton()) {
          b.addPropertyChangeListener(IButton.PROP_VISIBLE, m_scoutButtonVisibleListener);
        }
      }
    }
  }

  protected void detachScout() {
    if (m_scoutButtonVisibleListener != null) {
      for (IFormField f : getScoutGroupBox().getFields()) {
        if (f instanceof IButton) {
          IButton b = (IButton) f;
          if (b.isProcessButton()) {
            b.removePropertyChangeListener(IButton.PROP_VISIBLE, m_scoutButtonVisibleListener);
          }
        }
      }
      m_scoutButtonVisibleListener = null;
    }
  }

  public void updateButtonbarVisibility() {
    boolean excludeLeft = true;
    boolean excludeRight = true;
    for (Control c : m_leftPart.getChildren()) {
      if (c.getVisible()) {
        excludeLeft = false;
        break;
      }
    }
    ((GridData) m_leftPart.getLayoutData()).exclude = excludeLeft;
    for (Control c : m_rightPart.getChildren()) {
      if (c.getVisible()) {
        excludeRight = false;
        break;
      }
    }
    ((GridData) m_rightPart.getLayoutData()).exclude = excludeRight;
    if (!excludeLeft || !excludeRight) {
      m_swtContainer.layout(true, true);
    }
    if (!excludeLeft) {
      m_leftPart.layout(true, true);
      m_leftPart.pack();
    }
    if (!excludeRight) {
      m_leftPart.layout(true, true);
      m_rightPart.pack();
    }
    ((GridData) getSwtContainer().getLayoutData()).exclude = excludeLeft && excludeRight;
  }

  public IGroupBox getScoutGroupBox() {
    return m_scoutGroupBox;
  }

  public Composite getSwtContainer() {
    return m_swtContainer;
  }

  public Composite getLeftPart() {
    return m_leftPart;
  }

  public Composite getRightPart() {
    return m_rightPart;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  private class P_ScoutButtonVisiblePropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          if (getSwtContainer() != null && !getSwtContainer().isDisposed()) {
            updateButtonbarVisibility();
          }
        }
      };
      getEnvironment().invokeSwtLater(job);
    }
  } // end class P_ButtonVisiblePropertyListener
}
