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
package org.eclipse.scout.rt.client.ui.desktop;

/**
 * Describes the action that should be used by the UI to handle the URI in the desktop's "open URI" feature.
 */
public interface IOpenUriAction {

  /**
   * @return the identifier (known to the UI) for this action.
   */
  String getIdentifier();
}
