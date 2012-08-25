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
package org.eclipse.scout.service;

import org.eclipse.core.runtime.IAdaptable;

/**
 * This interface is obtained from {@link IAdaptable} objects by calling {@link
 * IAdaptable#getAdapter(IServiceInventory.class)}
 */
public interface IServiceInventory {

  /**
   * @return plain or html styled text describing the current inventory of the
   *         service
   */
  String getInventory();

}
