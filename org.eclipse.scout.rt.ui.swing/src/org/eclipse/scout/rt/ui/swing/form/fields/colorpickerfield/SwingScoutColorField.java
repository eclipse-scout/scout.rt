/*******************************************************************************
 * Copyright (c) 2014 Schweizerische Bundesbahnen SBB, BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Schweizerische Bundesbahnen SBB - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.colorpickerfield;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DropDownDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.IDecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutBasicFieldComposite;

public class SwingScoutColorField extends SwingScoutBasicFieldComposite<IColorField> implements ISwingScoutColorField {
  private JTextField m_colorPreview;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private DropDownDecorationItem m_dropdownIcon;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx(new BorderLayoutEx());
    container.setOpaque(false);

    JStatusLabelEx label = new JStatusLabelEx();
    container.add(label, BorderLayoutEx.CENTER);

    m_colorPreview = new JTextField();
    m_colorPreview.setEditable(false);
    container.add(m_colorPreview);

    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    addInputListenersForBasicField(textField, doc);
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new BasicDocumentFilter(60));
    }
    doc.addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        setInputDirty(true);
      }
    });

    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);

    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "colorPicker");
    inputMap.put(SwingUtility.createKeystroke("DOWN"), "colorPicker");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("colorPicker", new P_SwingColorPickerAction());

    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
    LogicalGridData data = LogicalGridDataBuilder.createButton1(getSwingEnvironment());
    data.gridx = LogicalGridDataBuilder.FIELD_GRID_X;
    data.widthHint = 20;
    data.fillVertical = true;
    m_colorPreview.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, data);
    data = LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData());
    data.gridx += 1;
    textField.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, data);

  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextComponent createTextField(JComponent container) {
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    container.add(textField);
    IDecorationGroup decorationGroup = new DecorationGroup(textField, getSwingEnvironment());
    // context menu marker
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    decorationGroup.addDecoration(m_contextMenuMarker);

    // smart chooser decoration
    m_dropdownIcon = new DropDownDecorationItem(textField, getSwingEnvironment());
    m_dropdownIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
        }
        else {
          getSwingField().requestFocus();
          handleUiPickColor();
        }
      }
    });
    decorationGroup.addDecoration(m_dropdownIcon);

    textField.setDecorationIcon(decorationGroup);
    return textField;

  }

  protected void installContextMenu() {
    m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingField(), getScoutObject().getContextMenu(), getSwingEnvironment());
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
    if (m_contextMenuMarker != null) {
      m_contextMenuMarker.destroy();
    }
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  public JTextComponent getSwingField() {
    return (JTextComponent) super.getSwingField();
  }

  protected void handleUiPickColor() {
    Color color = ColorUtility.createColor(getScoutObject().getValue());
    Color selectedColor = JColorChooser.showDialog(getSwingField(), ScoutTexts.get("ColorPickerSelectColor"), color);
    if (selectedColor != null) {
      final String colorString = org.eclipse.scout.commons.ColorUtility.rgbToText(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()).toUpperCase();
      RunnableWithData t = new RunnableWithData() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setValueFromUi(colorString);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 2345);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropdownIcon.setEnabled(b);
  }

  @Override
  protected void setValueFromScout(Object o) {
    String value = getScoutObject().getValue();
    if (value == null) {
      value = "";
    }
    Color c = ColorUtility.createColor(value);
    m_colorPreview.setBackground(c);
    getSwingField().setText(value);
  }

  /**
   *
   */
  protected void updateIconIdFromScout() {
    String iconId = getScoutObject().getIconId();
    if (iconId == null) {
      m_dropdownIcon.setVisible(false);
    }
    else {
      m_dropdownIcon.setIconGroup(new IconGroup(getSwingEnvironment(), iconId));
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IColorField.PROP_ICON_ID)) {
      updateIconIdFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  @Override
  protected void setSelectionFromSwing() {
    //Nothing to do: Selection is not stored in model for DateField.
  }

  @Override
  protected boolean isSelectAllOnFocusInScout() {
    return true; //No such property in Scout for DateField.
  }

  private class P_SwingColorPickerAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      handleUiPickColor();
    }
  }
}
