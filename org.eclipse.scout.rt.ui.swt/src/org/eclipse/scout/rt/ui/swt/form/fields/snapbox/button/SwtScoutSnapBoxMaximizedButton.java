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
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.ext.SnapButtonMaximized;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutSnapBoxMaximizedButton extends SwtScoutFieldComposite<IButton> implements ISwtScoutSnapBoxMaximizedButton {

  private OptimisticLock m_selectionLock;

  public SwtScoutSnapBoxMaximizedButton() {
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    SnapButtonMaximized button = getEnvironment().getFormToolkit().createSnapButtonMaximized(parent);
    setSwtField(button);
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
  public SnapButtonMaximized getSwtField() {
    return (SnapButtonMaximized) super.getSwtField();
  }

  protected void setSelectionFromScout(boolean booleanValue) {
    getSwtField().setSelected(booleanValue);
  }

  private void setIconIdFromScout(String iconId) {
    getSwtField().setImage(getEnvironment().getIcon(iconId));
  }

  @Override
  protected void setLabelFromScout(String s) {
    if (s == null) {
      s = "";
    }
    getSwtField().setText(s);
  }

  @Override
  protected void setFontFromScout(FontSpec scoutFont) {
    getSwtField().setFont(getEnvironment().getFont(scoutFont, getSwtField().getFont()));
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    getSwtField().setEnabled(b);
  }

  @Override
  protected void setVisibleFromScout(boolean b) {
    if (getSwtField().getVisible() != b) {
      getSwtField().setVisible(b);
      SwtLayoutUtility.invalidateLayout(getSwtContainer());
    }
  }

  @Override
  protected void setTooltipTextFromScout(String s) {
    getSwtField().setToolTipText(s);
  }

  protected void handleSelectionFromSwt() {
    final boolean selection = !getSwtField().isSelected();
    Runnable job = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setSelectedFromUI(selection);
      }
    };
    getEnvironment().invokeScoutLater(job, 0);
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
    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSelectionFromSwt();
    }
  }

}
