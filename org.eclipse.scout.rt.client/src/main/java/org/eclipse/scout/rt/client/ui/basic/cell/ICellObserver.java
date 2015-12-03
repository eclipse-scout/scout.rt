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
package org.eclipse.scout.rt.client.ui.basic.cell;

public interface ICellObserver {

  /**
   * before the fact report before a new value is stored in the cell
   * 
   * @return validated value
   */
  Object validateValue(ICell cell, Object value);

  /**
   * after the fact report after a value was stored in the cell
   */
  void cellChanged(ICell cell, int changedBit);

}
