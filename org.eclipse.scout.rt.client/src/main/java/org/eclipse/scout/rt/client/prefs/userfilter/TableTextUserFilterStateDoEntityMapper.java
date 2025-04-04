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

import org.eclipse.scout.rt.api.data.prefs.userfilter.TableTextUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

@ApplicationScoped
public class TableTextUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<TableTextUserFilterStateDo, TableTextUserFilterState> implements IUserFilterStateDoEntityMapper {

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof TableTextUserFilterState;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof TableTextUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    TableTextUserFilterStateDo stateDo = BEANS.get(TableTextUserFilterStateDo.class);
    toDo((TableTextUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    TableTextUserFilterStateDo stateDo = (TableTextUserFilterStateDo) stateDo0;
    TableTextUserFilterState state = new TableTextUserFilterState(stateDo.getText());
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<TableTextUserFilterStateDo, TableTextUserFilterState> mappings) {
    mappings
        .with(TableTextUserFilterStateDo::text, TableTextUserFilterState::getText);
  }
}
