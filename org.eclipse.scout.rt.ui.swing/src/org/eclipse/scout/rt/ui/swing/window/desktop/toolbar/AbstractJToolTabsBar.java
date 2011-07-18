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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

public abstract class AbstractJToolTabsBar extends AbstractJTabBar {

  public static final String PROP_COLLAPSED = "collapsed";
  public static final String PROP_MINIMUM_SIZE = "minimumSize";

  private static final long serialVersionUID = 1L;

  public abstract void rebuild(IDesktop desktop);

  public abstract void setSwingScoutToolBarContainer(SwingScoutToolBar swingScoutToolBarContainer);
}
