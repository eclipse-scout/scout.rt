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
package org.eclipse.scout.rt.ui.swt.form.fields.labelfield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>SwtScoutLabelField</h3> ...
 * 
 * @since 1.0.0 28.04.2008
 */
public class SwtScoutLabelField extends SwtScoutValueFieldComposite<ILabelField> implements ISwtScoutLabelField {

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    // label
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    int style = SWT.NONE;
    if (getScoutObject().isWrapText()) {
      style |= SWT.WRAP;
    }
    Label text = getEnvironment().getFormToolkit().createLabel(container, "", style);
    LogicalGridData textData = LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData());
    textData.topInset = 4;
    text.setLayoutData(textData);
    container.setTabList(new Control[]{});
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(text);
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  public Label getSwtField() {
    return (Label) super.getSwtField();
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    super.attachScout();
    setTextWrapFromScout(getScoutObject().isWrapText());
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    String oldText = getSwtField().getText();
    if (s == null) {
      s = "";
    }
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(s)) {
      return;
    }
    getSwtField().setText(s);
    getSwtContainer().layout(true);
  }

  protected void setTextWrapFromScout(boolean booleanValue) {
    // XXX
    // getSwtField().setWordWrap(booleanValue);
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {

    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ILabelField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }

}
