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
package org.eclipse.scout.rt.ui.swing.form.fields.numberfield;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutBasicFieldComposite;

public class SwingScoutNumberField extends SwingScoutBasicFieldComposite<INumberField<?>> implements ISwingScoutNumberField {
  private static final long serialVersionUID = 1L;

  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    textField.setDecorationIcon(m_contextMenuMarker);

    Document doc = textField.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new P_DocumentFilter());
    }
    addInputListenersForBasicField(textField, doc);
    //
    container.add(textField);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected void installContextMenu() {
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);
    m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingTextField(), getScoutObject().getContextMenu(), getSwingEnvironment());
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
  public JTextField getSwingTextField() {
    return (JTextField) getSwingField();
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    JComponent fld = getSwingField();
    if (fld != null && scoutColor != null && fld instanceof JTextComponent) {
      setDisabledTextColor(ColorUtility.createColor(scoutColor), (JTextComponent) fld);
    }
    super.setForegroundFromScout(scoutColor);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingTextField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void setSelectionFromSwing() {
    //Nothing to do: Selection is not stored in model for DecimalField.
  }

  @Override
  protected boolean isSelectAllOnFocusInScout() {
    return true; //No such property in Scout for DecimalField.
  }

  private final class P_DocumentFilter extends DocumentFilter {
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
      Document doc = fb.getDocument();
      if (AbstractNumberField.isWithinNumberFormatLimits(getScoutObject().getFormat(), doc.getText(0, doc.getLength()), offset, length, text)) {
        super.replace(fb, offset, length, text, attrs);
      }
      else {
        if (textWasPasted(text)) {
          try {
            text = AbstractNumberField.createNumberWithinFormatLimits(getScoutObject().getFormat(), doc.getText(0, doc.getLength()), offset, length, text);
            offset = 0;
            length = doc.getLength();
            super.replace(fb, offset, length, text, attrs);
          }
          catch (ProcessingException e) {
            showCouldNotPasteDialog();
          }
        }
      }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
      Document doc = fb.getDocument();
      if (textWasPasted(text)) {
        try {
          text = AbstractNumberField.createNumberWithinFormatLimits(getScoutObject().getFormat(), doc.getText(0, doc.getLength()), offset, 0, text);
          super.insertString(fb, offset, text, attr);
        }
        catch (ProcessingException e) {
          showCouldNotPasteDialog();
        }
      }
      else if (AbstractNumberField.isWithinNumberFormatLimits(getScoutObject().getFormat(), doc.getText(0, doc.getLength()), offset, 0, text)) {
        super.insertString(fb, offset, text, attr);
      }
    }

    private void showCouldNotPasteDialog() {
      SwingUtility.showMessageDialogSynthCapable(SwingUtility.getOwnerForChildWindow(), SwingUtility.getNlsText("PasteTextNotApplicableForNumberField", String.valueOf(getScoutObject().getFormat().getMaximumIntegerDigits())), SwingUtility.getNlsText("Paste"), JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * returns true if the text was pasted.
   */
  private boolean textWasPasted(String text) {
    return StringUtility.length(text) > 1;
  }
}
