/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.navigation;

import java.util.EventListener;

/**
 * The listener interface for receiving navigation history events.
 */
public interface NavigationHistoryListener extends EventListener {

  /**
   * Invoked when the navigation history has changed: Bookmarks have been changed, added or removed from the history.
   */
  void navigationChanged(NavigationHistoryEvent e);

}
