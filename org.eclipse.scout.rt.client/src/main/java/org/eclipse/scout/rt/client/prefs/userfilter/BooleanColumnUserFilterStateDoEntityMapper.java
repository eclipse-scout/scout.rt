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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.prefs.userfilter.BooleanColumnUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.prefs.TablePreferencesClientHelper;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Special case for boolean columns, where the UI uses a TextColumnUserFilterState, but stores numbers for selected
 * values.
 */
@Order(4000) // must be before TextColumnUserFilterStateDoEntityMapper
@ApplicationScoped
public class BooleanColumnUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<BooleanColumnUserFilterStateDo, TextColumnUserFilterState> implements IUserFilterStateDoEntityMapper {

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof TextColumnUserFilterState && ((TextColumnUserFilterState) state).getColumn() instanceof IBooleanColumn;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof BooleanColumnUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    BooleanColumnUserFilterStateDo stateDo = BEANS.get(BooleanColumnUserFilterStateDo.class);
    toDo((TextColumnUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    BooleanColumnUserFilterStateDo stateDo = (BooleanColumnUserFilterStateDo) stateDo0;
    IColumn<?> column = BEANS.get(TablePreferencesClientHelper.class).resolveColumn(table, stateDo.getColumnId());
    if (column == null) {
      return null;
    }

    TextColumnUserFilterState state = new TextColumnUserFilterState(column);
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<BooleanColumnUserFilterStateDo, TextColumnUserFilterState> mappings) {
    TablePreferencesClientHelper helper = BEANS.get(TablePreferencesClientHelper.class);
    mappings
        .with(BooleanColumnUserFilterStateDo::columnId, state -> helper.getColumnId(state.getColumn())) // value already set in state creation
        .with(BooleanColumnUserFilterStateDo::selectedValues, this::getSelectedValues, this::setSelectedValues);
  }

  protected Set<Boolean> getSelectedValues(TextColumnUserFilterState state) {
    if (CollectionUtility.isEmpty(state.getSelectedValues())) {
      return Collections.emptySet();
    }
    return state.getSelectedValues().stream()
        .filter(Objects::nonNull)
        .map(val -> TypeCastUtility.castValue(val, Boolean.class))
        .collect(Collectors.toSet());
  }

  protected void setSelectedValues(TextColumnUserFilterState state, Set<Boolean> selectedValues) {
    Set<Integer> values = selectedValues.stream().filter(Objects::nonNull).map(val -> TypeCastUtility.castValue(val, Integer.class)).collect(Collectors.toSet());
    state.setSelectedValues(new HashSet<>(values));
  }
}
