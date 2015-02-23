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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.ColorUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.action.menu.text.StyledTextAccess;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtScoutColorField extends SwtScoutBasicFieldComposite<IColorField> implements ISwtScoutColorField {

  private ColorCanvas m_colorCanvas;
  private Button m_colorPickButton;
  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwt(Composite parent) {

    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    m_colorCanvas = new ColorCanvas(container, getEnvironment());

    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment());
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, SWT.SINGLE);
    textField.setAlignment(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    textField.setWrapIndent(textField.getWrapIndent());
    textField.setMargins(2, 2, 2, 2);
    // key stokes
    textField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.stateMask == 0) {
          switch (e.keyCode) {
            case SWT.ARROW_DOWN:
            case SWT.F2:
              handleSwtPickColor();
              break;
          }
        }
      }
    });

    m_colorPickButton = getEnvironment().getFormToolkit().createButton(container, SWT.PUSH | SWT.NO_FOCUS);
    m_colorPickButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSwtPickColor();
      }
    });

    container.setTabList(new Control[]{m_menuMarkerComposite});

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    LogicalGridData data = LogicalGridDataBuilder.createButton1();
    data.gridx = 1;
    m_colorCanvas.setLayoutData(data);
    data = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    data.gridx = 2;
    m_menuMarkerComposite.setLayoutData(data);
    data = LogicalGridDataBuilder.createButton1();
    data.gridx = 3;
    m_colorPickButton.setLayoutData(data);
  }

  protected void installContextMenu() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          final boolean markerVisible = getScoutObject().getContextMenu().isVisible();
          getEnvironment().invokeSwtLater(new Runnable() {
            @Override
            public void run() {
              m_menuMarkerComposite.setMarkerVisible(markerVisible);
            }
          });
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);

    m_contextMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment());
    getColorPickButton().setMenu(m_contextMenu.getSwtMenu());

    SwtScoutContextMenu fieldMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment(),
        getScoutObject().isAutoAddDefaultMenus() ? new StyledTextAccess(getSwtField()) : null, getScoutObject().isAutoAddDefaultMenus() ? getSwtField() : null);
    getSwtField().setMenu(fieldMenu.getSwtMenu());
    // correction of menu position
    getSwtField().addListener(SWT.MenuDetect, new MenuPositionCorrectionListener(getSwtField()));
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconIdFromScout();
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    uninstallContextMenu();
    super.detachScout();
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
    try {
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
    finally {
      if (!getSwtField().isDisposed()) {
        getSwtField().setFocus();
      }
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
    if (name.equals(IColorField.PROP_ICON_ID)) {
      updateIconIdFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

}
