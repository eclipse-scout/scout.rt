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
package org.eclipse.scout.rt.ui.swing.form.fields.groupbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JSection;
import org.eclipse.scout.rt.ui.swing.ext.internal.LogicalGridLayoutSpyAction;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldGridData;

/**
 * A group box is a composite of the following structure: groupBox bodyPart
 * processButtonPart systemProcessButtonPart customProcessButtonPart
 */
public class SwingScoutGroupBox extends SwingScoutFieldComposite<IGroupBox> implements ISwingScoutGroupBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutGroupBox.class);

  private JPanel m_swingBodyPart;
  private JPanel m_swingButtonBarPart;
  // cache
  protected String m_containerLabel;
  protected String m_containerImage;
  protected int m_containerImageHAlign = SwingConstants.LEFT;
  protected int m_containerImageVAlign = SwingConstants.TOP;
  protected boolean m_containerBorderInstalled;
  protected boolean m_containerBorderVisible;
  protected String m_containerBorderDecoration;

  @Override
  protected void initializeSwing() {
    m_swingBodyPart = new JPanelEx();
    m_swingBodyPart.setName("Synth.GroupBoxBody");
    m_swingBodyPart.setOpaque(false);
    m_swingBodyPart.putClientProperty(LogicalGridLayoutSpyAction.GROUP_BOX_MARKER, Boolean.TRUE);
    m_swingButtonBarPart = createButtonBarPart();
    // main panel: NORTH=sectionHeader, CENTER=bodyPanel, SOUTH=buttonPanel
    JPanelEx swingBox = new JPanelEx();
    swingBox.setOpaque(false);
    swingBox.setLayout(new BorderLayoutEx(0, 0));
    //
    if (getScoutObject().isScrollable()) {
      JScrollPane scrollPane = new JScrollPaneEx(m_swingBodyPart);
      scrollPane.getVerticalScrollBar().setUnitIncrement(16);
      scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
      scrollPane.setBorder(null);
      swingBox.add(scrollPane, BorderLayoutEx.CENTER);
    }
    else {
      swingBox.add(m_swingBodyPart, BorderLayoutEx.CENTER);
    }
    swingBox.add(m_swingButtonBarPart, BorderLayoutEx.SOUTH);
    interceptBorderStyle(getScoutObject());
    // section?
    if (isSection()) {
      JSection section = new JSection(swingBox);
      section.setExpandable(getScoutObject().isExpandable());
      setSwingField(section);
      setSwingLabel(null);
      setSwingContainer(section);
    }
    else {
      setSwingField(swingBox);
      setSwingLabel(null);
      setSwingContainer(swingBox);
    }
    // FIELDS: add layout here and then add fields with constraints (no process buttons)
    LogicalGridLayout bodyLayout = new LogicalGridLayout(getSwingEnvironment(), getSwingEnvironment().getFormColumnGap(), getSwingEnvironment().getFormRowGap());
    m_swingBodyPart.setLayout(bodyLayout);
    // items without process buttons
    for (IFormField field : getScoutObject().getControlFields()) {
      // create item
      ISwingScoutFormField swingScoutComposite = getSwingEnvironment().createFormField(m_swingBodyPart, field);
      // create layout constraints
      SwingScoutFormFieldGridData cons = new SwingScoutFormFieldGridData(field);
      swingScoutComposite.getSwingContainer().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, cons);
      m_swingBodyPart.add(swingScoutComposite.getSwingContainer());
    }

  }

  protected boolean isSection() {
    return m_containerBorderVisible && IGroupBox.BORDER_DECORATION_SECTION.equals(m_containerBorderDecoration);
  }

  protected JPanel createButtonBarPart() {
    SwingScoutGroupBoxButtonbar swingScoutGroupBoxButtonbar = new SwingScoutGroupBoxButtonbar();
    swingScoutGroupBoxButtonbar.createField(getScoutObject(), getSwingEnvironment());
    return swingScoutGroupBoxButtonbar.getSwingContainer();
  }

  @Override
  public JPanel getSwingGroupBox() {
    return (JPanel) getSwingField();
  }

  @Override
  public JPanel getSwingBodyPart() {
    return m_swingBodyPart;
  }

  @Override
  public JPanel getSwingButtonBarPart() {
    return m_swingButtonBarPart;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IGroupBox scoutGroupBox = getScoutObject();
    setBackgroundImageFromScout(scoutGroupBox.getBackgroundImageName());
    setBackgroundImageHorizontalAlignFromScout(scoutGroupBox.getBackgroundImageHorizontalAlignment());
    setBackgroundImageVerticalAlignFromScout(scoutGroupBox.getBackgroundImageVerticalAlignment());
    setExpandedFromScout();
    // ensure foreground color
    setEnabledFromScout(scoutGroupBox.isEnabled());
    changeContainerLabel();
    installSwingContainerBorder();
  }

  /**
   * scout settings
   */

  @Override
  protected void setEnabledFromScout(boolean b) {
    if (b) {
      getSwingGroupBox().setForeground(null);
    }
    else {
      Color color = UIManager.getColor("textInactiveText");
      getSwingGroupBox().setForeground(color);
    }
  }

  @Override
  protected void setLabelVisibleFromScout() {
    super.setLabelVisibleFromScout();
    changeContainerLabel();
  }

  // override to set outer border to line border
  @Override
  protected void setLabelFromScout(String s) {
    super.setLabelFromScout(s);
    changeContainerLabel();
  }

  protected void setExpandedFromScout() {
    if (getSwingContainer() instanceof JSection) {
      JSection section = (JSection) getSwingContainer();
      section.setExpanded(getScoutObject().isExpanded());
    }
  }

  /**
   * set the values {@link #m_containerBorderVisible} and {@link #m_containerBorderDecoration}
   */
  protected void interceptBorderStyle(IGroupBox box) {
    m_containerBorderVisible = box.isBorderVisible();
    m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_EMPTY;
    if (m_containerBorderVisible) {
      if (IGroupBox.BORDER_DECORATION_SECTION.equals(box.getBorderDecoration())) {
        m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_SECTION;
      }
      else if (IGroupBox.BORDER_DECORATION_LINE.equals(box.getBorderDecoration())) {
        m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_LINE;
      }
      else if (IGroupBox.BORDER_DECORATION_AUTO.equals(box.getBorderDecoration())) {
        //auto default cases
        if (box.isMainBox()) {
          if (SwingUtility.isSynth()) {
            m_containerBorderVisible = false;
          }
          m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_EMPTY;
        }
        else if (box.isExpandable()) {
          // best guess
          m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_SECTION;
        }
        else if (box.getParentField() instanceof ITabBox) {
          m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_EMPTY;
        }
        else {
          m_containerBorderDecoration = IGroupBox.BORDER_DECORATION_LINE;
        }
      }
    }
  }

  protected void setBackgroundImageFromScout(String imageName) {
    if (imageName == m_containerImage || (imageName != null && imageName.equals(m_containerImage))) {
      // nop
    }
    else {
      m_containerImage = imageName;
      if (m_containerBorderInstalled) {
        installSwingContainerBorder();
      }
    }
  }

  protected void setBackgroundImageHorizontalAlignFromScout(int halign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(halign);
    if (swingAlign != m_containerImageHAlign) {
      m_containerImageHAlign = swingAlign;
      if (m_containerBorderInstalled) {
        installSwingContainerBorder();
      }
    }
  }

  protected void setBackgroundImageVerticalAlignFromScout(int valign) {
    int swingAlign = SwingUtility.createVerticalAlignment(valign);
    if (swingAlign != m_containerImageVAlign) {
      m_containerImageVAlign = swingAlign;
      if (m_containerBorderInstalled) {
        installSwingContainerBorder();
      }
    }
  }

  protected void changeContainerLabel() {
    String s = getScoutObject().isLabelVisible() ? getScoutObject().getLabel() : null;
    if (s == null) {
      s = "";
    }
    if (!s.equals(m_containerLabel)) {
      m_containerLabel = s;
      if (m_containerBorderInstalled) {
        installSwingContainerBorder();
      }
    }
  }

  protected void installSwingContainerBorder() {
    m_containerBorderInstalled = true;
    if (m_containerBorderVisible) {
      Border border = UIManager.getBorder("GroupBox.border");
      if (border == null) {
        border = new EmptyBorder(0, 0, 0, 0);
      }
      if (getScoutObject().isMainBox()) {
        Insets insets = border.getBorderInsets(null);
        insets.left += 6;
        insets.right += 6;
        border = new EmptyBorder(insets);
      }
      if (IGroupBox.BORDER_DECORATION_SECTION.equals(m_containerBorderDecoration)) {
        // section
        JSection section = (JSection) getSwingContainer();
        section.setText(m_containerLabel);
        section.addPropertyChangeListener("expanded", new P_ExpansionListener());
        section.getContentPane().setBorder(border);
      }
      else if (IGroupBox.BORDER_DECORATION_LINE.equals(m_containerBorderDecoration)) {
        Insets insets = new Insets(26, border.getBorderInsets(null).left, border.getBorderInsets(null).bottom, border.getBorderInsets(null).right);
        insets.bottom += 6;
        insets.right += 6;
        border = new TitledGroupBorder(m_containerLabel != null ? m_containerLabel : "", insets);
        Border bgBorder;
        if (m_containerImage != null) {
          Icon icon = getSwingEnvironment().getIcon(m_containerImage);
          if (icon != null) {
            // set minimum container size
            getSwingContainer().setMinimumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
          }
          else {
            // reset minimum container size
            getSwingContainer().setMinimumSize(null);
          }
          bgBorder = new BackgroundBorder(icon, m_containerImageHAlign, m_containerImageVAlign);
        }
        else {
          bgBorder = new EmptyBorder(0, 0, 0, 0);
        }
        getSwingContainer().setBorder(new CompoundBorder(border, bgBorder));
      }
      else {
        // none
        getSwingContainer().setBorder(border);
      }
    }
    else {
      getSwingContainer().setBorder(null);
      getSwingBodyPart().setName(null);
      //button bar has always insets
    }
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    IGroupBox gb = getScoutObject();
    if (name.equals(IGroupBox.PROP_EXPANDED)) {
      setExpandedFromScout();
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_NAME)) {
      setBackgroundImageFromScout(gb.getBackgroundImageName());
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT)) {
      setBackgroundImageHorizontalAlignFromScout(gb.getBackgroundImageHorizontalAlignment());
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT)) {
      setBackgroundImageVerticalAlignFromScout(gb.getBackgroundImageVerticalAlignment());
    }
  }

  protected void handleSwingGroupBoxExpanded(final boolean expanded) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    //notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setExpandedFromUI(expanded);
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  private class P_ExpansionListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("expanded".equals(evt.getPropertyName()) && evt.getNewValue() != null) {
        handleSwingGroupBoxExpanded((Boolean) evt.getNewValue());
      }
    }
  } // end class P_ExpansionListener

}
