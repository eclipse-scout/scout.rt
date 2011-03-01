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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.basic.comp.CLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.ILookAndFeelDecorations;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StatusLabelEx extends Composite implements ILabelComposite {
  private final ISwtEnvironment m_environment;
  private IProcessingStatus m_status;
  private CLabelEx m_label;
  private Label m_statusLabel;
  private Image m_infoImg;
  private Image m_warningImg;
  private Image m_errorImg;
  private String m_preMarker = "";
  private String m_postMarker = "";

  private Font m_swtFont;
  protected Font mandatoryFont;

  private Color m_swtLabelForeground;
  protected Color mandatoryLabelForeground;
  private String m_text;

  public StatusLabelEx(Composite parent, int style, ISwtEnvironment environment) {
    super(parent, SWT.NONE);
    m_environment = environment;
    m_infoImg = environment.getIcon(AbstractIcons.StatusWarning);
    m_warningImg = environment.getIcon(AbstractIcons.StatusWarning);
    m_errorImg = environment.getIcon(AbstractIcons.StatusError);
    createContent(this, style);
    GridLayout containerLayout = new GridLayout(2, false);
    containerLayout.horizontalSpacing = 0;
    containerLayout.marginHeight = 0;
    containerLayout.marginWidth = 0;
    containerLayout.verticalSpacing = 0;
    setLayout(containerLayout);
  }

  protected void createContent(Composite parent, int style) {
    m_label = new CLabelEx(parent, style | m_environment.getFormToolkit().getFormToolkit().getOrientation());
    m_swtFont = m_label.getFont();
    m_swtLabelForeground = m_label.getForeground();
    m_environment.getFormToolkit().getFormToolkit().adapt(m_label, false, false);
    m_statusLabel = new Label(parent, SWT.NONE);
    m_environment.getFormToolkit().getFormToolkit().adapt(m_statusLabel, false, false);
    // layout
    GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    m_label.setLayoutData(data);
    GridData dataStatus = new GridData(GridData.FILL_VERTICAL);
    m_statusLabel.setLayoutData(dataStatus);
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

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
  public boolean setMandadatory(boolean b) {
    boolean updateLayout = false;
    FontSpec labelFontString = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryLabelFont();
    if (labelFontString != null) {
      Font f = null;
      if (b) {
        f = getEnvironment().getFont(labelFontString, m_swtFont);
      }
      else {
        f = null;
      }
      if (mandatoryFont != f) {
        mandatoryFont = f;
        updateFont();
        updateLayout = true;
      }
    }
    String labelTextColor = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryLabelTextColor();
    if (labelTextColor != null) {
      Color c = null;
      if (b) {
        c = getEnvironment().getColor(labelTextColor);
      }
      else {
        c = null;
      }
      if (mandatoryLabelForeground != c) {
        mandatoryLabelForeground = c;
        updateLabelForeground();

      }
    }
    int starPos = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryStarMarkerPosition();
    if (starPos != ILookAndFeelDecorations.STAR_MARKER_NONE) {
      switch (starPos) {
        case ILookAndFeelDecorations.STAR_MARKER_AFTER_LABEL:
          m_postMarker = b ? "*" : "";
          break;
        case ILookAndFeelDecorations.STAR_MARKER_BEFORE_LABEL:
          m_preMarker = b ? "*" : "";
          break;
      }
      updateText();
      layout(true, true);
      updateLayout = true;
    }
    return updateLayout;
  }

  private void updateLabelForeground() {
    if (mandatoryLabelForeground != null) {
      m_label.setForeground(mandatoryLabelForeground);
    }
    else {
      m_label.setForeground(m_swtLabelForeground);
    }
  }

  protected void updateFont() {
    if (mandatoryFont != null) {
      m_label.setFont(mandatoryFont);
    }
    else {
      m_label.setFont(m_swtFont);
    }
  }

  // delegate methods
  @Override
  public Color getBackground() {
    return m_label.getBackground();
  }

  @Override
  public void setBackground(Color color) {
    m_label.setBackground(color);
  }

  @Override
  public Color getForeground() {
    return m_label.getForeground();
  }

  @Override
  public void setForeground(Color color) {
    m_label.setForeground(color);
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
    updateText();
  }

  protected void updateText() {
    String text = m_text;
    if (text == null) {
      text = "";
    }
    m_label.setText(m_preMarker + text + m_postMarker);
  }

  public void setStatus(IProcessingStatus status) {
    m_status = status;
    if (m_status == null) {
      m_statusLabel.setToolTipText("");
      m_statusLabel.setImage(null);
      ((GridData) m_statusLabel.getLayoutData()).exclude = true;
    }
    else {
      String iconId = m_status instanceof ScoutFieldStatus ? ((ScoutFieldStatus) m_status).getIconId() : null;
      if (iconId != null) {
        m_statusLabel.setImage(getEnvironment().getIcon(iconId));
      }
      else {
        switch (m_status.getSeverity()) {
          case IProcessingStatus.FATAL:
          case IProcessingStatus.ERROR:
            m_statusLabel.setImage(m_errorImg);
            break;
          case IProcessingStatus.WARNING:
            m_statusLabel.setImage(m_warningImg);
            break;
          default:
            m_statusLabel.setImage(m_infoImg);
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
      m_statusLabel.setToolTipText(buf.toString());
      ((GridData) m_statusLabel.getLayoutData()).exclude = false;
    }
    layout(true, true);
  }

  @Override
  public void setFont(Font font) {
    m_swtFont = font;
    updateFont();

  }

  @Override
  public Font getFont() {
    return m_label.getFont();
  }

  public String getDisplayText() {
    return m_label.getText();
  }

}
