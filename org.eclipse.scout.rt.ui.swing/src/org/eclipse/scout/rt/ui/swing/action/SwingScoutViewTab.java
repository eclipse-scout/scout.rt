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

import javax.swing.AbstractButton;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.JTabEx;

public class SwingScoutViewTab extends AbstractSwingScoutActionButton<IViewButton> {

  @Override
  protected AbstractButton createButton(ISwingEnvironment env) {
    JTabEx swingButton = new JTabEx();
    swingButton.setMargin(new Insets(2, 5, 2, 5));
    return swingButton;
  }
}
