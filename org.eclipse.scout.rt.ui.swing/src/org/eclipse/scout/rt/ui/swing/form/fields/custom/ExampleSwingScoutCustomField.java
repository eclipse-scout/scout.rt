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
package org.eclipse.scout.rt.ui.swing.form.fields.custom;

import javax.swing.JPanel;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.customfield.ICustomField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class ExampleSwingScoutCustomField extends SwingScoutFieldComposite {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ExampleSwingScoutCustomField.class);

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();

    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);

    JPanel specialComponent = new JPanel();// whatever
    container.add(specialComponent);

    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(specialComponent);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  public ICustomField getScoutCustomField() {
    return (ICustomField) getScoutObject();
  }

  public JPanel getSwingSpecialComponent() {
    return (JPanel) getSwingField();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    // special enabling on my field
    // ...
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
  }

}
