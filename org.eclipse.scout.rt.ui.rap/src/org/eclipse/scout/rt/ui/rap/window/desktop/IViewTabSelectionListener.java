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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import java.util.EventListener;

/**
 * <h3>IViewTabSelectionListener</h3> ...
 *
 * @author aho
 * @since 3.7.0 June 2011
 */
public interface IViewTabSelectionListener extends EventListener {

  void handleEvent(ViewTabSelectionEvent event);
}
