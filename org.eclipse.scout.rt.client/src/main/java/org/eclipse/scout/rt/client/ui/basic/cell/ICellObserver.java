/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
