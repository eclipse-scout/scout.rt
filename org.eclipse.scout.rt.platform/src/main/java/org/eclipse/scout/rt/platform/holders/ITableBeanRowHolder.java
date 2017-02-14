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
package org.eclipse.scout.rt.platform.holders;

/**
 * Holder for rows of bean based table (see {@link ITableBeanHolder}).
 *
 * @since 3.10.0-M3
 */
public interface ITableBeanRowHolder {

  int STATUS_NON_CHANGED = 0;

  int STATUS_INSERTED = 1;

  int STATUS_UPDATED = 2;

  int STATUS_DELETED = 3;

  /**
   * @return Returns this row's state.
   * @see #STATUS_NON_CHANGED
   * @see #STATUS_INSERTED
   * @see #STATUS_UPDATED
   * @see #STATUS_DELETED
   */
  int getRowState();

}
