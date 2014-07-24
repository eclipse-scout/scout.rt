/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.swt.widgets.Button;

public interface IRwtScoutFormButton extends IRwtScoutComposite<IAction> {

  @Override
  Button getUiField();

  void makeButtonActive();

  void makeButtonInactive();

}
