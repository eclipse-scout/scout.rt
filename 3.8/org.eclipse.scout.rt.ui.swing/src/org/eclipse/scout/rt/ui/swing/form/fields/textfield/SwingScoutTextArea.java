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
package org.eclipse.scout.rt.ui.swing.form.fields.textfield;

import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellingMonitor;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextAreaEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.spellchecker.ISwingSpellCheckerService;
import org.eclipse.scout.rt.ui.swing.spellchecker.SwingFieldHolder;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutTextArea extends SwingScoutTextFieldComposite<IStringField> implements ISwingScoutTextArea {

  private JScrollPane m_swingScrollPane;
  private ISpellingMonitor m_spellingMonitor;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JTextComponent textField = new JTextAreaEx();
    textField.setOpaque(true);
    m_swingScrollPane = new JScrollPaneEx(textField);
    m_swingScrollPane.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
    container.add(m_swingScrollPane);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JScrollPane getSwingScrollPane() {
    return m_swingScrollPane;
  }

  @Override
  public JTextAreaEx getSwingTextArea() {
    return (JTextAreaEx) getSwingTextComponent();
  }

  /*
   * scout property handlers
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    setTextWrapFromScout(getScoutObject().isWrapText());
    // spell checking
    ISwingSpellCheckerService spellCheckerService = SERVICES.getService(ISwingSpellCheckerService.class);
    if (spellCheckerService != null && spellCheckerService.isInstalled()) {
      m_spellingMonitor = spellCheckerService.createSpellingMonitor(new SwingFieldHolder(this, this.getSwingTextArea()));
    }
  }

  @Override
  protected void detachScout() {
    // spell checking
    if (m_spellingMonitor != null) {
      m_spellingMonitor.dispose();
      m_spellingMonitor = null;
    }
    super.detachScout();
  }

  @Override
  protected void setKeyStrokesFromScout() {
    super.setKeyStrokesFromScout();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    float swingAlignX = SwingUtility.createAlignmentX(scoutAlign);
    getSwingTextArea().setAlignmentX(swingAlignX);
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    float swingAlignY = SwingUtility.createAlignmentY(scoutAlign);
    getSwingTextArea().setAlignmentY(swingAlignY);
  }

  protected void setTextWrapFromScout(boolean b) {
    getSwingTextArea().setLineWrap(b);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IStringField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }
}
