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
package org.eclipse.scout.rt.ui.swt.form.fields.colorpickerfield;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.ColorUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorPickerField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SwtScoutColorPickerField extends SwtScoutBasicFieldComposite<IColorPickerField> implements ISwtScoutColorPickerField {

  private Menu m_contextMenu;
  private ColorCanvas m_colorCanvas;
  private Button m_colorPickButton;

  @Override
  protected void initializeSwt(Composite parent) {

    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    m_colorCanvas = new ColorCanvas(container, getEnvironment());
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);

    m_colorPickButton = getEnvironment().getFormToolkit().createButtonEx(container, SWT.PUSH);
    m_colorPickButton.setImage(getEnvironment().getIcon(AbstractIcons.Palette));
    m_colorPickButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        handleSwtPickColor();
      }
    });

    // context menu
    m_contextMenu = new Menu(m_colorCanvas.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener());
    m_colorCanvas.setMenu(m_contextMenu);

    container.setTabList(new Control[]{textField});
    m_colorPickButton.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        getSwtField().setFocus();
      }
    });

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    LogicalGridData data = new LogicalGridData();
    data.gridx = 1;
    data.gridy = 1;
    data.heightHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonHeight();
    data.widthHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonWidth();
    data.fillVertical = false;
    data.fillHorizontal = false;
    m_colorCanvas.setLayoutData(data);
    data = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    data.gridx = 2;
    textField.setLayoutData(data);
    data = LogicalGridDataBuilder.createButton1();
    data.gridx = 3;
    m_colorPickButton.setLayoutData(data);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconIdFromScout();
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
  }

  @Override
  public Button getColorPickButton() {
    return m_colorPickButton;
  }

  @Override
  public ColorCanvas getColorCanvas() {
    return m_colorCanvas;
  }

  @Override
  protected String getText() {
    return getSwtField().getText();
  }

  @Override
  protected void setText(String text) {
    getSwtField().setText(text);
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
  }

  @Override
  protected void setSelection(int startIndex, int endIndex) {
    getSwtField().setSelection(startIndex, endIndex);
  }

  @Override
  protected int getCaretOffset() {
    return getSwtField().getCaretOffset();
  }

  @Override
  protected void setCaretOffset(int caretPosition) {
    getSwtField().setCaretOffset(caretPosition);
  }

  @Override
  protected TextFieldEditableSupport createEditableSupport() {
    return new TextFieldEditableSupport(getSwtField());
  }

  protected void handleSwtPickColor() {

    ColorDialog colorDialog = new ColorDialog(getSwtContainer().getShell());
    colorDialog.setText(ScoutTexts.get("ColorPickerSelectColor"));
    RGB rgb = org.eclipse.scout.rt.ui.swt.basic.ColorUtility.toRGB(getScoutObject().getValue());
    if (rgb != null) {
      colorDialog.setRGB(rgb);

    }
    RGB selectedColor = colorDialog.open();
    if (selectedColor != null) {
      final String color = ColorUtility.rgbToText(selectedColor.red, selectedColor.green, selectedColor.blue);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setValueFromUi(color);
        }
      };
      getEnvironment().invokeScoutLater(t, 2345);
    }
  }

  @Override
  protected void updateValueFromScout() {
    String value = getScoutObject().getValue();
    if (value == null) {
      value = "";
    }
    m_colorCanvas.setColor(value);
    getSwtField().setText(value);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_colorPickButton.setEnabled(b);
  }

  protected void updateIconIdFromScout() {
    m_colorPickButton.setImage(getEnvironment().getIcon(getScoutObject().getIconId()));
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IColorPickerField.PROP_ICON_ID)) {
      updateIconIdFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  private class P_ContextMenuListener extends MenuAdapter {
    @Override
    public void menuShown(MenuEvent e) {
      for (MenuItem item : m_contextMenu.getItems()) {
        disposeMenuItem(item);
      }
      final AtomicReference<List<IMenu>> scoutMenusRef = new AtomicReference<List<IMenu>>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          List<IMenu> scoutMenus = getScoutObject().getUIFacade().firePopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = getEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        // nop
      }
      // grab the actions out of the job, when the actions are providden within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        SwtMenuUtility.fillContextMenu(scoutMenusRef.get(), m_contextMenu, getEnvironment());
      }
    }

    private void disposeMenuItem(MenuItem item) {
      Menu menu = item.getMenu();
      if (menu != null) {
        for (MenuItem childItem : menu.getItems()) {
          disposeMenuItem(childItem);
        }
        menu.dispose();
      }
      item.dispose();
    }

  } // end class P_ContextMenuListener

}
