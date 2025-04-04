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

import org.eclipse.scout.rt.api.data.prefs.userfilter.TextColumnUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.prefs.TablePreferencesClientHelper;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

@ApplicationScoped
public class TextColumnUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<TextColumnUserFilterStateDo, TextColumnUserFilterState> implements IUserFilterStateDoEntityMapper {

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof TextColumnUserFilterState;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof TextColumnUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    TextColumnUserFilterStateDo stateDo = BEANS.get(TextColumnUserFilterStateDo.class);
    toDo((TextColumnUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    TextColumnUserFilterStateDo stateDo = (TextColumnUserFilterStateDo) stateDo0;
    IColumn<?> column = BEANS.get(TablePreferencesClientHelper.class).resolveColumn(table, stateDo.getColumnId());
    if (column == null) {
      return null;
    }

    TextColumnUserFilterState state = new TextColumnUserFilterState(column);
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<TextColumnUserFilterStateDo, TextColumnUserFilterState> mappings) {
    TablePreferencesClientHelper helper = BEANS.get(TablePreferencesClientHelper.class);
    mappings
        .with(TextColumnUserFilterStateDo::columnId, state -> helper.getColumnId(state.getColumn())) // value already set in state creation
        .with(TextColumnUserFilterStateDo::selectedValues, this::getSelectedValues, this::setSelectedValues)
        .with(TextColumnUserFilterStateDo::textFilter, TextColumnUserFilterState::getFreeText, TextColumnUserFilterState::setFreeText);
  }

  protected Set<String> getSelectedValues(TextColumnUserFilterState state) {
    return BEANS.get(UserFilterStateHelper.class).getSelectedValues(state.getSelectedValues(), String.class, Collectors.toSet());
  }

  protected void setSelectedValues(TextColumnUserFilterState state, Set<String> selectedValues) {
    state.setSelectedValues(new HashSet<>(selectedValues));
  }
}
