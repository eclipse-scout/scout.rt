/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.prefs.userfilter;

import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Base interface for mappers that map user filter states to user filter settings.
 */
@ApplicationScoped
public interface IUserFilterStateDoEntityMapper {

  /**
   * @return <code>true</code> if the given state is handled by this mapper.
   */
  boolean accept(IUserFilterState state);

  /**
   * @return <code>true</code> if the given state is handled by this mapper.
   */
  boolean accept(IUserFilterStateDo stateDo);

  IUserFilterStateDo toDo(ITable table, IUserFilterState state);

  IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo);
}
