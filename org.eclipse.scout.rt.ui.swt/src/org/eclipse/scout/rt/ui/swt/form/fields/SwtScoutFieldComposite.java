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
package org.eclipse.scout.rt.ui.swt.form.fields;

import java.util.ArrayList;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutFieldComposite</h3> ...
 * 
 * @since 1.0.0 19.05.2008
 */
public abstract class SwtScoutFieldComposite<T extends IFormField> extends SwtScoutComposite<T> implements ISwtScoutFormField<T> {
  private ILabelComposite m_swtLabel;
  private ISwtKeyStroke[] m_keyStrokes;

  private Color m_mandatoryFieldBackgroundColor;
  private OnFieldLabelDecorator m_onFieldLabelDecorator;

  @Override
  public ILabelComposite getSwtLabel() {
    return m_swtLabel;
  }

  protected void setSwtLabel(ILabelComposite label) {
    m_swtLabel = label;
    if (m_swtLabel != null && label.getLayoutData() == null) {
      LogicalGridData statusLabelGridData = null;
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
        statusLabelGridData = LogicalGridDataBuilder.createLabelOnTop(getScoutObject().getGridData());
      }
      else {
        statusLabelGridData = LogicalGridDataBuilder.createLabel(getScoutObject().getGridData());
      }

      m_swtLabel.setLayoutData(statusLabelGridData);
    }
  }

  public Color getMandatoryFieldBackgroundColor() {
    return m_mandatoryFieldBackgroundColor;
  }

  protected void setErrorStatusFromScout(IProcessingStatus s) {
    if (getSwtLabel() != null) {
      getSwtLabel().setStatus(s);
      getSwtContainer().layout(true, true);
    }
  }

  public void setMandatoryFieldBackgroundColor(Color mandatoryFieldBackgroundColor) {
    m_mandatoryFieldBackgroundColor = mandatoryFieldBackgroundColor;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      setBackgroundFromScout(getScoutObject().getBackgroundColor());
      setForegroundFromScout(getScoutObject().getForegroundColor());
      setVisibleFromScout(getScoutObject().isVisible());
      setEnabledFromScout(getScoutObject().isEnabled());
      setMandatoryFromScout(getScoutObject().isMandatory());
      setErrorStatusFromScout(getScoutObject().getErrorStatus());
      setLabelFromScout(getScoutObject().getLabel());
      setLabelVisibleFromScout();
      setLabelPositionFromScout();
      setLabelWidthInPixelFromScout();
      setLabelHorizontalAlignmentFromScout();
      setTooltipTextFromScout(getScoutObject().getTooltipText());
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD && getScoutObject().getLabel() != null && getScoutObject().getTooltipText() == null) {
        setTooltipTextFromScout(getScoutObject().getLabel());
      }
      setFontFromScout(getScoutObject().getFont());
      setSaveNeededFromScout(getScoutObject().isSaveNeeded());
      setFocusableFromScout(getScoutObject().isFocusable());
      updateKeyStrokesFromScout();
    }
  }

  protected void setVisibleFromScout(boolean b) {
    boolean updateLayout = false;
    if (getSwtContainer() != null && getSwtContainer().getVisible() != b) {
      updateLayout = true;
      getSwtContainer().setVisible(b);
    }
    else if (getSwtField() != null && getSwtField().getVisible() != b) {
      updateLayout = true;
      getSwtField().setVisible(b);
    }
    if (updateLayout && isConnectedToScout()) {
      /*
       * workaround for bug 344966 $
       * (http://bugs.eclipse.org/bugs/show_bug.cgi?id=344966)
       * controls with size 0,0 gets removed from the tab-list.
       */
      if (b) {
        Point size = getSwtContainer().getSize();
        if (size.x == 0 && size.y == 0) {
          getSwtContainer().setSize(100, 100);
        }
      }
      SwtLayoutUtility.invalidateLayout(getSwtContainer());
    }
  }

  protected void setEnabledFromScout(boolean b) {
    boolean updateLayout = false;
    Control swtField = getSwtField();
    if (swtField != null) {
      updateLayout = true;
      setFieldEnabled(swtField, b);
      if (b) {
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        setForegroundFromScout(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
      }
    }
    if (getSwtLabel() != null) {
      if (getSwtLabel().getEnabled() != b) {
        updateLayout = true;
        getSwtLabel().setEnabled(b);
      }
    }
    if (updateLayout && isConnectedToScout()) {
      SwtLayoutUtility.invalidateLayout(getSwtContainer());
    }
  }

  /**
   * used to change enabled into read only
   * 
   * @param swtField
   * @param enabled
   */
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    swtField.setEnabled(enabled);
  }

  protected void setMandatoryFromScout(boolean b) {
    String fieldBackgroundColorString = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryFieldBackgroundColor();
    if (fieldBackgroundColorString != null) {
      Color color = null;
      if (b) {
        color = getEnvironment().getColor(fieldBackgroundColorString);
      }
      else {
        color = null;
      }
      if (getMandatoryFieldBackgroundColor() != color) {
        setMandatoryFieldBackgroundColor(color);
        setBackgroundFromScout(getScoutObject().getBackgroundColor());
      }
    }
    if (getSwtLabel() != null) {
      if (getSwtLabel().setMandadatory(b)) {
        if (isConnectedToScout()) {
          SwtLayoutUtility.invalidateLayout(getSwtContainer());
        }
      }
    }
  }

  protected void setLabelPositionFromScout() {
    if (getSwtField() != null) {
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
        if (m_onFieldLabelDecorator == null) {
          m_onFieldLabelDecorator = new OnFieldLabelDecorator(getEnvironment(), getScoutObject().isMandatory());
          m_onFieldLabelDecorator.setText(getScoutObject().getLabel());
        }
        m_onFieldLabelDecorator.attach(getSwtField());
      }
      else {
        if (m_onFieldLabelDecorator != null) {
          m_onFieldLabelDecorator.detach(getSwtField());
        }
      }
      getSwtField().redraw();
    }
    setLabelVisibleFromScout();
    setLabelFromScout(getScoutObject().getLabel());
  }

  protected void setLabelWidthInPixelFromScout() {
    if (getSwtLabel() != null) {
      int w = getScoutObject().getLabelWidthInPixel();
      if (w > 0) {
        getSwtLabel().setLayoutWidthHint(w);
      }
      else if (w == IFormField.LABEL_WIDTH_DEFAULT) {
        getSwtLabel().setLayoutWidthHint(UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelWidth());
      }
      else if (w == IFormField.LABEL_WIDTH_UI) {
        getSwtLabel().setLayoutWidthHint(0);
      }
    }
  }

  protected void setLabelHorizontalAlignmentFromScout() {
    // XXX not supported by swt to dynamically change style of a widget
  }

  protected void setLabelFromScout(String s) {
    if (m_swtLabel != null && s != null) {
      m_swtLabel.setText(s);
    }
    if (m_onFieldLabelDecorator != null) {
      m_onFieldLabelDecorator.setText(s);
    }
  }

  protected void setLabelVisibleFromScout() {
    boolean b = getScoutObject().isLabelVisible() && getScoutObject().getLabelPosition() != IFormField.LABEL_POSITION_ON_FIELD;
    if (m_swtLabel != null && b != m_swtLabel.getVisible()) {
      m_swtLabel.setVisible(b);
      if (getSwtContainer() != null && isConnectedToScout()) {
        getSwtContainer().layout(true, true);
      }
    }
  }

  protected void setTooltipTextFromScout(String s) {
    if (getSwtField() != null) {
      getSwtField().setToolTipText(s);
    }
  }

  protected void setBackgroundFromScout(String scoutColor) {
    if (getSwtField() != null) {
      Control fld = getSwtField();
      if (fld.getData(CLIENT_PROP_INITIAL_BACKGROUND) == null) {
        fld.setData(CLIENT_PROP_INITIAL_BACKGROUND, fld.getBackground());
      }
      Color initCol = (Color) fld.getData(CLIENT_PROP_INITIAL_BACKGROUND);
      Color c = getEnvironment().getColor(scoutColor);
      if (getMandatoryFieldBackgroundColor() != null) {
        c = getMandatoryFieldBackgroundColor();
      }
      if (c == null) {
        c = initCol;
      }
      fld.setBackground(c);
    }
  }

  protected void setForegroundFromScout(String scoutColor) {
    if (getSwtField() != null) {
      Control fld = getSwtField();
      if (fld.getData(CLIENT_PROP_INITIAL_FOREGROUND) == null) {
        fld.setData(CLIENT_PROP_INITIAL_FOREGROUND, fld.getForeground());
      }
      Color initCol = (Color) fld.getData(CLIENT_PROP_INITIAL_FOREGROUND);
      Color c = getEnvironment().getColor(scoutColor);
      if (c == null) {
        c = initCol;
      }
      fld.setForeground(c);
    }
  }

  protected void setFontFromScout(FontSpec scoutFont) {
    if (getSwtField() != null) {
      Control fld = getSwtField();
      Font currentFont = fld.getFont();
      if (fld.getData(CLIENT_PROP_INITIAL_FONT) == null) {
        fld.setData(CLIENT_PROP_INITIAL_FONT, currentFont);
      }
      Font initFont = (Font) fld.getData(CLIENT_PROP_INITIAL_FONT);
      Font f = getEnvironment().getFont(scoutFont, initFont);
      if (f == null) {
        f = initFont;
      }
      if (currentFont == null || !currentFont.equals(f)) {
        // only set the new font if it is different to the current one
        fld.setFont(f);
      }
    }
    if (isConnectedToScout()) {
      SwtLayoutUtility.invalidateLayout(getSwtContainer());
    }
  }

  protected void setSaveNeededFromScout(boolean b) {
  }

  protected void setFocusableFromScout(boolean b) {
  }

  protected void updateEmptyFromScout() {
  }

  protected void updateKeyStrokesFromScout() {
    // key strokes
    Control widget = getSwtContainer();
    if (widget == null) {
      widget = getSwtField();
    }
    if (widget != null) {

      // remove old
      if (m_keyStrokes != null) {
        for (ISwtKeyStroke swtKeyStroke : m_keyStrokes) {
          getEnvironment().removeKeyStroke(getSwtContainer(), swtKeyStroke);
        }
      }

      ArrayList<ISwtKeyStroke> newSwtKeyStrokes = new ArrayList<ISwtKeyStroke>();
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKeyStroke : scoutKeyStrokes) {
        ISwtKeyStroke[] swtStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, getEnvironment());
        for (ISwtKeyStroke swtStroke : swtStrokes) {
          getEnvironment().addKeyStroke(getSwtContainer(), swtStroke);
          newSwtKeyStrokes.add(swtStroke);
        }
      }
      m_keyStrokes = newSwtKeyStrokes.toArray(new ISwtKeyStroke[newSwtKeyStrokes.size()]);
    }
  }

  //runs in scout job
  @Override
  protected boolean isHandleScoutPropertyChange(final String name, final Object newValue) {
    if (name.equals(IFormField.PROP_ENABLED) || name.equals(IFormField.PROP_VISIBLE)) {
      //add immediate change to swt environment to support TAB traversal to component that changes from disabled to enabled.
      getEnvironment().postImmediateSwtJob(new Runnable() {
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
    else if (name.equals(IFormField.PROP_SAVE_NEEDED)) {
      setSaveNeededFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_EMPTY)) {
      updateEmptyFromScout();
    }
    else if (name.equals(IFormField.PROP_KEY_STROKES)) {
      updateKeyStrokesFromScout();
    }
  }

}
