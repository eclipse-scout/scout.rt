/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.services.lookup.LookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam.QueryBy;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchicalLookupResultBuilder<VALUE> {

  private static final Logger LOG = LoggerFactory.getLogger(HierarchicalLookupResultBuilder.class);

  private final ISmartField<VALUE> m_smartField;

  private ILookupRowByKeyProvider<VALUE> m_lookupRowProvider;

  private final Map<VALUE, ILookupRow<VALUE>> m_keyCache = new HashMap<>();

  public HierarchicalLookupResultBuilder(ISmartField<VALUE> smartField) {
    m_smartField = smartField;
  }

  /**
   * @param lookupRows
   * @param selectedKey
   *          selected key or null
   * @return
   */
  protected List<ILookupRow<VALUE>> getRowsWithParents(List<ILookupRow<VALUE>> lookupRows, VALUE parent) {
    List<ILookupRow<VALUE>> res = new ArrayList<>();

    cacheKeys(lookupRows);
    Set<VALUE> allRows = new HashSet<>();

    List<List<ILookupRow<VALUE>>> paths = createPaths(lookupRows);
    if (parent == null) {
      // ensure, the path to the root is included for every node
      for (List<ILookupRow<VALUE>> path : paths) {
        for (ILookupRow<VALUE> row : path) {
          if (!allRows.contains(row.getKey())) {
            allRows.add(row.getKey());
            res.add(row);
          }

        }
      }
    }
    else {
      // ensure that rows other then children of the parent are included
      for (List<ILookupRow<VALUE>> path : paths) {
        if (contains(parent, path)) {
          ILookupRow<VALUE> leaf = path.get(path.size() - 1);
          if (!allRows.contains(leaf.getKey())) {
            allRows.add(leaf.getKey());
            res.add(leaf);
          }
        }
      }
    }
    return res;
  }

  protected boolean contains(VALUE key, List<ILookupRow<VALUE>> path) {
    if (key == null) {
      return false;
    }
    for (ILookupRow<VALUE> row : path) {
      if (key.equals(row.getKey())) {
        return true;
      }
    }
    return false;
  }

  protected ILookupRow<VALUE> getLookupRow(VALUE key) {
    if (key == null) {
      return null;
    }
    if (!m_keyCache.containsKey(key)) {
      ILookupRow<VALUE> row = m_lookupRowProvider.getLookupRow(key);
      m_keyCache.put(key, row);
      return row;
    }
    return m_keyCache.get(key);
  }

  /**
   * Collects the path to the root for each node using the existing tree to lookup parent nodes, if possible. Each path
   * starts with the root node.
   */
  protected List<List<ILookupRow<VALUE>>> createPaths(Collection<? extends ILookupRow<VALUE>> lookupRows) {
    Map<VALUE, ILookupRow<VALUE>> parentMap = createParentMap(lookupRows);
    List<List<ILookupRow<VALUE>>> paths = new ArrayList<>();
    for (ILookupRow<VALUE> row : lookupRows) {
      // build path to root for this row
      List<ILookupRow<VALUE>> path = new ArrayList<>();
      ILookupRow<VALUE> r = row;
      while (r != null) {
        path.add(0, r);
        VALUE parentKey = r.getParentKey();
        if (parentKey == null) {
          // no parent
          break;
        }
        if (!parentMap.containsKey(r.getKey())) {
          ILookupRow<VALUE> parentRow = getLookupRow(parentKey);
          parentMap.put(r.getKey(), parentRow);
        }
        r = parentMap.get(r.getKey());
      }
      paths.add(path);
    }
    return paths;
  }

  protected void cacheKeys(Collection<? extends ILookupRow<VALUE>> lookupRows) {
    for (ILookupRow<VALUE> row : lookupRows) {
      m_keyCache.put(row.getKey(), row);
    }
  }

  /**
   * Creates a map containing every key in the tree and its parent tree node
   */
  protected Map<VALUE, ILookupRow<VALUE>> createParentMap(Collection<? extends ILookupRow<VALUE>> lookupRows) {
    for (ILookupRow<VALUE> row : lookupRows) {
      m_keyCache.put(row.getKey(), row);
    }
    final Map<VALUE, ILookupRow<VALUE>> map = new HashMap<>();
    for (ILookupRow<VALUE> row : lookupRows) {
      map.put(row.getKey(), getLookupRow(row.getParentKey()));
    }
    return map;
  }

  /**
   * @return all new rows to be inserted (including parents of search result)
   */
  public ILookupCallResult<VALUE> addParentLookupRows(ILookupCallResult<VALUE> result) {
    List<ILookupRow<VALUE>> lookupRows;
    if (m_smartField.isLoadParentNodes()) {
      if (m_smartField.isBrowseLoadIncremental()) {
        m_lookupRowProvider = new P_KeyLookupRowProvider();
      }
      else {
        m_lookupRowProvider = new P_BrowseLookupRowProvider();
      }
      VALUE recKey = null;
      IQueryParam<VALUE> queryParam = result.getQueryParam();
      if (queryParam.is(QueryBy.REC)) {
        recKey = queryParam.getKey();
      }
      lookupRows = getRowsWithParents(result.getLookupRows(), recKey);
    }
    else {
      lookupRows = result.getLookupRows();
    }
    return new LookupCallResult<>(lookupRows, result.getQueryParam(), result.getException());
  }

  private class P_KeyLookupRowProvider implements ILookupRowByKeyProvider<VALUE> {

    @Override
    public ILookupRow<VALUE> getLookupRow(VALUE key) {
      // do not cancel lookups that are already in progress
      List<ILookupRow<VALUE>> rows = LookupJobHelper.await(m_smartField.callKeyLookupInBackground(key, false));
      if (rows.isEmpty()) {
        return null;
      }
      else if (rows.size() > 1) {
        LOG.error("More than one row found for key {}", key);
        return null;
      }
      return rows.get(0);
    }
  }

  private class P_BrowseLookupRowProvider implements ILookupRowByKeyProvider<VALUE> {

    private final FinalValue<Map<VALUE, ILookupRow<VALUE>>> m_rows = new FinalValue<>();

    @Override
    public ILookupRow<VALUE> getLookupRow(VALUE key) {
      m_rows.setIfAbsentAndGet(() -> {
        List<ILookupRow<VALUE>> rows = LookupJobHelper.await(m_smartField.callBrowseLookupInBackground(false));
        Map<VALUE, ILookupRow<VALUE>> rowMap = new HashMap<>();
        for (ILookupRow<VALUE> r : rows) {
          rowMap.put(r.getKey(), r);
        }
        return Collections.unmodifiableMap(rowMap);
      });
      return m_rows.get().get(key);
    }
  }

}
