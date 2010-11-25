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

import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellingMonitor;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.spellchecker.ISwingSpellCheckerService;
import org.eclipse.scout.rt.ui.swing.spellchecker.SwingFieldHolder;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutTextField extends SwingScoutTextFieldComposite<IStringField> implements ISwingScoutTextField {

  private ISpellingMonitor m_spellingMonitor;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    JTextFieldEx textField = new JTextFieldEx();
    container.add(textField);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  public JTextFieldEx getSwingTextField() {
    return (JTextFieldEx) getSwingTextComponent();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
    getSwingTextField().setHorizontalAlignment(swingAlign);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // spell checking
    ISwingSpellCheckerService spellCheckerService = SERVICES.getService(ISwingSpellCheckerService.class);
    if (spellCheckerService != null && spellCheckerService.isInstalled()) {
      m_spellingMonitor = spellCheckerService.createSpellingMonitor(new SwingFieldHolder(this, this.getSwingTextField()));
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
}
