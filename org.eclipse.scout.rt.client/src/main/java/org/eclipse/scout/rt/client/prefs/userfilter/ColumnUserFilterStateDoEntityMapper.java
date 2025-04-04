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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.prefs.userfilter.ColumnUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.prefs.TablePreferencesClientHelper;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;

@ApplicationScoped
@Order(999_999) // always last because fallback if no other matches
public class ColumnUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<ColumnUserFilterStateDo, ColumnUserFilterState> implements IUserFilterStateDoEntityMapper {

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof ColumnUserFilterState;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof ColumnUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    ColumnUserFilterStateDo stateDo = BEANS.get(ColumnUserFilterStateDo.class);
    toDo((ColumnUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    ColumnUserFilterStateDo stateDo = (ColumnUserFilterStateDo) stateDo0;
    IColumn<?> column = BEANS.get(TablePreferencesClientHelper.class).resolveColumn(table, stateDo.getColumnId());
    if (column == null) {
      return null;
    }

    ColumnUserFilterState state = new ColumnUserFilterState(column);
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<ColumnUserFilterStateDo, ColumnUserFilterState> mappings) {

    TablePreferencesClientHelper helper = BEANS.get(TablePreferencesClientHelper.class);
    mappings
        .with(ColumnUserFilterStateDo::columnId, state -> helper.getColumnId(state.getColumn())) // value already set in state creation
        .with(ColumnUserFilterStateDo::selectedValues, this::getSelectedValues, this::setSelectedValues);
  }

  protected Set<String> getSelectedValues(ColumnUserFilterState state) {
    return BEANS.get(UserFilterStateHelper.class).getSelectedValues(state.getSelectedValues(), String.class, Collectors.toSet());
  }

  protected void setSelectedValues(ColumnUserFilterState state, Set<String> selectedValues) {
    state.setSelectedValues(new HashSet<>(selectedValues));
  }
}
