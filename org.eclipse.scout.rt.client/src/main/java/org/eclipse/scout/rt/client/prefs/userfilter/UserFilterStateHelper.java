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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.api.data.table.IUserFilterStateDo;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for working with user defined filter states. These are the filter options a user can select on a column
 * headers, table text filters etc.
 * <p>
 * Also see {@link IUserFilterStateDo}
 */
@ApplicationScoped
public class UserFilterStateHelper {

  private static final Logger LOG = LoggerFactory.getLogger(UserFilterStateHelper.class);

  /**
   * Applies filter settings to table.
   */
  public void applyUserColumnFilterStates(ITable table, Collection<IUserFilterStateDo> filterStateDos) {
    if (table.getUserFilterManager() == null || CollectionUtility.isEmpty(filterStateDos)) {
      return;
    }

    TableUserFilterManager userFilterManager = table.getUserFilterManager();
    userFilterManager.reset();
    filterStateDos.stream()
        .filter(Objects::nonNull)
        .map(setting -> createUserFilterState(table, setting))
        .filter(Objects::nonNull)
        .forEach(userFilterManager::addFilter);
  }

  /**
   * Collects all filters on table and creates corresponding filter object.
   *
   * @return List of user filter settings or an empty list if none are available.
   */
  public List<IUserFilterStateDo> createUserFilterStates(ITable table) {
    TableUserFilterManager userFilterManager = table.getUserFilterManager();
    if (userFilterManager == null || CollectionUtility.isEmpty(userFilterManager.getFilters())) {
      return Collections.emptyList();
    }

    return userFilterManager.getFilters().stream()
        .filter(Objects::nonNull)
        .map(filter -> createUserFilterSettingsDo(table, filter))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  protected IUserFilterStateDo createUserFilterSettingsDo(ITable table, IUserFilterState filterState) {
    IUserFilterStateDoEntityMapper mapper = getUserFilterSettingsMapperForFilterState(filterState);
    if (mapper == null) {
      return null;
    }
    return mapper.toDo(table, filterState);
  }

  protected IUserFilterState createUserFilterState(ITable table, IUserFilterStateDo filerStateDo) {
    IUserFilterStateDoEntityMapper mapper = getUserFilterSettingsMapperForFilterSettings(filerStateDo);
    if (mapper == null) {
      return null;
    }
    return mapper.fromDo(table, filerStateDo);
  }

  protected IUserFilterStateDoEntityMapper getUserFilterSettingsMapperForFilterState(IUserFilterState filterState) {
    IUserFilterStateDoEntityMapper userFilterStateMapper = BEANS.all(IUserFilterStateDoEntityMapper.class).stream()
        .filter(mapper -> mapper.accept(filterState))
        .findFirst()
        .orElse(null);

    if (userFilterStateMapper == null) {
      LOG.warn("Unknown user filterState state class {}. Ignoring filterState.", filterState != null ? filterState.getClass() : null);
      return null;
    }
    return userFilterStateMapper;
  }

  protected IUserFilterStateDoEntityMapper getUserFilterSettingsMapperForFilterSettings(IUserFilterStateDo filterStateDo) {
    IUserFilterStateDoEntityMapper userFilterStateMapper = BEANS.all(IUserFilterStateDoEntityMapper.class).stream()
        .filter(mapper -> mapper.accept(filterStateDo))
        .findFirst()
        .orElse(null);

    if (userFilterStateMapper == null) {
      LOG.warn("Unknown user filter state class {}. Ignoring filter.", filterStateDo != null ? filterStateDo.getClass() : null);
      return null;
    }
    return userFilterStateMapper;
  }

  /**
   * Collects selected values from UI for use in data objects
   *
   * @return collection of selected values
   */
  public <CLASS, T extends Collection<CLASS>> T getSelectedValues(Collection<Object> selectedValues, Class<CLASS> typeClass, Collector<CLASS, ?, T> collector) {
    if (CollectionUtility.isEmpty(selectedValues)) {
      return Stream.empty().map(typeClass::cast).collect(collector); // map call is needed so return stream is typed
    }
    return selectedValues.stream()
        .filter(selectedValue -> {
          if (selectedValue != null && !typeClass.isInstance(selectedValue)) {
            LOG.warn("Unexpected user filter selected value '{}', expected class {}. Value is skipped", selectedValue, typeClass);
            return false;
          }
          return true;
        })
        .map(typeClass::cast)
        .collect(collector);
  }
}
