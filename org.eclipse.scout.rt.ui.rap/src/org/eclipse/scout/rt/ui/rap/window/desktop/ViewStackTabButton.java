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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ViewStackTabButton</h3> ...
 * 
 * @author aho
 * @since 3.7.0 June 2011
 */
public class ViewStackTabButton extends Composite {
  private static final long serialVersionUID = 1L;

  private static final String VARIANT_ACIVATE_BUTTON = "formTabActivate";
  private static final String VARIANT_ACIVATE_BUTTON_ACTIVE = "formTabActivate-active";
  private static final String VARIANT_FORM_CLOSE_BUTTON = "formTabClose";
  private static final String VARIANT_TAB = "formTab";
  private static final String VARIANT_TAB_ACTIVE = "formTab-active";

  private Button m_activateButton;
  private Button m_closeButton;
  private EventListenerList m_eventListeners;
  private boolean m_closable = false;
  private boolean m_active = false;

  public ViewStackTabButton(Composite parent) {
    super(parent, SWT.NONE);
    m_eventListeners = new EventListenerList();
    createContent(this);
    setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TAB);
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  protected void createContent(Composite parent) {
    m_activateButton = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.PUSH);
    m_activateButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_ACIVATE_BUTTON_ACTIVE);
    m_activateButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        fireViewTabEvent(new ViewTabSelectionEvent(ViewStackTabButton.this, ViewTabSelectionEvent.TYPE_VIEW_TAB_SELECTION));
      }
    });
    m_closeButton = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.PUSH);
    m_closeButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORM_CLOSE_BUTTON);
    m_closeButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        fireViewTabEvent(new ViewTabSelectionEvent(ViewStackTabButton.this, ViewTabSelectionEvent.TYPE_VIEW_TAB_CLOSE_SELECTION));
      }
    });
    GridLayout layout = new GridLayout(2, false);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    parent.setLayout(layout);
    GridData activateButtonData = new GridData();
    activateButtonData.horizontalAlignment = SWT.CENTER;
    activateButtonData.verticalAlignment = SWT.CENTER;
    m_activateButton.setLayoutData(activateButtonData);
    GridData closeButtonData = new GridData();
    closeButtonData.horizontalAlignment = SWT.CENTER;
    closeButtonData.verticalAlignment = SWT.CENTER;
    closeButtonData.heightHint = 12;
    closeButtonData.widthHint = 12;

    m_closeButton.setLayoutData(closeButtonData);
  }

  public void setActive(boolean active) {
    m_active = active;
    updateStylesAndButtons();
  }

  private void updateStylesAndButtons() {
    if (m_active) {
      setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TAB_ACTIVE);
      m_activateButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_ACIVATE_BUTTON_ACTIVE);
    }
    else {
      setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TAB);
      m_activateButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_ACIVATE_BUTTON);
    }
    GridData closeButtonData = (GridData) m_closeButton.getLayoutData();
    if (m_active && m_closable) {
      m_closeButton.setVisible(true);
      closeButtonData.exclude = false;
    }
    else {
      m_closeButton.setVisible(false);
      closeButtonData.exclude = true;
    }

    layout(true);
  }

  public void setLabel(String label) {
    m_activateButton.setText(label);
    getParent().layout();
  }

  public String getLabel() {
    return m_activateButton.getText();
  }

  public void setImage(Image img) {
    m_activateButton.setImage(img);
    getParent().layout();
  }

  public Image getImage() {
    return m_activateButton.getImage();
  }

  public void setShowClose(boolean closable) {
    m_closable = closable;
    updateStylesAndButtons();
  }

  public void addViewTabListener(IViewTabSelectionListener listener) {
    m_eventListeners.add(IViewTabSelectionListener.class, listener);
  }

  public void removeViewTabListener(IViewTabSelectionListener listener) {
    m_eventListeners.remove(IViewTabSelectionListener.class, listener);
  }

  protected void fireViewTabEvent(ViewTabSelectionEvent event) {
    for (IViewTabSelectionListener l : m_eventListeners.getListeners(IViewTabSelectionListener.class)) {
      l.handleEvent(event);
    }
  }
}
