/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.holders;

/**
 * Holder for rows of bean based table (see {@link ITableBeanHolder}).
 *
 * @since 3.10.0-M3
 */
public interface ITableBeanRowHolder {

  /**
   * same value as {@link ITableHolder#STATUS_NON_CHANGED}.
   */
  int STATUS_NON_CHANGED = ITableHolder.STATUS_NON_CHANGED;

  /**
   * same value as {@link ITableHolder#STATUS_INSERTED}.
   */
  int STATUS_INSERTED = ITableHolder.STATUS_INSERTED;

  /**
   * same value as {@link ITableHolder#STATUS_UPDATED}.
   */
  int STATUS_UPDATED = ITableHolder.STATUS_UPDATED;

  /**
   * same value as {@link ITableHolder#STATUS_DELETED}.
   */
  int STATUS_DELETED = ITableHolder.STATUS_DELETED;

  /**
   * @return Returns this row's state.
   * @see #STATUS_NON_CHANGED
   * @see #STATUS_INSERTED
   * @see #STATUS_UPDATED
   * @see #STATUS_DELETED
   */
  int getRowState();

}
