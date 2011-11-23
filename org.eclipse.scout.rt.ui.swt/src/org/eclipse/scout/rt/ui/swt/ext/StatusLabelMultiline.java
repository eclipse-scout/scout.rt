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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.comp.CLabelEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Version;

/**
 * <p>
 * Contains a label which actually is a {@link StyledText}.
 * </p>
 * <p>
 * Compared to {@link StatusLabelEx} which uses a {@link CLabelEx} the text won't be shortened. Additionally the place
 * of the status icon is different. It is located next to the text and not at the right side.
 * </p>
 */
public class StatusLabelMultiline extends StatusLabelEx {
  private StyledText m_label;

  public StatusLabelMultiline(Composite parent, int style, ISwtEnvironment environment) {
    super(parent, style, environment);
  }

  @Override
  protected void createLayout() {
    setLayout(new LogicalGridLayout(0, 0));
  }

  @Override
  protected void createContent(Composite parent, int style) {
    m_label = getEnvironment().getFormToolkit().createStyledText(parent, style);
    m_label.setEnabled(false);
    setMarginsOnLabel(m_label);

    setNonMandatoryFont(m_label.getFont());
    setNonMandatoryForegroundColor(m_label.getForeground());

    setStatusLabel(new Label(parent, SWT.NONE));
    getEnvironment().getFormToolkit().getFormToolkit().adapt(getStatusLabel(), false, false);

    // layout
    LogicalGridData data = new LogicalGridData();
    data.gridx = 0;
    data.weightx = 0.0;
    data.useUiWidth = true;
    data.useUiHeight = true;
    m_label.setLayoutData(data);

    data = new LogicalGridData();
    data.gridx = 1;
    data.weightx = 1;
    data.useUiWidth = true;
    data.useUiHeight = true;
    getStatusLabel().setLayoutData(data);
  }

  @Override
  public void setLayoutWidthHint(int w) {
    Object o = getLayoutData();
    if (o instanceof LogicalGridData) {
      LogicalGridData data = (LogicalGridData) o;
      data.widthHint = w;
    }
  }

  @Override
  protected void updateLabelForeground() {
    if (getMandatoryForegroundColor() != null) {
      m_label.setForeground(getMandatoryForegroundColor());
    }
    else {
      m_label.setForeground(getNonMandatoryForegroundColor());
    }
  }

  @Override
  protected void updateLabelFont() {
    if (getMandatoryFont() != null) {
      m_label.setFont(getMandatoryFont());
    }
    else {
      m_label.setFont(getNonMandatoryFont());
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

  @Override
  protected void updateText() {
    String text = getText();
    if (text == null) {
      text = "";
    }
    m_label.setText(getPreMarker() + text + getPostMarker());
  }

  @Override
  public Font getFont() {
    return m_label.getFont();
  }

  @Override
  public String getDisplayText() {
    return m_label.getText();
  }

  protected void setMarginsOnLabel(StyledText label) {
    //Necessary for backward compatibility to Eclipse 3.4 needed for Lotus Notes 8.5.2
    Version frameworkVersion = new Version(Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version"));
    if (frameworkVersion.getMajor() == 3
        && frameworkVersion.getMinor() <= 5) {
    }
    else {
      try {
        //Make sure the wrap indent is the same as the indent so that the text is vertically aligned
        Method setWrapIndent = StyledText.class.getMethod("setWrapIndent", int.class);
        setWrapIndent.invoke(label, label.getIndent());
        //Make sure the text is horizontally aligned with the label
        int borderWidth = 4;
        Method setMargins = StyledText.class.getMethod("setMargins", int.class, int.class, int.class, int.class);
        setMargins.invoke(label, 0, borderWidth, 0, borderWidth);
      }
      catch (Exception e) {
        Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, "could not access methods 'setWrapIndent' and 'setMargins' on 'StyledText'.", e));
      }
    }
  }
}
