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

import java.lang.reflect.Method;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.VersionUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutLabelField</h3> ...
 * 
 * @since 1.0.0 28.04.2008
 */
public class SwtScoutLabelField extends SwtScoutValueFieldComposite<ILabelField> implements ISwtScoutLabelField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutLabelField.class);

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    int style = SWT.NONE;
    if (getScoutObject().isWrapText()) {
      style |= SWT.WRAP;
    }

    //Always set to multiline so that line breaks are visible even if wrapText is set to false
    style |= SWT.MULTI;

    StyledText text = getEnvironment().getFormToolkit().createStyledText(container, style);
    text.setBackground(container.getBackground());
    //Editing the text is never allowed at label fields
    text.setEditable(false);

    if (VersionUtility.isEclipseVersionLessThan35()) {
      //FIXME we need a bugfix for bug 350237
    }
    else {
      try {
        //Make sure the wrap indent is the same as the indent so that the text is vertically aligned
        Method setWrapIndent = StyledText.class.getMethod("setWrapIndent", int.class);
        setWrapIndent.invoke(text, text.getIndent());
        //Make sure the text is horizontally aligned with the label
        int borderWidth = 4;
        Method setMargins = StyledText.class.getMethod("setMargins", int.class, int.class, int.class, int.class);
        setMargins.invoke(text, 0, borderWidth, 0, borderWidth);
      }
      catch (Exception e) {
        LOG.warn("could not access methods 'setWrapIndent' or 'setMargins' on 'StyledText'.", e);
      }
    }

    LogicalGridData textData = LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData());
    text.setLayoutData(textData);
    container.setTabList(new Control[]{});
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(text);
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
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
    getSwtContainer().layout(true, true);
  }

  protected void setTextWrapFromScout(boolean booleanValue) {
    // TODO sle wrap does not work as expected
    // getSwtField().setWordWrap(booleanValue);
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    // void here
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
