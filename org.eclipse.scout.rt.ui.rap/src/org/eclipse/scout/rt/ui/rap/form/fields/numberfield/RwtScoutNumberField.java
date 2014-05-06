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
package org.eclipse.scout.rt.ui.rap.form.fields.numberfield;

import java.io.InputStream;
import java.text.DecimalFormat;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>RwtScoutNumberField</h3>
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutNumberField extends RwtScoutBasicFieldComposite<INumberField<?>> implements IRwtScoutNumberField {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutNumberField.class);

  private TextFieldEditableSupport m_editableSupport;
  private static volatile String clientVerifyScript;
  private RwtContextMenuMarkerComposite m_markerComposite;
  private static final Object LOCK = new Object();

  // Constants must correspond to the keys used in org/eclipse/scout/rt/ui/rap/form/fields/numberfield/RwtScoutNumberField.js
  public static final String PROP_MAX_INTEGER_DIGITS = "RwtScoutNumberField.maxInt";
  public static final String PROP_MAX_FRACTION_DIGITS = "RwtScoutNumberField.maxFra";
  public static final String PROP_ZERO_DIGIT = "RwtScoutNumberField.zeroDig";
  public static final String PROP_DECIMAL_SEPARATOR = "RwtScoutNumberField.decSep";

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_markerComposite = new RwtContextMenuMarkerComposite(container, getUiEnvironment());
    getUiEnvironment().getFormToolkit().adapt(m_markerComposite);

    int style = SWT.None;
    style |= RwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment);
    StyledText textField = getUiEnvironment().getFormToolkit().createStyledText(m_markerComposite, style);
    installClientScripting(textField);
    attachFocusListener(textField, true);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    m_markerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
  }

  @Override
  public StyledText getUiField() {
    return (StyledText) super.getUiField();
  }

  @SuppressWarnings("restriction")
  protected void installClientScripting(StyledText text) {
    String js = getVerifyClientScript();
    if (js != null) {
      text.addListener(SWT.Verify, new ClientListener(js));
      org.eclipse.rap.rwt.internal.lifecycle.WidgetDataUtil.registerDataKeys(PROP_MAX_INTEGER_DIGITS, PROP_MAX_FRACTION_DIGITS, PROP_ZERO_DIGIT, PROP_DECIMAL_SEPARATOR);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (INumberField.PROP_DECIMAL_FORMAT.equals(name)) {
      handleDecimalFormatChanged(getUiField(), (DecimalFormat) newValue);
    }
  }

  protected void handleDecimalFormatChanged(StyledText text, DecimalFormat format) {
    text.setData(PROP_MAX_INTEGER_DIGITS, format.getMaximumIntegerDigits());
    text.setData(PROP_MAX_FRACTION_DIGITS, format.getMaximumFractionDigits());
    text.setData(PROP_ZERO_DIGIT, "" + format.getDecimalFormatSymbols().getZeroDigit());
    text.setData(PROP_DECIMAL_SEPARATOR, "" + format.getDecimalFormatSymbols().getDecimalSeparator());
  }

  private static String getVerifyClientScript() {
    if (clientVerifyScript == null) {
      synchronized (LOCK) {
        if (clientVerifyScript == null) {
          try {
            InputStream is = RwtScoutNumberField.class.getClassLoader().getResourceAsStream("org/eclipse/scout/rt/ui/rap/form/fields/numberfield/RwtScoutNumberField.js");
            String content = IOUtility.getContentUtf8(is);
            clientVerifyScript = content;
          }
          catch (ProcessingException e) {
            LOG.error("Unable to read NumberField client verify script.", e);
          }
        }
      }
    }
    return clientVerifyScript;
  }

  protected void installContextMenu() {
    new RwtScoutContextMenu(m_markerComposite.getShell(), getScoutObject().getContextMenu(), m_markerComposite, getUiEnvironment());

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
