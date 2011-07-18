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
package org.eclipse.scout.rt.ui.swing.action;

import java.awt.Insets;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.JTabEx;

public class SwingScoutToolTab extends AbstractSwingScoutActionButton<IToolButton> {

  @Override
  protected JTabEx createButton(ISwingEnvironment env) {
    JTabEx swingButton = new JTabEx(env);
    swingButton.setMargin(new Insets(2, 5, 2, 5));
    // replace model to allow deselection of the tool button, meaning that no tool button is activated
    swingButton.setModel(new ToggleButtonModel());
    return swingButton;
  }
}
