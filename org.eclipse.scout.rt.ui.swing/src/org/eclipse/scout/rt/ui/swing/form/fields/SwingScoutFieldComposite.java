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
package org.eclipse.scout.rt.ui.swing.form.fields;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JRootPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextAreaEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;

public abstract class SwingScoutFieldComposite<T extends IFormField> extends SwingScoutComposite<T> implements ISwingScoutFormField<T> {

  public static final String CLIENT_PROP_INITIAL_LABEL_OPAQUE = "scoutInitialLabelOpaque";
  public static final String CLIENT_PROP_INITIAL_LABEL_FONT = "scoutInitialLabelFont";
  public static final String CLIENT_PROP_INITIAL_LABEL_BACKGROUND = "scoutInitialLabelBackground";
  public static final String CLIENT_PROP_INITIAL_LABEL_FOREGROUND = "scoutInitialLabelForeground";

  private JComponent m_swingContainer;
  private JStatusLabelEx m_swingStatusLabel;
  // cache
  private IKeyStroke[] m_installedScoutKs;

  /**
   * @deprecated since 3.8, replaced by {@link FormEvent#TYPE_REQUEST_FOCUS}
   */
  @Deprecated
  public static final String CLIENT_PROP_FOCUSED = "scout.ui.swing.focused";

  public SwingScoutFieldComposite() {
    super();
  }

  @Override
  public JComponent getSwingContainer() {
    return m_swingContainer;
  }

  protected void setSwingContainer(JComponent swingContainer) {
    m_swingContainer = swingContainer;
  }

  @Override
  public JStatusLabelEx getSwingLabel() {
    return m_swingStatusLabel;
  }

  @Override
  protected void cacheSwingClientProperties() {
    super.cacheSwingClientProperties();
    JStatusLabelEx fld = getSwingLabel();
    if (fld != null) {
      // opaque
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_OPAQUE)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_OPAQUE, new Boolean(fld.isOpaque()));
      }
      // background
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_BACKGROUND)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_BACKGROUND, fld.getBackground());
      }
      // foreground
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FOREGROUND)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FOREGROUND, fld.getForeground());
      }
      // font
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FONT)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FONT, fld.getFont());
      }
    }
  }

  protected void setSwingLabel(JStatusLabelEx swingLabel) {
    m_swingStatusLabel = swingLabel;
    if (m_swingStatusLabel != null) {
      LogicalGridData statusLabelGridData = null;
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
        statusLabelGridData = LogicalGridDataBuilder.createLabelOnTop(((IFormField) getScoutObject()).getGridData());
      }
      else {
        statusLabelGridData = LogicalGridDataBuilder.createLabel(getSwingEnvironment(), ((IFormField) getScoutObject()).getGridData());
      }
      m_swingStatusLabel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, statusLabelGridData);
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IFormField scoutField = getScoutObject();
    if (scoutField != null) {
      setVisibleFromScout(scoutField.isVisible());
      setLabelWidthInPixelFromScout();
      setLabelHorizontalAlignmentFromScout();
      setEnabledFromScout(scoutField.isEnabled());
      // bsh 2010-10-01: The "mandatory" state of a SequenceBoxes is always derived from the
      // inner fields. Don't use the model value (it will always be false).
      if (!(scoutField instanceof ISequenceBox)) {
        setMandatoryFromScout(scoutField.isMandatory());
      }
      setErrorStatusFromScout(scoutField.getErrorStatus());
      setLabelFromScout(scoutField.getLabel());
      setLabelVisibleFromScout();
      setTooltipTextFromScout(scoutField.getTooltipText());
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD && scoutField.getLabel() != null && scoutField.getTooltipText() == null) {
        setTooltipTextFromScout(scoutField.getLabel());
      }
      setBackgroundFromScout(scoutField.getBackgroundColor());
      setForegroundFromScout(scoutField.getForegroundColor());
      setFontFromScout(scoutField.getFont());
      setLabelBackgroundFromScout(scoutField.getLabelBackgroundColor());
      setLabelForegroundFromScout(scoutField.getLabelForegroundColor());
      setLabelFontFromScout(scoutField.getLabelFont());
      setSaveNeededFromScout(scoutField.isSaveNeeded());
      setEmptyFromScout(scoutField.isEmpty());
      setFocusableFromScout(scoutField.isFocusable());
      setHorizontalAlignmentFromScout(scoutField.getGridData().horizontalAlignment);
      setVerticalAlignmentFromScout(scoutField.getGridData().verticalAlignment);
      setKeyStrokesFromScout();
    }
  }

  protected void setVisibleFromScout(boolean b) {
    Component changedComponent = null;
    if (m_swingContainer != null) {
      if (m_swingContainer.isVisible() != b) {
        m_swingContainer.setVisible(b);
        changedComponent = m_swingContainer;
      }
    }
    else if (getSwingField() != null) {
      if (getSwingField().isVisible() != b) {
        getSwingField().setVisible(b);
        changedComponent = getSwingField();
      }
    }
    //
    if (changedComponent != null) {
      JRootPaneEx rp = (JRootPaneEx) SwingUtilities.getAncestorOfClass(JRootPaneEx.class, changedComponent);
      if (rp != null) {
        rp.notifyVisibleChanged(changedComponent);
      }
    }
  }

  protected void setEnabledFromScout(boolean b) {
    if (getSwingField() instanceof JTextComponent) {
      /**
       * Workaround: Deep inside swing the text field editor View.class is only
       * checking for isEnable() This is not changeable by a LookAndFeel. This
       * is handled in the corresponding ...Ex sub classes of JTextComponent.
       * Scout supports non-editable text components to behave: background like
       * disabled selectable copy/paste -able scrollable not focusable not
       * mutable
       */
      JTextComponent c = (JTextComponent) getSwingField();
      c.setEditable(b);
    }
    else if (getSwingField() != null) {
      getSwingField().setEnabled(b);
    }
    // label
    if (getSwingLabel() != null) {
      getSwingLabel().setEnabled(b);
    }
  }

  protected void setMandatoryFromScout(boolean b) {
    if (getSwingLabel() != null) {
      getSwingLabel().setMandatory(b);
    }
  }

  protected void setErrorStatusFromScout(IProcessingStatus s) {
    if (getSwingLabel() != null) {
      getSwingLabel().setStatus(s);
    }
  }

  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
  }

  protected void setVerticalAlignmentFromScout(int scoutAlign) {
  }

  protected void setLabelVisibleFromScout() {
    if (m_swingStatusLabel == null) {
      return;
    }

    boolean b = getScoutObject().isLabelVisible();
    if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
      m_swingStatusLabel.setText(null);
      m_swingStatusLabel.setLayoutWidthHint(0);
    }
    m_swingStatusLabel.setVisible(b);
    if (m_swingContainer != null) {
      m_swingContainer.revalidate();
    }
  }

  protected void setLabelFromScout(String s) {
    if (m_swingStatusLabel != null) {
      m_swingStatusLabel.setText(s);
      if (m_swingContainer != null && m_swingStatusLabel.isVisible()) {
        m_swingContainer.revalidate();
      }
    }
    if (getSwingField() instanceof JTextFieldEx) {
      JTextFieldEx tf = (JTextFieldEx) getSwingField();
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
        if (tf.getOnFieldLabelHandler() == null) {
          tf.setOnFieldLabelHandler(getSwingEnvironment().createOnFieldLabelDecorator(tf, getScoutObject().isMandatory()));
        }
        tf.getOnFieldLabelHandler().setLabel(s);
      }
      else {
        tf.setOnFieldLabelHandler(null);
      }
    }
    else if (getSwingField() instanceof JTextAreaEx) {
      JTextAreaEx taf = (JTextAreaEx) getSwingField();
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
        if (taf.getOnFieldLabelHandler() == null) {
          taf.setOnFieldLabelHandler(getSwingEnvironment().createOnFieldLabelDecorator(taf, getScoutObject().isMandatory()));
        }
        taf.getOnFieldLabelHandler().setLabel(s);
      }
      else {
        taf.setOnFieldLabelHandler(null);
      }
    }
  }

  protected void setLabelWidthInPixelFromScout() {
    if (getSwingLabel() != null) {
      int w = getScoutObject().getLabelWidthInPixel();
      if (w > 0) {
        getSwingLabel().setLayoutWidthHint(w);
      }
      else if (w == IFormField.LABEL_WIDTH_DEFAULT) {
        getSwingLabel().setLayoutWidthHint(getSwingEnvironment().getFieldLabelWidth());
      }
      else if (w == IFormField.LABEL_WIDTH_UI) {
        getSwingLabel().setLayoutWidthHint(0);
      }
    }
  }

  protected void setLabelHorizontalAlignmentFromScout() {
    if (getSwingLabel() != null) {
      int swingAlign = SwingUtility.createHorizontalAlignment(getScoutObject().getLabelHorizontalAlignment());
      getSwingLabel().setLayoutHorizontalAlignment(swingAlign);
    }
  }

  protected void setTooltipTextFromScout(String s) {
    s = SwingUtility.createHtmlLabelText(s, true);
    if (getSwingField() != null) {
      getSwingField().setToolTipText(s);
    }
    /**
     * not activated, label tooltip is used to display label when it is not
     * fully rendered
     */
    /*
     * if(m_swingLabel!=null){ m_swingLabel.setToolTipText(s); }
     */
    /**
     * not activated, container tooltip is irritating for some users
     */
    /*
     * if(m_swingContainer!=null){ m_swingContainer.setToolTipText(s); }
     */
  }

  protected void setBackgroundFromScout(String scoutColor) {
    JComponent fld = getSwingField();
    if (fld != null) {
      Color initCol = (Color) getClientProperty(fld, CLIENT_PROP_INITIAL_BACKGROUND);
      boolean initOpaque = ((Boolean) getClientProperty(fld, CLIENT_PROP_INITIAL_OPAQUE)).booleanValue();
      Color c = SwingUtility.createColor(scoutColor);
      boolean opaque = (c != null ? true : initOpaque);
      if (c == null) {
        c = initCol;
      }
      fld.setOpaque(opaque);
      fld.setBackground(c);
    }
  }

  protected void setForegroundFromScout(String scoutColor) {
    JComponent fld = getSwingField();
    if (fld != null) {
      Color initCol = (Color) getClientProperty(fld, CLIENT_PROP_INITIAL_FOREGROUND);
      Color c = SwingUtility.createColor(scoutColor);
      if (c == null) {
        c = initCol;
      }
      fld.setForeground(c);
    }
  }

  protected void setFontFromScout(FontSpec scoutFont) {
    JComponent fld = getSwingField();
    if (fld != null) {
      Font initFont = (Font) getClientProperty(fld, CLIENT_PROP_INITIAL_FONT);
      Font f = SwingUtility.createFont(scoutFont, initFont);
      if (f == null) {
        f = initFont;
      }
      fld.setFont(f);
    }
  }

  protected void setLabelBackgroundFromScout(String scoutColor) {
    JComponent fld = getSwingLabel();
    if (fld != null) {
      Color initCol = (Color) getClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_BACKGROUND);
      boolean initOpaque = ((Boolean) getClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_OPAQUE)).booleanValue();
      Color c = SwingUtility.createColor(scoutColor);
      boolean opaque = (c != null ? true : initOpaque);
      if (c == null) {
        c = initCol;
      }
      fld.setOpaque(opaque);
      fld.setBackground(c);
    }
  }

  protected void setLabelForegroundFromScout(String scoutColor) {
    JComponent fld = getSwingLabel();
    if (fld != null) {
      Color initCol = (Color) getClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FOREGROUND);
      Color c = SwingUtility.createColor(scoutColor);
      if (c == null) {
        c = initCol;
      }
      fld.setForeground(c);
    }
  }

  protected void setLabelFontFromScout(FontSpec scoutFont) {
    JComponent fld = getSwingLabel();
    if (fld != null) {
      Font initFont = (Font) getClientProperty(fld, CLIENT_PROP_INITIAL_LABEL_FONT);
      Font f = SwingUtility.createFont(scoutFont, initFont);
      if (f == null) {
        f = initFont;
      }
      fld.setFont(f);
    }
  }

  protected void setSaveNeededFromScout(boolean b) {
  }

  protected void setEmptyFromScout(boolean b) {
  }

  protected void setFocusableFromScout(boolean b) {
    if (getSwingField() != null) {
      getSwingField().setFocusable(b);
    }
  }

  protected void setKeyStrokesFromScout() {
    JComponent component = getSwingContainer();
    if (component == null) {
      component = getSwingField();
    }
    if (component != null) {
      // remove old key strokes
      if (m_installedScoutKs != null) {
        for (int i = 0; i < m_installedScoutKs.length; i++) {
          IKeyStroke scoutKs = m_installedScoutKs[i];
          KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
          //
          InputMap imap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          imap.remove(swingKs);
          ActionMap amap = component.getActionMap();
          amap.remove(scoutKs.getActionId());
        }
      }
      m_installedScoutKs = null;
      // add new key strokes
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKs : scoutKeyStrokes) {
        int swingWhen = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
        SwingScoutAction<IAction> action = new SwingScoutAction<IAction>();
        action.createField(scoutKs, getSwingEnvironment());
        //
        InputMap imap = component.getInputMap(swingWhen);
        imap.put(swingKs, scoutKs.getActionId());
        ActionMap amap = component.getActionMap();
        amap.put(scoutKs.getActionId(), action.getSwingAction());
      }
      m_installedScoutKs = scoutKeyStrokes;
    }
  }

  //runs in scout job
  @Override
  protected boolean isHandleScoutPropertyChange(final String name, final Object newValue) {
    if (name.equals(IFormField.PROP_ENABLED) || name.equals(IFormField.PROP_VISIBLE)) {
      //add immediate change to swing environment to support TAB traversal to component that changes from disabled to enabled.
      getSwingEnvironment().postImmediateSwingJob(new Runnable() {
        @Override
        public void run() {
          handleScoutPropertyChange(name, newValue);
        }
      });
    }
    return super.isHandleScoutPropertyChange(name, newValue);
  }

  //runs in gui thread
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFormField.PROP_ENABLED)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_FOCUSABLE)) {
      setFocusableFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      setLabelFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_LABEL_VISIBLE)) {
      setLabelVisibleFromScout();
    }
    else if (name.equals(IFormField.PROP_LABEL_VISIBLE)) {
      setLabelVisibleFromScout();
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_VISIBLE)) {
      setVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_MANDATORY)) {
      setMandatoryFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_ERROR_STATUS)) {
      setErrorStatusFromScout((IProcessingStatus) newValue);
    }
    else if (name.equals(IFormField.PROP_FOREGROUND_COLOR)) {
      setForegroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_BACKGROUND_COLOR)) {
      setBackgroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_FONT)) {
      setFontFromScout((FontSpec) newValue);
    }
    else if (name.equals(IFormField.PROP_LABEL_FOREGROUND_COLOR)) {
      setLabelForegroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_LABEL_BACKGROUND_COLOR)) {
      setLabelBackgroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_LABEL_FONT)) {
      setLabelFontFromScout((FontSpec) newValue);
    }
    else if (name.equals(IFormField.PROP_SAVE_NEEDED)) {
      setSaveNeededFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_EMPTY)) {
      setEmptyFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_KEY_STROKES)) {
      setKeyStrokesFromScout();
    }
  }

}
