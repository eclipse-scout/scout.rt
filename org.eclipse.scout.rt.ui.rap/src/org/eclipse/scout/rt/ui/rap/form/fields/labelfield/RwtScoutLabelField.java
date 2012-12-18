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
package org.eclipse.scout.rt.ui.rap.form.fields.labelfield;

import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @since 3.8.0
 */
public class RwtScoutLabelField extends RwtScoutValueFieldComposite<ILabelField> implements IRwtScoutLabelField {
  private static final String VARIANT_LABELFIELD = "labelfield";

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    // label
    if (getScoutObject().isLabelVisible()) {
      StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());
      setUiLabel(label);
    }
    //
    int style = SWT.NONE;
    if (getScoutObject().isWrapText()) {
      style |= SWT.WRAP;
    }

    //LabelContainer is only necessary because labels don't support margins -> see css for container padding
    final Composite labelContainer = getUiEnvironment().getFormToolkit().createComposite(container);
    labelContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    labelContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_LABELFIELD);
    labelContainer.setLayout(new FillLayout());

    Label text = getUiEnvironment().getFormToolkit().createLabel(labelContainer, "", style);
    setUiField(text);
    //
    container.setTabList(new Control[]{});
    setUiContainer(container);
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  protected boolean isAutoSetLayoutData() {
    return false;
  }

  @Override
  public Label getUiField() {
    return (Label) super.getUiField();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    String oldText = getUiField().getText();
    if (s == null) {
      s = "";
    }
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(s)) {
      return;
    }
    getUiField().setText(s);
    getUiContainer().layout(true, true);
  }

}
