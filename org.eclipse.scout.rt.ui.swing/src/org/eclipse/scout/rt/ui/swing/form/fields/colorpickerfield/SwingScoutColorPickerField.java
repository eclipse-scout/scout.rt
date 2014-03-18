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
import java.awt.Point;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.IColorPickerField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.IDropDownButtonListener;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldWithDropDownButton;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutBasicFieldComposite;

public class SwingScoutColorPickerField extends SwingScoutBasicFieldComposite<IColorPickerField> implements ISwingScoutColorPickerField {
  private JTextField m_colorPreview;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx(new BorderLayoutEx());
    container.setOpaque(false);

    JStatusLabelEx label = new JStatusLabelEx();
    container.add(label, BorderLayoutEx.CENTER);

    m_colorPreview = new JTextField();
    m_colorPreview.setEditable(false);
    container.add(m_colorPreview);

    JTextFieldWithDropDownButton textField = new JTextFieldWithDropDownButton(getSwingEnvironment());
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
    container.add(textField);
    textField.addDropDownButtonListener(new IDropDownButtonListener() {
      @Override
      public void iconClicked(Object source) {
        getSwingField().requestFocus();
        handleUiPickColor();
      }

      @Override
      public void menuClicked(Object source) {

        handleSwingPopup((JComponent) source);
      }
    });

    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);

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

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconIdFromScout();

  }

  @Override
  public JTextFieldWithDropDownButton getSwingField() {
    return (JTextFieldWithDropDownButton) super.getSwingField();
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

  protected void handleSwingPopup(final JComponent target) {
    final List<IMenu> scoutMenus = getScoutObject().getMenus();
    if (CollectionUtility.hasElements(scoutMenus)) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {

          // <bsh 2010-10-08>
          // The default implemention positions the popup menu on the left side of the
          // "target" component. This is no longer correct in Rayo. So we use the target's
          // width and substract a certain amount.
          int x = 0;
          if (target instanceof JTextFieldWithDropDownButton) {
            JTextFieldWithDropDownButton tf = (JTextFieldWithDropDownButton) target;
            x = tf.getWidth() - tf.getMargin().right;
          }
          Point point = new Point(x, target.getHeight());
          // </bsh>
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), target, point, scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
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
    JTextFieldWithDropDownButton field = getSwingField();
    if (iconId == null) {
      field.setDropDownButtonVisible(false);
    }
    else {
      field.setDropDownButtonVisible(true);
      field.setIconGroup(new IconGroup(getSwingEnvironment(), iconId));
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IColorPickerField.PROP_ICON_ID)) {
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
}
