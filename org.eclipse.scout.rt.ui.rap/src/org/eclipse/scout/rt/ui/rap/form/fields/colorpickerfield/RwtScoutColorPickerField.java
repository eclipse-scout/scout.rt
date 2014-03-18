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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.ColorUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorPickerField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

public class RwtScoutColorPickerField extends RwtScoutBasicFieldComposite<IColorPickerField> implements IRwtScoutColorPickerField {
  private Menu m_contextMenu;
  private Label m_colorPreviewLabel;
  private Button m_colorPickButton;
  private Composite m_colorPickerContainer;

  private TextFieldEditableSupport m_editableSupport;
  private Color m_previewColor;

  @Override
  protected void initializeUi(Composite parent) {

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_colorPickerContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldVariant());

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

    StyledText textField = new StyledTextEx(m_colorPickerContainer, SWT.SINGLE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    // correction to look like a normal text
    textField.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldVariant());

    m_colorPickButton = getUiEnvironment().getFormToolkit().createButtonEx(m_colorPickerContainer, SWT.PUSH);
    m_colorPickButton.setImage(getUiEnvironment().getIcon(AbstractIcons.Palette));

    // listeners
    attachFocusListener(textField, true);
    m_colorPickButton.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseDown(MouseEvent e) {
        handleSwtPickColor();
      }
    });

    // context menu
    m_contextMenu = new Menu(m_colorPreviewLabel.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener((Control) m_colorPickButton, getUiField()));
    m_colorPreviewLabel.setMenu(m_contextMenu);

    m_colorPickerContainer.setTabList(new Control[]{textField});
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
    textField.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    m_colorPickButton.setLayoutData(buttonLayoutData);
  }

  protected String getColorPickerFieldVariant() {
    return VARIANT_COLORPICKERFIELD;
  }

  protected String getColorPickerFieldDisabledVariant() {
    return VARIANT_COLORPICKERFIELD_DISABLED;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconIdFromScout();
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

  protected void handleSwtPickColor() {

    ColorDialog colorDialog = new ColorDialog(getUiContainer().getShell());
    colorDialog.setText(ScoutTexts.get("ColorPickerSelectColor"));
    RGB rgb = org.eclipse.scout.rt.ui.rap.basic.ColorUtility.toRGB(getScoutObject().getValue());
    if (rgb != null) {
      colorDialog.setRGB(rgb);
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
      m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldVariant());
    }
    else {
      m_colorPickerContainer.setData(RWT.CUSTOM_VARIANT, getColorPickerFieldDisabledVariant());
    }
  }

  protected void updateIconIdFromScout() {
    String iconId = getScoutObject().getIconId();
    if (m_colorPickButton.getLayoutData() instanceof GridData) {
      GridData layoutData = (GridData) m_colorPickButton.getLayoutData();
      layoutData.exclude = StringUtility.isNullOrEmpty(iconId);
    }
    m_colorPickButton.setData(RWT.CUSTOM_VARIANT, iconId);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IColorPickerField.PROP_ICON_ID.equals(name)) {
      updateIconIdFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  private class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener(Control menuControl, Control keyStrokeWidget) {
      super(menuControl, keyStrokeWidget);
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      final AtomicReference<List<IMenu>> scoutMenusRef = new AtomicReference<List<IMenu>>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          List<IMenu> scoutMenus = getScoutObject().getUIFacade().firePopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        Menu menu = ((Menu) e.getSource());
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), getUiEnvironment(), menu);
      }
    }
  } // end class P_ContextMenuListener

}
