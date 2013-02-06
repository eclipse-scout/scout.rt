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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Special label that usually is attached to a form field.
 * <p>
 * This implementation offers different ways to indicate, that the form field is mandatory. The default implementation
 * of setMandatory() just sets a bold font to the label. For Rayo, the method showMandatoryIcon() can be used instead,
 * to show a special mandatory marker to the right of the icon.
 * <p>
 * The element consists of two panels that are layed out using a BorderLayout. The left panel (labelPanel) is in the
 * center and is stretched as far as possible. The right panel (iconPanel) is in the EAST region. If no icons are
 * displayed, this panel is not visible at all.
 * <p>
 * The iconPanel contains to icon labels to indicate the status (error, warning, info) and the mandatory flag.
 * 
 * @author bsh
 */
public class JStatusLabelEx extends JComponent {
  private static final long serialVersionUID = 1L;

//  private final ISwingEnvironment m_env;
  private IProcessingStatus m_status;
  private boolean m_mandatoryLabelVisible; // contains actual state, needed for error status overlay (bsh 2010-10-08)

  private JPanelEx m_labelPanel;
  private JLabelEx m_label;
  private JLabelEx m_mandatoryLabel;
  private JPanelEx m_iconPanel;
  private JLabelEx m_statusLabel;

  private Icon m_mandatoryIconEnabled;
  private Icon m_mandatoryIconDisabled;

  private boolean statusHidesMandatoryIconEnabled;

  public JStatusLabelEx() {
    setLayout(new BorderLayoutEx(0, 0));
    m_mandatoryIconEnabled = Activator.getIcon(SwingIcons.Mandantory);
    m_mandatoryIconDisabled = Activator.getIcon(SwingIcons.MandantoryDisabled);

    createPanels();

    // Add labels to panels (using the FlowLayoutEx)
    m_label = new JLabelEx();
    if (m_label.getBorder() == null) {
      m_label.setBorder(new EmptyBorder(0, 0, 0, 3));
    }
    m_label.setHorizontalTextPosition(SwingConstants.LEADING);
    m_label.setHorizontalAlignment(SwingConstants.RIGHT);
    m_label.setVerticalAlignment(SwingConstants.CENTER);
    m_label.setVisible(false);
    getLabelPanel().add(m_label);
    //
    m_statusLabel = new JLabelEx();
    m_statusLabel.setName("Synth.StatusLabelIcon");
    m_statusLabel.setVisible(false);
    getIconPanel().add(m_statusLabel);
    //
    m_mandatoryLabel = new JLabelEx();
    m_mandatoryLabel.setIcon(m_mandatoryIconEnabled);
    m_mandatoryLabel.setName("Synth.StatusLabelIcon");
    m_mandatoryLabel.setVisible(false);
    m_mandatoryLabelVisible = false;
    setStatusHidesMandatoryIconEnabled(true);
    getMandatoryIconPanel().add(m_mandatoryLabel);
  }

  /**
   * Creates a panel for the label and one for the statusLabel (icon).
   */
  protected void createPanels() {
    m_labelPanel = new JPanelEx();
    m_labelPanel.setLayout(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.RIGHT, 0, 0));
    add(m_labelPanel, BorderLayout.CENTER);

    m_iconPanel = new JPanelEx();
    m_iconPanel.setLayout(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.RIGHT, 0, 0));
    add(m_iconPanel, BorderLayout.EAST);
  }

  public void setMandatory(boolean b) {
    m_label.setBold(b);
  }

  @Override
  public void setForeground(Color fg) {
    super.setForeground(fg);

    m_label.setForeground(fg);
  }

  @Override
  public void setBackground(Color bg) {
    super.setBackground(bg);

    m_label.setBackground(bg);
    m_label.setOpaque(bg != null);
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);

    m_label.setFont(font);
  }

  @Override
  public Font getFont() {
    return m_label.getFont();
  }

  public void showMandatoryIcon(boolean b) {
    m_mandatoryLabelVisible = b;
    if (isStatusHidesMandatoryIconEnabled() && m_status != null) {
      // Do not actually show the label, the error status always "wins" (bsh 2010-10-08)
      return;
    }
    m_mandatoryLabel.setVisible(b);
  }

  @Override
  public void setName(String name) {
    m_label.setName(name);
  }

  /**
   * makes the label fixed sized width
   */
  public void setFixedSize(int w) {
    LogicalGridData data = (LogicalGridData) getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    if (data != null) {
      if (w > 0) {
        data.widthHint = w;
      }
      else {
        data.widthHint = 0;
      }
    }
  }

  public void setLayoutWidthHint(int w) {
    LogicalGridData data = (LogicalGridData) getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    if (data != null) {
      data.widthHint = w;
    }
  }

  /**
   * @param One
   *          of SwingConstants.LEFT, RIGHT or CENTER
   */
  public void setLayoutHorizontalAlignment(int alignment) {
    FlowLayoutEx layout = (FlowLayoutEx) m_labelPanel.getLayout();
    if (layout != null) {
      layout.setAlignment(alignment);
    }
  }

  public String getText() {
    return m_label.getText();
  }

  public void setText(String text) {
    m_label.setText(text);
    m_label.setVisible(StringUtility.hasText(text)); // Hide empty labels (so the spacing is not too big within SequenceBoxes)
  }

  public void setStatus(IProcessingStatus status) {
    m_status = status;
    if (m_status == null) {
      m_statusLabel.setVisible(false);
      m_statusLabel.setIcon(null);
      m_statusLabel.setToolTipText(null);
      m_mandatoryLabel.setVisible(m_mandatoryLabelVisible);
    }
    else {
      // icon
      String iconId = (m_status instanceof ScoutFieldStatus ? ((ScoutFieldStatus) m_status).getIconId() : null);
      if (iconId == null) {
        switch (m_status.getSeverity()) {
          case IProcessingStatus.FATAL:
          case IProcessingStatus.ERROR:
            iconId = AbstractIcons.StatusError;
            break;
          case IProcessingStatus.WARNING:
            iconId = AbstractIcons.StatusWarning;
            break;
          default:
            iconId = AbstractIcons.StatusInfo;
            break;
        }
      }
      m_statusLabel.setIcon(Activator.getIcon(iconId));
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
      // visibility
      m_statusLabel.setVisible(true);

      if (isStatusHidesMandatoryIconEnabled()) {
        m_mandatoryLabel.setVisible(false);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_label.setEnabled(enabled);
    m_mandatoryLabel.setIcon(enabled ? m_mandatoryIconEnabled : m_mandatoryIconDisabled);
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }

  protected void setIconPanel(JPanelEx iconPanel) {
    m_iconPanel = iconPanel;
  }

  protected JPanelEx getIconPanel() {
    return m_iconPanel;
  }

  protected void setLabelPanel(JPanelEx labelPanel) {
    m_labelPanel = labelPanel;
  }

  protected JPanelEx getLabelPanel() {
    return m_labelPanel;
  }

  protected JPanelEx getMandatoryIconPanel() {
    //Use the same panel for status icons and mandatory icons
    return m_iconPanel;
  }

  protected JLabelEx getMandatoryLabel() {
    return m_mandatoryLabel;
  }

  public boolean isStatusHidesMandatoryIconEnabled() {
    return statusHidesMandatoryIconEnabled;
  }

  public void setStatusHidesMandatoryIconEnabled(boolean statusHidesMandatoryIconEnabled) {
    this.statusHidesMandatoryIconEnabled = statusHidesMandatoryIconEnabled;
  }

}
