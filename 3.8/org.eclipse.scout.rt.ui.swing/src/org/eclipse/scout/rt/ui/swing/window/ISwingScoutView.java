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
package org.eclipse.scout.rt.ui.swing.window;

import javax.swing.JComponent;

public interface ISwingScoutView {

  void openView();

  void closeView();

  boolean isVisible();

  boolean isActive();

  void setTitle(String s);

  void setCloseEnabled(boolean b);

  void setMaximizeEnabled(boolean b);

  void setMinimizeEnabled(boolean b);

  void setMaximized(boolean b);

  void setMinimized(boolean b);

  void addSwingScoutViewListener(SwingScoutViewListener listener);

  void removeSwingScoutViewListener(SwingScoutViewListener listener);

  JComponent getSwingContentPane();

  void setName(String name);
}
