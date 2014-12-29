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

import javax.swing.SizeRequirements;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Highlighter;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
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
  private String m_unformattedText;
  private boolean m_textWrap;
  private IStyledTextCreator m_styledTextCreator;
  private Color m_foregroundColor;
  private Color m_backgroundColor;
  private TransferHandler m_transferHandler;
  private Highlighter m_highlighter;

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
    m_transferHandler = labelField.getTransferHandler();
    m_highlighter = labelField.getHighlighter();

    labelField.addComponentListener(new P_ResizeListener());
    m_fieldPanel.add(labelField);
    setTopMarginForField();
    m_styledTextCreator = createStyledTextCreator();

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
    return labelField;
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    getSwingLabelField().setEditable(false); //JTextPane is never editable!
    getSwingLabelField().setEnabled(b);
    updateTextInGUI();
  }

  /**
   * Create and return the HTMLEditorKit. Override this method to use a custom HTMLEditorKit
   *
   * @since 3.10.0-M5
   */
  protected HTMLEditorKit createEditorKit() {
    return new HTMLEditorKit() {
      private static final long serialVersionUID = 1L;

      @Override
      public ViewFactory getViewFactory() {

        return new HTMLFactory() {
          @Override
          public View create(Element element) {
            View v = super.create(element);
            if (v instanceof InlineView) {
              // javax.swing.text.html.BRView (the <br> tag) is also a LabelView but
              // our overridden class must not change it's behavior
              Object o = element.getAttributes().getAttribute(StyleConstants.NameAttribute);
              if ((o instanceof HTML.Tag) && o == HTML.Tag.BR) {
                return v;
              }
              return new InlineView(element) {
                @Override
                public int getBreakWeight(int axis, float pos, float len) {
                  // default behavior if no text wrap required
                  if (!m_textWrap) {
                    return super.getBreakWeight(axis, pos, len);
                  }
                  // --> GlyphView.getBreakWeight()
                  if (axis == View.X_AXIS) {
                    checkPainter();
                    int offset = getStartOffset();
                    int maxPos = getGlyphPainter().getBoundedPosition(this, offset, pos, len);
                    if (maxPos == offset) {
                      // can't even fit a single character
                      return View.BadBreakWeight;
                    }
                    // <-- GlyphView.getBreakWeight()
                    // if line break found enforce break
                    try {
                      int separatorIndex = getDocument().getText(offset, maxPos - offset).indexOf(System.getProperty("line.separator"));
                      if (separatorIndex >= 0) {
                        return View.ForcedBreakWeight;
                      }
                    }
                    catch (BadLocationException ex) {
                      //nop
                    }
                  }
                  return super.getBreakWeight(axis, pos, len);
                }

                @Override
                public View breakView(int axis, int offset, float pos, float len) {
                  // default behavior if no text wrap required
                  if (!m_textWrap) {
                    return super.breakView(axis, offset, pos, len);
                  }
                  // --> GlyphView.breakView()
                  if (axis == View.X_AXIS) {
                    checkPainter();
                    int maxPos = getGlyphPainter().getBoundedPosition(this, offset, pos, len);
                    // <-- GlyphView.breakView()
                    // if line break found create new text fragment to display
                    try {
                      int separatorIndex = getDocument().getText(offset, maxPos - offset).indexOf(System.getProperty("line.separator"));
                      if (separatorIndex >= 0) {
                        GlyphView glyphView = (GlyphView) createFragment(offset, offset + separatorIndex + 1);
                        return glyphView;
                      }
                    }
                    catch (BadLocationException ex) {
                      //nop
                    }
                  }
                  return super.breakView(axis, offset, pos, len);
                }
              };
            }
            else if (v instanceof ParagraphView) {
              return new ParagraphView(element) {
                @Override
                protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
                  // default behavior if no text wrap required
                  if (!m_textWrap) {
                    return super.calculateMinorAxisRequirements(axis, r);
                  }
                  // enforce behavior as defined by FlowView
                  // --> FlowView.calculateMinorAxisRequirements()
                  if (r == null) {
                    r = new SizeRequirements();
                  }
                  float pref = layoutPool.getPreferredSpan(axis);
                  float min = layoutPool.getMinimumSpan(axis);
                  // Don't include insets, Box.getXXXSpan will include them.
                  r.minimum = (int) min;
                  r.preferred = Math.max(r.minimum, (int) pref);
                  r.maximum = Integer.MAX_VALUE;
                  r.alignment = 0.5f;
                  return r;
                  // <-- FlowView.calculateMinorAxisRequirements()
                }
              };
            }
            return v;
          }
        };
      }
    };
  }

  /**
   * Create and return the {@link IStyledTextCreator}.
   */
  protected IStyledTextCreator createStyledTextCreator() {
    return new HTMLStyledTextCreator();
  }

  public IStyledTextCreator getStyledTextCreator() {
    return m_styledTextCreator;
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
    setSelectableFromScout(f.isSelectable());
  }

  protected void setTextWrapFromScout(boolean b) {
    m_textWrap = b;
    updateTextInGUI();
  }

  /**
   * Defines if the label should be selectable or not
   *
   * @since 3.10.0-M6
   */
  protected void setSelectableFromScout(boolean b) {
    if (b) {
      getSwingLabelField().setHighlighter(m_highlighter);
      getSwingLabelField().setTransferHandler(m_transferHandler);
    }
    else {
      getSwingLabelField().setHighlighter(null);
      getSwingLabelField().setTransferHandler(null);
    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    m_unformattedText = s;
    updateTextInGUI();
  }

  public String getUnformattedText() {
    return m_unformattedText;
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    this.m_horizontalAlignment = SwingUtility.createHorizontalAlignment(scoutAlign);
    updateTextInGUI();
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    this.m_verticalAlignment = SwingUtility.createVerticalAlignment(scoutAlign);
    updateTextInGUI();
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
    else if (ILabelField.PROP_SELECTABLE.equals(name)) {
      setSelectableFromScout(getScoutObject().isSelectable());
    }
  }

  /**
   * This method creates an aligned text for the JTextPane.
   */
  protected String createStyledText() {
    Color bgColor = (Color) ((m_backgroundColor != null) ? m_backgroundColor : UIManager.getLookAndFeelDefaults().get("TextPane.background"));
    Color fgColor = getScoutObject().isEnabled() ? m_foregroundColor : getSwingLabelField().getDisabledTextColor();

    getStyledTextCreator().setText(getUnformattedText())
    .setTextWrap(m_textWrap)
    .setForegroundColor(fgColor)
    .setBackgroundColor(bgColor)
    .setHorizontalAlignment(m_horizontalAlignment)
    .setVerticalAlignment(m_verticalAlignment)
    .setHeight(adjustHeight(getSwingLabelField().getHeight()));

    return getStyledTextCreator().createStyledText();
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    super.setBackgroundFromScout(scoutColor);
    m_backgroundColor = ColorUtility.createColor(scoutColor);
    updateTextInGUI();
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    super.setForegroundFromScout(scoutColor);
    m_foregroundColor = ColorUtility.createColor(scoutColor);
    updateTextInGUI();
  }

  /**
   * Update the text in the GUI
   *
   * @since 3.10.0-M5
   */
  protected void updateTextInGUI() {
    String styledText = createStyledText();
    if (hasTextChanged(styledText)) {
      getSwingLabelField().setText(styledText);
    }
  }

  private boolean hasTextChanged(String newText) {
    String oldText = getSwingLabelField().getText();
    return !StringUtility.nvl(oldText, "").equals(StringUtility.nvl(newText, ""));
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
    int m_oldHeight = 0;

    @Override
    public void componentResized(ComponentEvent e) {
      if (m_oldHeight == e.getComponent().getHeight()) {
        return;
      }
      m_oldHeight = e.getComponent().getHeight();
      updateTextInGUI();
    }
  }
}
