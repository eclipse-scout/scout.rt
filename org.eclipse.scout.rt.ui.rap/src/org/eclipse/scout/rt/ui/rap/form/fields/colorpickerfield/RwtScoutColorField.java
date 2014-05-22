/*******************************************************************************
 * Copyright (c) 2014 Schweizerische Bundesbahnen SBB, BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Schweizerische Bundesbahnen SBB - initial API and implementation and/or initial documentation
 *    BSI Buseiness Systems Integration AG - enhancements input field
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.colorpickerfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.ColorUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

public class RwtScoutColorField extends RwtScoutBasicFieldComposite<IColorField> implements IRwtScoutColorField {
  private Label m_colorPreviewLabel;
  private Button m_colorPickButton;
  private Composite m_colorPickerContainer;

  private TextFieldEditableSupport m_editableSupport;
  private Color m_previewColor;

  private RwtContextMenuMarkerComposite m_menuMarkerComposite;
  private RwtScoutContextMenu m_uiContextMenu;
  private P_ContextMenuPropertyListener m_contextMenuPropertyListener;

  @Override
  protected void initializeUi(Composite parent) {

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_colorPickerContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, VARIANT_COLOR_FIELD);

    m_colorPreviewLabel = getUiEnvironment().getFormToolkit().createLabel(m_colorPickerContainer, "");
    m_colorPreviewLabel.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent event) {
        if (m_previewColor != null) {
          m_previewColor.dispose();
        }
      }
    });
    m_colorPreviewLabel.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldVariant());

    Label splitLabel = getUiEnvironment().getFormToolkit().createLabel(m_colorPickerContainer, "");
    splitLabel.setData(RWT.CUSTOM_VARIANT, "colorpickerfield_splitLabel");

    m_menuMarkerComposite = new RwtContextMenuMarkerComposite(m_colorPickerContainer, getUiEnvironment(), SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
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

    StyledText textField = new StyledTextEx(m_menuMarkerComposite, SWT.SINGLE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    // correction to look like a normal text
    textField.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldVariant());

    m_colorPickButton = getUiEnvironment().getFormToolkit().createButton(m_colorPickerContainer, "", SWT.PUSH);
    m_colorPickButton.setImage(getUiEnvironment().getIcon(AbstractIcons.Palette));

    // listeners
    attachFocusListener(textField, true);
    m_colorPickButton.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseUp(MouseEvent e) {
        if (e.button == 1) {
          handleUiPickColor();
        }
      }
    });

    m_colorPickerContainer.setTabList(new Control[]{m_menuMarkerComposite});
    m_colorPickButton.addFocusListener(new FocusAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void focusGained(FocusEvent e) {
        getUiField().setFocus();
      }
    });

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    // key binding
    getUiEnvironment().addKeyStroke(textField, new P_F2KeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ArrowDownKeyStroke(), false);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_colorPickerContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_colorPickerContainer.setLayout(RwtLayoutUtility.createGridLayoutNoSpacing(4, false));

    GridData colorPreviewLabelData = new GridData(SWT.CENTER, SWT.FILL, false, false);
    colorPreviewLabelData.widthHint = 20;
    m_colorPreviewLabel.setLayoutData(colorPreviewLabelData);

    GridData splitLabelData = new GridData(SWT.CENTER, SWT.FILL, false, true);
    splitLabelData.widthHint = 1;
    splitLabel.setLayoutData(splitLabelData);

    GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_menuMarkerComposite.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    m_colorPickButton.setLayoutData(buttonLayoutData);
  }

  public RwtScoutContextMenu getUiContextMenu() {
    return m_uiContextMenu;
  }

  protected String getColorPickerFieldVariant() {
    return VARIANT_COLOR_FIELD;
  }

  protected String getColorPickerFieldDisabledVariant() {
    return VARIANT_COLOR_FIELD_DISABLED;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconIdFromScout();
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

  @Override
  public StyledText getUiField() {
    return (StyledText) super.getUiField();
  }

  @Override
  protected void setFieldEnabled(Control rwtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    final String text = getUiField().getText();
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text, false);
        result.setValue(b);
      }
    };
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    getUiEnvironment().dispatchImmediateUiJobs();
  }

  protected void handleUiPickColor() {

    ColorDialog colorDialog = new ColorDialog(getUiContainer().getShell());
    colorDialog.setText(ScoutTexts.get("ColorPickerSelectColor"));
    if (getScoutObject().getValue() != null) {
      RGB rgb = org.eclipse.scout.rt.ui.rap.basic.ColorUtility.toRGB(getScoutObject().getValue());
      if (rgb != null) {
        colorDialog.setRGB(rgb);
      }
    }
    RGB selectedColor = colorDialog.open();
    if (selectedColor != null) {

      final String color = ColorUtility.rgbToText(selectedColor.red, selectedColor.green, selectedColor.blue).toUpperCase();

      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setValueFromUi(color);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 2345);
    }
  }

  @Override
  protected void setValueFromScout() {
    String value = getScoutObject().getValue();
    if (value == null) {
      value = "";
    }
    if (m_previewColor != null) {
      m_previewColor.dispose();
      m_previewColor = null;
    }
    m_previewColor = org.eclipse.scout.rt.ui.rap.basic.ColorUtility.createColor(m_colorPreviewLabel.getDisplay(), value);
    m_colorPreviewLabel.setBackground(m_previewColor);
    getUiField().setText(value);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_colorPickButton.setEnabled(b);
    getUiField().setEnabled(b);
    if (b) {
      m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, VARIANT_COLOR_FIELD);
    }
    else {
      m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, VARIANT_COLOR_FIELD_DISABLED);
    }
  }

  protected void updateIconIdFromScout() {
    String iconId = getScoutObject().getIconId();
    m_colorPickButton.setData(RWT.CUSTOM_VARIANT, iconId);
    if (StringUtility.isNullOrEmpty(iconId)) {
      if (m_colorPickButton.getLayoutData() instanceof GridData) {
        GridData layoutData = (GridData) m_colorPickButton.getLayoutData();
        layoutData.exclude = true;
      }
      m_colorPickButton.setVisible(false);
    }
    else {
      if (m_colorPickButton.getLayoutData() instanceof GridData) {
        GridData layoutData = (GridData) m_colorPickButton.getLayoutData();
        layoutData.exclude = false;
      }
      m_colorPickButton.setVisible(true);
    }
  }

  protected void updateContextMenuVisibilityFromScout() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    if (getScoutObject().getContextMenu().isVisible()) {
      if (m_uiContextMenu == null) {
        m_uiContextMenu = new RwtScoutContextMenu(getUiField().getShell(), getScoutObject().getContextMenu(), getUiEnvironment());
        m_colorPickButton.setMenu(m_uiContextMenu.getUiMenu());
        m_colorPreviewLabel.setMenu(m_uiContextMenu.getUiMenu());
      }
    }
    else {
      m_colorPickButton.setMenu(null);
      m_colorPreviewLabel.setMenu(null);
      if (m_uiContextMenu != null) {
        m_uiContextMenu.dispose();
      }
      m_uiContextMenu = null;
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IColorField.PROP_ICON_ID.equals(name)) {
      updateIconIdFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  private class P_F2KeyStroke extends RwtKeyStroke {
    public P_F2KeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiPickColor();
    }
  }

  private class P_ArrowDownKeyStroke extends RwtKeyStroke {
    public P_ArrowDownKeyStroke() {
      super(SWT.ARROW_DOWN);
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiPickColor();
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
