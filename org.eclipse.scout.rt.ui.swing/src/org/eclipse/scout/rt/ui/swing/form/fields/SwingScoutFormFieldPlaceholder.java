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

import javax.swing.SwingConstants;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;

/**
 * used for field models that have no ui implementation
 */
public class SwingScoutFormFieldPlaceholder extends SwingScoutFieldComposite<IFormField> implements ISwingScoutFormField<IFormField> {

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    //
    JLabelEx label = new JLabelEx();
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setText("Placeholder for " + getScoutObject().getClass().getSimpleName());
    JPanelEx container = new JPanelEx(new BorderLayoutEx(0, 0));
    container.add(BorderLayoutEx.CENTER, label);
    //
    setSwingContainer(container);
    setSwingField(label);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }
}
