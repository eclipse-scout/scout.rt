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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>RwtScoutNumberField</h3>
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutNumberField extends RwtScoutBasicFieldComposite<INumberField<?>> implements IRwtScoutNumberField {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutNumberField.class);

  private TextFieldEditableSupport m_editableSupport;
  private static volatile String clientVerifyScript;
  private static final Object LOCK = new Object();

  private RwtContextMenuMarkerComposite m_menuMarkerComposite;
  private RwtScoutContextMenu m_uiContextMenu;
  private P_ContextMenuPropertyListener m_contextMenuPropertyListener;

  // Constants must correspond to the keys used in org/eclipse/scout/rt/ui/rap/form/fields/numberfield/RwtScoutNumberField.js
  public static final String PROP_MAX_INTEGER_DIGITS = "RwtScoutNumberField.maxInt";
  public static final String PROP_MAX_FRACTION_DIGITS = "RwtScoutNumberField.maxFra";
  public static final String PROP_ZERO_DIGIT = "RwtScoutNumberField.zeroDig";
  public static final String PROP_DECIMAL_SEPARATOR = "RwtScoutNumberField.decSep";

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_menuMarkerComposite = new RwtContextMenuMarkerComposite(container, getUiEnvironment());
    getUiEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.setData(RWT.CUSTOM_VARIANT, RwtUtility.VARIANT_COMPOSITE_INPUT_FIELD_BORDER);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (getUiContextMenu() != null) {
          Menu uiMenu = getUiContextMenu().getUiMenu();
          if (e.widget instanceof Control) {
            Point loc = ((Control) e.widget).toDisplay(e.x, e.y);
            uiMenu.setLocation(RwtMenuUtility.getMenuLocation(getScoutObject().getContextMenu().getChildActions(), uiMenu, loc, getUiEnvironment()));
          }
          uiMenu.setVisible(true);
        }
      }
    });

    int style = SWT.None;
    style |= RwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment);
    StyledText textField = getUiEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, style);
    installClientScripting(textField);
    attachFocusListener(textField, true);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
  }

  @Override
  public StyledText getUiField() {
    return (StyledText) super.getUiField();
  }

  public RwtScoutContextMenu getUiContextMenu() {
    return m_uiContextMenu;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    handleDecimalFormatChanged(getScoutObject().getFormat());
    // context menu
    updateContextMenuVisibilityFromScout();
    if (getScoutObject().getContextMenu() != null && m_contextMenuPropertyListener == null) {
      m_contextMenuPropertyListener = new P_ContextMenuPropertyListener();
      getScoutObject().getContextMenu().addPropertyChangeListener(IContextMenu.PROP_VISIBLE, m_contextMenuPropertyListener);
    }
  }

  @Override
  protected void detachScout() {
    // context menu listener
    if (m_contextMenuPropertyListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(IContextMenu.PROP_VISIBLE, m_contextMenuPropertyListener);
      m_contextMenuPropertyListener = null;
    }
    super.detachScout();
  }

  @SuppressWarnings("restriction")
  protected void installClientScripting(StyledText text) {
    String js = getVerifyClientScript();
    if (js != null) {
      text.addListener(SWT.Verify, new ClientListener(js));
      org.eclipse.rap.rwt.internal.lifecycle.WidgetDataUtil.registerDataKeys(PROP_MAX_INTEGER_DIGITS, PROP_MAX_FRACTION_DIGITS, PROP_ZERO_DIGIT, PROP_DECIMAL_SEPARATOR);
    }
  }

  protected void updateContextMenuVisibilityFromScout() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    if (getScoutObject().getContextMenu().isVisible()) {
      if (m_uiContextMenu == null) {
        m_uiContextMenu = new RwtScoutContextMenu(getUiField().getShell(), getScoutObject().getContextMenu(), getUiEnvironment());
      }
    }
    else {
      if (m_uiContextMenu != null) {
        m_uiContextMenu.dispose();
      }
      m_uiContextMenu = null;
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (INumberField.PROP_DECIMAL_FORMAT.equals(name)) {
      handleDecimalFormatChanged((DecimalFormat) newValue);
    }
  }

  protected void handleDecimalFormatChanged(DecimalFormat format) {
    getUiField().setData(PROP_MAX_INTEGER_DIGITS, format.getMaximumIntegerDigits());
    getUiField().setData(PROP_MAX_FRACTION_DIGITS, format.getMaximumFractionDigits());
    getUiField().setData(PROP_ZERO_DIGIT, "" + format.getDecimalFormatSymbols().getZeroDigit());
    getUiField().setData(PROP_DECIMAL_SEPARATOR, "" + format.getDecimalFormatSymbols().getDecimalSeparator());
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
    if (b) {
      m_menuMarkerComposite.setData(RWT.CUSTOM_VARIANT, RwtUtility.VARIANT_COMPOSITE_INPUT_FIELD_BORDER);
    }
    else {
      m_menuMarkerComposite.setData(RWT.CUSTOM_VARIANT, RwtUtility.VARIANT_COMPOSITE_INPUT_FIELD_BORDER_READONLY);
    }
  }

  private class P_ContextMenuPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IContextMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        // synchronize
        getUiEnvironment().invokeUiLater(new Runnable() {
          @Override
          public void run() {
            updateContextMenuVisibilityFromScout();
          }
        });
      }
    }
  }
}
