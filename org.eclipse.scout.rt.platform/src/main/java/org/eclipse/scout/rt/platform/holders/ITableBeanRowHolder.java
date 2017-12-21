/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.holders;

import org.eclipse.scout.rt.platform.util.ChangeStatus;

/**
 * Holder for rows of bean based table (see {@link ITableBeanHolder}).
 *
 * @since 3.10.0-M3
 */
@FunctionalInterface
public interface ITableBeanRowHolder {

  /**
   * Describes a row that has not been changed.
   */
  int STATUS_NON_CHANGED = ChangeStatus.NOT_CHANGED;

  /**
   * Describes a row that was inserted.
   */
  int STATUS_INSERTED = ChangeStatus.INSERTED;

  /**
   * Describes a row that was updated.
   */
  int STATUS_UPDATED = ChangeStatus.UPDATED;

  /**
   * Describes a row that has been deleted.
   */
  int STATUS_DELETED = ChangeStatus.DELETED;

  /**
   * @return Returns this row's state.
   * @see #STATUS_NON_CHANGED
   * @see #STATUS_INSERTED
   * @see #STATUS_UPDATED
   * @see #STATUS_DELETED
   */
  int getRowState();

}
