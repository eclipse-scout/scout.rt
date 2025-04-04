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

import org.eclipse.scout.rt.api.data.prefs.userfilter.DateColumnUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.prefs.TablePreferencesClientHelper;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.DateColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

@ApplicationScoped
public class DateColumnUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<DateColumnUserFilterStateDo, DateColumnUserFilterState> implements IUserFilterStateDoEntityMapper {

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof DateColumnUserFilterState;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof DateColumnUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    DateColumnUserFilterStateDo stateDo = BEANS.get(DateColumnUserFilterStateDo.class);
    toDo((DateColumnUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    DateColumnUserFilterStateDo stateDo = (DateColumnUserFilterStateDo) stateDo0;
    IColumn<?> column = BEANS.get(TablePreferencesClientHelper.class).resolveColumn(table, stateDo.getColumnId());
    if (column == null) {
      return null;
    }

    DateColumnUserFilterState state = new DateColumnUserFilterState(column);
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<DateColumnUserFilterStateDo, DateColumnUserFilterState> mappings) {
    TablePreferencesClientHelper helper = BEANS.get(TablePreferencesClientHelper.class);
    mappings
        .with(DateColumnUserFilterStateDo::columnId, state -> helper.getColumnId(state.getColumn())) // value already set in state creation
        .with(DateColumnUserFilterStateDo::selectedValues, this::getSelectedValues, this::setSelectedValues)
        .with(DateColumnUserFilterStateDo::dateFrom, DateColumnUserFilterState::getDateFrom, DateColumnUserFilterState::setDateFrom)
        .with(DateColumnUserFilterStateDo::dateTo, DateColumnUserFilterState::getDateTo, DateColumnUserFilterState::setDateTo);
  }

  protected Set<Integer> getSelectedValues(DateColumnUserFilterState state) {
    return BEANS.get(UserFilterStateHelper.class).getSelectedValues(state.getSelectedValues(), Integer.class, Collectors.toSet());
  }

  protected void setSelectedValues(DateColumnUserFilterState state, Set<Integer> selectedValues) {
    state.setSelectedValues(new HashSet<>(selectedValues));
  }
}
