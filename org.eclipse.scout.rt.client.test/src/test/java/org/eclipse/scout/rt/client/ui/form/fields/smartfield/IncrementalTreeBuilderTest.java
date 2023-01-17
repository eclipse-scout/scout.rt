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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link IncrementalTreeBuilder}
 */
public class IncrementalTreeBuilderTest {

  @Test
  public void testCreateParentMap_EmptyTree() {
    ITree tree = createTestTree();
    Map<Long, ILookupRow<Long>> parentMap = new IncrementalTreeBuilder<Long>(null)
        .createParentMap(tree);
    assertTrue(parentMap.isEmpty());
  }

  @Test
  public void testCreateParentMap_NonEmpty() {
    ITree tree = createTestTree();
    tree.addChildNode(tree.getRootNode(), createNode(1L));
    Map<Long, ILookupRow<Long>> parentMap = new IncrementalTreeBuilder<Long>(null)
        .createParentMap(tree);

    assertTrue(parentMap.size() == 1);
    assertTrue(parentMap.containsKey(1L));
  }

  @Test
  public void testCreatePaths_Empty() {
    ITree tree = createTestTree();
    IncrementalTreeBuilder<Long> builder = new IncrementalTreeBuilder<>(null);
    ArrayList<ILookupRow<Long>> rows = new ArrayList<>();
    List<List<ILookupRow<Long>>> paths = builder.createPaths(rows, tree);
    assertTrue(paths.isEmpty());
  }

  @Test
  public void testCreatePaths_NonEmpty() {
    @SuppressWarnings("unchecked")
    ILookupRowByKeyProvider<Long> mockProvider = Mockito.mock(ILookupRowByKeyProvider.class);
    ITree tree = createTestTree();
    IncrementalTreeBuilder<Long> builder = new IncrementalTreeBuilder<>(mockProvider);
    ArrayList<ILookupRow<Long>> rows = new ArrayList<>();
    rows.add(new LookupRow<>(1L, ""));
    rows.add(new LookupRow<>(2L, "").withParentKey(1L));
    List<List<ILookupRow<Long>>> paths = builder.createPaths(rows, tree);
    assertTrue(paths.size() == 2);
  }

  @Test
  public void testCreatePaths_NullKeyLookupRow() {
    final Map<Long, ILookupRow<Long>> lookupRowsMap = new HashMap<>();
    lookupRowsMap.put(1L, new LookupRow<>(1L, "A"));
    lookupRowsMap.put(2L, new LookupRow<>(2L, "A-B").withParentKey(1L));
    lookupRowsMap.put(null, new LookupRow<>(null, "(none)"));
    Collection<ILookupRow<Long>> rows = lookupRowsMap.values();

    ILookupRowByKeyProvider<Long> provider = key -> lookupRowsMap.get(key);

    ITree tree = createTestTree();
    IncrementalTreeBuilder<Long> builder = new IncrementalTreeBuilder<>(provider);

    List<List<ILookupRow<Long>>> paths = builder.createPaths(rows, tree);
    assertEquals(3, paths.size());
  }

  private ITree createTestTree() {
    AbstractTree tree = new AbstractTree() {
    };
    tree.init();
    return tree;
  }

  private AbstractTreeNode createNode(Long key) {
    AbstractTreeNode node = new AbstractTreeNode() {
    };
    node.getCellForUpdate().setValue(new LookupRow<>(1L, ""));
    return node;
  }

}
