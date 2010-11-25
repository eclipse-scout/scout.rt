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
package org.eclipse.scout.rt.ui.swt.form.fields.splitbox;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.AbstractShellPackHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>SwtScoutSplitBox</h3> ...
 * 
 * @since 1.0.9 16.07.2008
 */
public class SwtScoutSplitBox extends SwtScoutFieldComposite<ISplitBox> implements ISwtScoutSplitBox {

  private OptimisticLock lockSplitter;

  public SwtScoutSplitBox() {
    lockSplitter = new OptimisticLock();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    SashForm container = getEnvironment().getFormToolkit().createSashForm(parent, getScoutObject().isSplitHorizontal() ? SWT.HORIZONTAL : SWT.VERTICAL);
    container.setData(PROP_SHELL_PACK_HANDLER, new AbstractShellPackHandler(container.getDisplay()) {
      @Override
      protected void execSizeCheck() {
        if (!getSwtContainer().isDisposed()) {
          getSwtContainer().layout(true, true);
          setLayoutDirty();
        }
      }
    });

    container.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
    for (IFormField scoutField : getScoutObject().getFields()) {
      getEnvironment().createFormField(container, scoutField);
    }
    setSwtContainer(container);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // split position
    int[] a = null;
    String propName = getScoutObject().getCacheSplitterPositionPropertyName();
    if (!StringUtility.isNullOrEmpty(propName)) {
      if (getScoutObject().isCacheSplitterPosition()) {
        try {
          a = ClientUIPreferences.getInstance().getPropertyIntArray(propName);
        }
        catch (Throwable t) {
          // nop
        }
      }
    }
    if (a != null && a.length == 2) {
      setSplitterPosition(a[0], a[1]);
    }
    else {
      setSplitterPositionFromScout();
    }
  }

  @Override
  protected void detachScout() {
    // XXX There is no listener on the sash for split location changes. save it
    // when the component is disposed
    setSplitterPositionFromSwt();
    super.detachScout();
  }

  @Override
  public SashForm getSwtContainer() {
    return (SashForm) super.getSwtContainer();
  }

  protected void setSplitterPositionFromSwt() {
    String propName = getScoutObject().getCacheSplitterPositionPropertyName();
    if (!StringUtility.isNullOrEmpty(propName)) {
      if (getScoutObject().isCacheSplitterPosition()) {
        int[] weights = getSwtContainer().getWeights();
        ClientUIPreferences.getInstance().setPropertyIntArray(propName, weights);
      }
      else {
        ClientUIPreferences.getInstance().setPropertyIntArray(propName, null);
      }
    }
  }

  protected void setSplitterPositionFromScout() {
    try {
      if (lockSplitter.acquire()) {
        double position = getScoutObject().getSplitterPosition();
        int[] weights = new int[2];
        weights[0] = (int) (position * 100.0);
        weights[1] = 100 - weights[0];
        getSwtContainer().setWeights(weights);
      }
    }
    finally {
      lockSplitter.release();
    }
  }

  protected void setSplitterPosition(int leftWidth, int rightWidth) {
    try {
      if (lockSplitter.acquire()) {
        int total = leftWidth + rightWidth;
        if (total > 0) {
          int leftPct = leftWidth * 100 / total;
          int rightPct = rightWidth * 100 / total;
          getSwtContainer().setWeights(new int[]{leftPct, rightPct});
        }
      }
    }
    finally {
      lockSplitter.release();
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (ISplitBox.PROP_SPLITTER_ENABLED.equals(name)) {

    }
    else if (ISplitBox.PROP_SPLITTER_POSITION.equals(name)) {
      setSplitterPositionFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }
}
