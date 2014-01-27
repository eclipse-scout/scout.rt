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
package org.eclipse.scout.rt.ui.swing.form.fields.labelfield;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;

import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextPaneEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.text.HTMLStyledTextCreator;
import org.eclipse.scout.rt.ui.swing.text.IStyledTextCreator;

public class SwingScoutLabelField extends SwingScoutValueFieldComposite<ILabelField> implements ISwingScoutLabelField {
  private static final long serialVersionUID = 1L;

  private JPanelEx m_fieldPanel;
  private int m_horizontalAlignment;
  private int m_verticalAlignment;
  private String m_cachedOriginalText;
  private boolean m_textWrap;
  private IStyledTextCreator m_styledTextCreator;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    m_fieldPanel = new JPanelEx(new SingleLayout());
    LogicalGridData fieldData = LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData());
    fieldData.topInset = SwingLayoutUtility.getTextFieldTopInset();

    m_fieldPanel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, fieldData);

    JTextPaneEx labelField = createLabelField();
    labelField.addComponentListener(new P_ResizeListener());
    m_fieldPanel.add(labelField);
    setTopMarginForField();

    container.add(m_fieldPanel);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(labelField);
    // layout
    LogicalGridLayout layout = new LogicalGridLayout(getSwingEnvironment(), 1, 0);
    getSwingContainer().setLayout(layout);
  }

  /**
   * Create and return the LabelField
   */
  protected JTextPaneEx createLabelField() {
    JTextPaneEx labelField = new JTextPaneEx();
    labelField.setContentType("text/html");
    HTMLEditorKit editorKit = createEditorKit();
    labelField.setEditorKit(editorKit);
    labelField.setBorder(null);
    m_styledTextCreator = new HTMLStyledTextCreator();
    return labelField;
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    getSwingLabelField().setEditable(false); //JTextPane is never editable!
  }

  /**
   * Create and return the HTMLEditorKit. Override this method to use a custom HTMLEditorKit
   * 
   * @since 3.10.0-M5
   */
  protected HTMLEditorKit createEditorKit() {
    return new HTMLEditorKit();
  }

  /**
   * Creates a border to have correct alignment for customized look and feel (e.g. Rayo)
   * 
   * @since 3.10.0-M2
   */
  protected void setTopMarginForField() {
    int topMargin = SwingUtility.getTopMarginForField();
    if (topMargin > 0) {
      m_fieldPanel.setBorder(new EmptyBorder(topMargin, 0, 0, 0));
    }
  }

  @Override
  public JTextPaneEx getSwingLabelField() {
    return (JTextPaneEx) getSwingField();
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    super.attachScout();
    ILabelField f = getScoutObject();
    setTextWrapFromScout(f.isWrapText());
  }

  protected void setTextWrapFromScout(boolean b) {
    m_textWrap = b;
    updateText();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    m_cachedOriginalText = s;
    s = createStyledText(s);

    JTextPane swingField = getSwingLabelField();
    String oldText = swingField.getText();
    if (s == null) {
      s = "";
    }
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(s)) {
      return;
    }
    swingField.setText(s);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    this.m_horizontalAlignment = SwingUtility.createHorizontalAlignment(scoutAlign);
    updateText();
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    this.m_verticalAlignment = SwingUtility.createVerticalAlignment(scoutAlign);
    updateText();
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (ILabelField.PROP_WRAP_TEXT.equals(name)) {
      setTextWrapFromScout(getScoutObject().isWrapText());
    }
  }

  /**
   * This method creates an aligned text for the JTextPane.
   */
  protected String createStyledText(String text) {
    m_styledTextCreator.setText(text);
    m_styledTextCreator.setTextWrap(m_textWrap);
    Color bgColor = (Color) ((getSwingLabelField().getBackground() != null) ? getSwingLabelField().getBackground() : UIManager.getLookAndFeelDefaults().get("TextPane.background"));
    m_styledTextCreator.setBackgroundColor(bgColor);
    m_styledTextCreator.setHorizontalAlignment(m_horizontalAlignment);
    m_styledTextCreator.setVerticalAlignment(m_verticalAlignment);
    m_styledTextCreator.setHeight(adjustHeight(getSwingLabelField().getHeight()));
    return m_styledTextCreator.createStyledText();
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    super.setBackgroundFromScout(scoutColor);
    updateText();
  }

  /**
   * Update the text in the GUI
   * 
   * @since 3.10.0-M5
   */
  protected void updateText() {
    setDisplayTextFromScout(m_cachedOriginalText);
  }

  /**
   * This is due to a Swing bug. See the following post:
   * http://stackoverflow.com/questions/21131748/jtextpane-html-renderer-wrong
   */
  protected int adjustHeight(int height) {
    double factor = 0;
    if (height < 10) {
      factor = 0;
    }
    else if (height >= 10 && height < 150) {
      factor = 0.25;
    }
    else if (height >= 150 && height < 250) {
      factor = 0.24;
    }
    else if (height >= 250 && height < 400) {
      factor = 0.235;
    }
    else if (height >= 400 && height < 700) {
      factor = 0.2347;
    }
    else if (height >= 700 && height < 1000) {
      factor = 0.2335;
    }
    else if (height >= 1000) {
      factor = 0.2330;
    }
    return (int) (height - (height * factor));
  }

  private class P_ResizeListener extends ComponentAdapter {
    int oldHeight = 0;

    @Override
    public void componentResized(ComponentEvent e) {
      if (oldHeight == e.getComponent().getHeight()) {
        return;
      }
      oldHeight = e.getComponent().getHeight();
      updateText();
    }
  }
}
