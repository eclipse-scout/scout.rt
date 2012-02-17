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
package org.eclipse.scout.rt.ui.rap.form.fields.splitbox;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.ui.rap.core.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.rap.core.IValidateRoot;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>RwtScoutSplitBox</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutSplitBox extends RwtScoutFieldComposite<ISplitBox> implements IRwtScoutSplitBox {

  private OptimisticLock lockSplitter;

  public RwtScoutSplitBox() {
    lockSplitter = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    SashForm container = getUiEnvironment().getFormToolkit().createSashForm(parent, getScoutObject().isSplitHorizontal() ? SWT.HORIZONTAL : SWT.VERTICAL);
    container.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(container));
    for (IFormField scoutField : getScoutObject().getFields()) {
      getUiEnvironment().createFormField(container, scoutField);
    }
    setUiContainer(container);
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
          a = ClientUIPreferences.getInstance(getUiEnvironment().getClientSession()).getPropertyIntArray(propName);
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
    setSplitterPositionFromUi();
    super.detachScout();
  }

  @Override
  public SashForm getUiContainer() {
    return (SashForm) super.getUiContainer();
  }

  protected void setSplitterPositionFromUi() {
    String propName = getScoutObject().getCacheSplitterPositionPropertyName();
    if (!StringUtility.isNullOrEmpty(propName)) {
      if (getScoutObject().isCacheSplitterPosition()) {
        int[] weights = getUiContainer().getWeights();
        ClientUIPreferences.getInstance(getUiEnvironment().getClientSession()).setPropertyIntArray(propName, weights);
      }
      else {
        ClientUIPreferences.getInstance(getUiEnvironment().getClientSession()).setPropertyIntArray(propName, null);
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
        getUiContainer().setWeights(weights);
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
          getUiContainer().setWeights(new int[]{leftPct, rightPct});
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
