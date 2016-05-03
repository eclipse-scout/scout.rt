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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.basic.comp.CLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.ILookAndFeelDecorations;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class StatusLabelEx extends Composite implements ILabelComposite {
  private final ISwtEnvironment m_environment;
  private IProcessingStatus m_status;
  private boolean m_mandatory;
  private boolean m_enabled;
  private Control m_label;

  private Label m_statusLabel;
  private final Image m_infoImg;
  private final Image m_warningImg;
  private final Image m_errorImg;

  private String m_preMarker = "";
  private String m_postMarker = "";
  private Font m_nonMandatoryFont;
  protected Font m_mandatoryFont;
  private Color m_nonMandatoryForegroundColor;
  protected Color m_mandatoryForegroundColor;
  private String m_nonMandatoryText = "";

  private Control m_mnemonicFocusControl;

  public StatusLabelEx(Composite parent, int style, ISwtEnvironment environment) {
    super(parent, SWT.NO_FOCUS);
    m_environment = environment;
    m_infoImg = Activator.getIcon(AbstractIcons.StatusInfo);
    m_warningImg = Activator.getIcon(AbstractIcons.StatusWarning);
    m_errorImg = Activator.getIcon(AbstractIcons.StatusError);

    createContent(this, style);
    createLayout();

    m_nonMandatoryFont = m_label.getFont();
    m_nonMandatoryForegroundColor = m_label.getForeground();
    m_enabled = super.getEnabled();
  }

  protected void createLayout() {
    GridLayout containerLayout = new GridLayout(2, false);
    containerLayout.horizontalSpacing = 0;
    containerLayout.marginHeight = 0;
    containerLayout.marginWidth = 0;
    containerLayout.verticalSpacing = 0;
    setLayout(containerLayout);
  }

  protected void createContent(Composite parent, int style) {
    m_label = new CLabelEx(parent, style | m_environment.getFormToolkit().getFormToolkit().getOrientation());
    m_label.addListener(SWT.Traverse, new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (event.widget == m_label && event.detail == SWT.TRAVERSE_MNEMONIC) {
          char mnemonic = findMnemonicCharakter(getText());
          if (mnemonic == '\0') {
            return;
          }
          if (Character.toUpperCase(event.character) != Character.toUpperCase(mnemonic)) {
            return;
          }
          event.doit = false;
          if (getMnemonicFocusControl() != null && !getMnemonicFocusControl().isDisposed()) {
            getMnemonicFocusControl().setFocus();
          }
        }
      }
    });
    m_environment.getFormToolkit().getFormToolkit().adapt(m_label, false, false);

    m_statusLabel = new Label(parent, SWT.NONE);
    m_environment.getFormToolkit().getFormToolkit().adapt(m_statusLabel, false, false);

    GridData data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    m_statusLabel.setLayoutData(data);

    //Make sure the label composite fills the cell so that horizontal alignment of the text works well
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_label.setLayoutData(data);
  }

  /**
   * Reads the mandatory settings if not already read
   */
  protected void initMandatorySettings() {
    if (m_mandatoryFont == null) {
      FontSpec labelFontSpec = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryLabelFont();
      if (labelFontSpec != null) {
        m_mandatoryFont = getEnvironment().getFont(labelFontSpec, getNonMandatoryFont());
      }
    }

    if (m_mandatoryForegroundColor == null) {
      String labelTextColor = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryLabelTextColor();
      if (labelTextColor != null) {
        m_mandatoryForegroundColor = getEnvironment().getColor(labelTextColor);
      }
    }

    if (!StringUtility.hasText(m_postMarker) && !StringUtility.hasText(m_preMarker)) {
      int starPos = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryStarMarkerPosition();
      if (starPos != ILookAndFeelDecorations.STAR_MARKER_NONE) {
        switch (starPos) {
          case ILookAndFeelDecorations.STAR_MARKER_AFTER_LABEL:
            m_postMarker = "*";
            break;
          case ILookAndFeelDecorations.STAR_MARKER_BEFORE_LABEL:
            m_preMarker = "*";
            break;
        }
      }
    }
  }

  protected char findMnemonicCharakter(String string) {
    int index = 0;
    if (StringUtility.isNullOrEmpty(string)) {
      return '\0';
    }
    int length = string.length();
    do {
      while (index < length && string.charAt(index) != '&') {
        index++;
      }
      if (++index >= length) {
        return '\0';
      }
      if (string.charAt(index) != '&') {
        return string.charAt(index);
      }
      index++;
    }
    while (index < length);
    return '\0';
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  @Override
  public Control getMnemonicFocusControl() {
    return m_mnemonicFocusControl;
  }

  @Override
  public void setMnemonicFocusControl(Control mnemonicFocusControl) {
    m_mnemonicFocusControl = mnemonicFocusControl;
  }

  @Override
  public void setLayoutWidthHint(int w) {
    Object o = getLayoutData();
    if (o instanceof LogicalGridData) {
      LogicalGridData data = (LogicalGridData) o;
      data.widthHint = w;
    }
  }

  /**
   * @param b
   * @return if the layout has to be updated up to the top container.
   */
  @Override
  public boolean setMandadatory(boolean mandatory) {
    if (isMandatory() == mandatory) {
      return false;
    }
    m_mandatory = mandatory;

    if (mandatory) {
      initMandatorySettings();
    }

    boolean updateLayout = false;
    if (getMandatoryFont() != null) {
      updateLabelFont();
      updateLayout = true;
    }
    if (getMandatoryForegroundColor() != null) {
      updateLabelForeground();
      updateLayout = true;
    }
    if (getPostMarker() != null || getPreMarker() != null) {
      updateText();
      updateLayout = true;
    }

    return updateLayout;
  }

  @Override
  public void setEnabled(boolean enabled) {
    // only mark/display it disabled otherwise the tooltip caused by shortened label text is only visible in enabled state
    m_enabled = enabled;

    if (enabled) {
      setForeground(null);
    }
    else {
      setForeground(getEnvironment().getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled()));
    }
  }

  @Override
  public boolean getEnabled() {
    return m_enabled;
  }

  protected void updateLabelForeground() {
    //Update the foreground only if the field is enabled otherwise the disabled state would not be visible
    if (isEnabled() && isMandatory()) {
      m_label.setForeground(getMandatoryForegroundColor());
    }
    else {
      m_label.setForeground(getNonMandatoryForegroundColor());
    }
  }

  protected void updateLabelFont() {
    if (isMandatory()) {
      m_label.setFont(getMandatoryFont());
    }
    else {
      m_label.setFont(getNonMandatoryFont());
    }
  }

  protected void updateText() {
    if (isMandatory()) {
      setLabelText(m_preMarker + getNonMandatoryText() + m_postMarker);
    }
    else {
      setLabelText(getNonMandatoryText());
    }
  }

  protected void setLabelText(String text) {
    if (m_label instanceof CLabel) {
      ((CLabel) m_label).setText(text);
    }
  }

  protected String getLabelText() {
    if (m_label instanceof CLabel) {
      return ((CLabel) m_label).getText();
    }

    return null;
  }

  @Override
  public void setStatus(IProcessingStatus status) {
    m_status = status;
    if (m_status == null) {
      getStatusLabel().setToolTipText("");
      getStatusLabel().setImage(null);
      getStatusLabel().setVisible(false);
      if (getStatusLabel().getLayoutData() instanceof GridData) {
        ((GridData) getStatusLabel().getLayoutData()).exclude = true;
      }
    }
    else {
      String iconId = m_status instanceof ScoutFieldStatus ? ((ScoutFieldStatus) m_status).getIconId() : null;
      if (iconId != null) {
        getStatusLabel().setImage(getEnvironment().getIcon(iconId));
      }
      else {
        switch (m_status.getSeverity()) {
          case IProcessingStatus.FATAL:
          case IProcessingStatus.ERROR:
            getStatusLabel().setImage(m_errorImg);
            break;
          case IProcessingStatus.WARNING:
            getStatusLabel().setImage(m_warningImg);
            break;
          default:
            getStatusLabel().setImage(m_infoImg);
            break;
        }
      }
      // tooltip
      StringBuffer buf = new StringBuffer();
      if (m_status.getTitle() != null) {
        buf.append(m_status.getTitle());
      }
      if (m_status.getMessage() != null) {
        if (buf.length() > 0) {
          buf.append("\n");
        }
        buf.append(m_status.getMessage());
      }
      getStatusLabel().setToolTipText(buf.toString());
      getStatusLabel().setVisible(true);
      if (getStatusLabel().getLayoutData() instanceof GridData) {
        ((GridData) getStatusLabel().getLayoutData()).exclude = false;
      }
    }
    layout(true, true);
  }

// delegate methods
  @Override
  public String getText() {
    return getLabelText();
  }

  @Override
  public void setText(String text) {
    if (text == null) {
      text = "";
    }

    m_nonMandatoryText = text;
    updateText();
  }

  @Override
  public Color getBackground() {
    return m_label.getBackground();
  }

  @Override
  public void setBackground(Color color) {
    super.setBackground(color);

    m_label.setBackground(color);
  }

  @Override
  public Color getForeground() {
    return m_label.getForeground();
  }

  @Override
  public void setForeground(Color color) {
    super.setForeground(color);

    m_nonMandatoryForegroundColor = color;
    updateLabelForeground();
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);

    m_nonMandatoryFont = font;
    updateLabelFont();
  }

  @Override
  public Font getFont() {
    return m_label.getFont();
  }

  public Font getNonMandatoryFont() {
    return m_nonMandatoryFont;
  }

  public void setNonMandatoryFont(Font nonMandatoryFont) {
    m_nonMandatoryFont = nonMandatoryFont;
  }

  public Font getMandatoryFont() {
    return m_mandatoryFont;
  }

  public void setMandatoryFont(Font mandatoryFont) {
    m_mandatoryFont = mandatoryFont;
  }

  public Color getNonMandatoryForegroundColor() {
    return m_nonMandatoryForegroundColor;
  }

  public void setNonMandatoryForegroundColor(Color nonMandatoryForegroundColor) {
    m_nonMandatoryForegroundColor = nonMandatoryForegroundColor;
  }

  public Color getMandatoryForegroundColor() {
    return m_mandatoryForegroundColor;
  }

  public void setMandatoryForegroundColor(Color mandatoryForegroundColor) {
    m_mandatoryForegroundColor = mandatoryForegroundColor;
  }

  public String getPreMarker() {
    return m_preMarker;
  }

  public String getPostMarker() {
    return m_postMarker;
  }

  public Label getStatusLabel() {
    return m_statusLabel;
  }

  protected void setStatusLabel(Label statusLabel) {
    m_statusLabel = statusLabel;
  }

  public boolean isMandatory() {
    return m_mandatory;
  }

  public String getNonMandatoryText() {
    return m_nonMandatoryText;
  }

  protected void setLabel(Control label) {
    m_label = label;
  }

  public Control getLabel() {
    return m_label;
  }
}
