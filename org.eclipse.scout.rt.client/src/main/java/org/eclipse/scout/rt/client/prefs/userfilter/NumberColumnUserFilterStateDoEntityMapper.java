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
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.prefs.userfilter.NumberColumnUserFilterStateDo;
import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.prefs.TablePreferencesClientHelper;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.NumberColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NumberColumnUserFilterStateDoEntityMapper extends AbstractDoEntityMapper<NumberColumnUserFilterStateDo, NumberColumnUserFilterState> implements IUserFilterStateDoEntityMapper {

  private static final Logger LOG = LoggerFactory.getLogger(NumberColumnUserFilterStateDoEntityMapper.class);

  @Override
  public boolean accept(IUserFilterState state) {
    return state instanceof NumberColumnUserFilterState;
  }

  @Override
  public boolean accept(IUserFilterStateDo stateDo) {
    return stateDo instanceof NumberColumnUserFilterStateDo;
  }

  @Override
  public IUserFilterStateDo toDo(ITable table, IUserFilterState state) {
    NumberColumnUserFilterStateDo stateDo = BEANS.get(NumberColumnUserFilterStateDo.class);
    toDo((NumberColumnUserFilterState) state, stateDo);
    return stateDo;
  }

  @Override
  public IUserFilterState fromDo(ITable table, IUserFilterStateDo stateDo0) {
    NumberColumnUserFilterStateDo stateDo = (NumberColumnUserFilterStateDo) stateDo0;
    IColumn<?> column = BEANS.get(TablePreferencesClientHelper.class).resolveColumn(table, stateDo.getColumnId());
    if (column == null) {
      return null;
    }

    NumberColumnUserFilterState state = new NumberColumnUserFilterState(column);
    fromDo(stateDo, state);
    return state;
  }

  @Override
  protected void initMappings(DoEntityMappings<NumberColumnUserFilterStateDo, NumberColumnUserFilterState> mappings) {
    TablePreferencesClientHelper helper = BEANS.get(TablePreferencesClientHelper.class);
    mappings
        .with(NumberColumnUserFilterStateDo::columnId, state -> helper.getColumnId(state.getColumn())) // value already set in state creation
        .with(NumberColumnUserFilterStateDo::selectedValues, this::getSelectedValues, this::setSelectedValues)
        .with(NumberColumnUserFilterStateDo::numberFrom, NumberColumnUserFilterState::getNumberFrom, NumberColumnUserFilterState::setNumberFrom)
        .with(NumberColumnUserFilterStateDo::numberTo, NumberColumnUserFilterState::getNumberTo, NumberColumnUserFilterState::setNumberTo);
  }

  protected Set<Double> getSelectedValues(NumberColumnUserFilterState state) {
    // Special handling for numbers, state contains Doubles and Integers in selected filters at the same time.
    // We convert all of them to Doubles because the UI doesn't care anyway and selects the Integer correctly given a Double representation of it.
    if (CollectionUtility.isEmpty(state.getSelectedValues())) {
      return Collections.emptySet();
    }
    return state.getSelectedValues().stream()
        .filter(selectedValue -> {
          if (selectedValue != null && !(selectedValue instanceof Double) && !(selectedValue instanceof Integer)) {
            LOG.warn("Unexpected user filter selected value '{}', expected class Double or Integer. Value is skipped", selectedValue);
            return false;
          }
          return true;
        })
        .map(obj -> (Number) obj)
        .map(NumberUtility::toDouble)
        .collect(Collectors.toSet());
  }

  protected void setSelectedValues(NumberColumnUserFilterState state, Set<Double> selectedValues) {
    state.setSelectedValues(new HashSet<>(selectedValues));
  }
}
