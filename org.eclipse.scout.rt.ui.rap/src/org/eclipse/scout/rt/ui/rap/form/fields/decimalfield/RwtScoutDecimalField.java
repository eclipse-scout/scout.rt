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
package org.eclipse.scout.rt.ui.rap.form.fields.decimalfield;

import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>RwtScoutDoubleField</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutDecimalField extends RwtScoutBasicFieldComposite<IDecimalField<?>> implements IRwtScoutDecimalField {
  private TextFieldEditableSupport m_editableSupport;

  @Override
  protected void initializeUi(Composite parent) {
    int style = SWT.BORDER;
    style |= RwtUtility.getVerticalAlignment(getScoutObject().getGridData().verticalAlignment);
    style |= RwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment);

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    StyledText text = new StyledTextEx(container, style);
    text.setTextLimit(32);
    attachFocusListener(text, true);
    //
    setUiContainer(container);
    setUiLabel(label);
    setUiField(text);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    getUiField().setEnabled(b);
  }
}
