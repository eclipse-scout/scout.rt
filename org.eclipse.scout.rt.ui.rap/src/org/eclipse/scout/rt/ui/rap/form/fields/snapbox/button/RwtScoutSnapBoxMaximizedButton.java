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
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.core.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.ext.SnapButtonMaximized;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutSnapBoxMaximizedButton extends RwtScoutFieldComposite<IButton> implements IRwtScoutSnapBoxMaximizedButton {

  private OptimisticLock m_selectionLock;

  public RwtScoutSnapBoxMaximizedButton() {
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    SnapButtonMaximized button = getUiEnvironment().getFormToolkit().createSnapButtonMaximized(parent);
    setUiField(button);
    // listeners
    button.addSelectionListener(new P_SelectionListener());
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setIconIdFromScout(getScoutObject().getIconId());
    setSelectionFromScout(getScoutObject().isSelected());
  }

  @Override
  public SnapButtonMaximized getUiField() {
    return (SnapButtonMaximized) super.getUiField();
  }

  protected void setSelectionFromScout(boolean booleanValue) {
    getUiField().setSelected(booleanValue);
  }

  private void setIconIdFromScout(String iconId) {
    getUiField().setImage(getUiEnvironment().getIcon(iconId));
  }

  @Override
  protected void setLabelFromScout(String s) {
    if (s == null) {
      s = "";
    }
    getUiField().setText(s);
  }

  @Override
  protected void setFontFromScout(FontSpec scoutFont) {
    getUiField().setFont(getUiEnvironment().getFont(scoutFont, getUiField().getFont()));
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    getUiField().setEnabled(b);
  }

  @Override
  protected void setVisibleFromScout(boolean b) {
    if (getUiField().getVisible() != b) {
      getUiField().setVisible(b);
      RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
    }
  }

  @Override
  protected void setTooltipTextFromScout(String s) {
    getUiField().setToolTipText(s);
  }

  protected void handleSelectionFromUi() {
    final boolean selection = !getUiField().isSelected();
    Runnable job = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setSelectedFromUI(selection);
      }
    };
    getUiEnvironment().invokeScoutLater(job, 0);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IButton.PROP_SELECTED.equals(name)) {
      setSelectionFromScout(((Boolean) newValue).booleanValue());
    }
    else if (IButton.PROP_ICON_ID.equals(name)) {
      setIconIdFromScout((String) newValue);
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  private class P_SelectionListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSelectionFromUi();
    }
  }

}
