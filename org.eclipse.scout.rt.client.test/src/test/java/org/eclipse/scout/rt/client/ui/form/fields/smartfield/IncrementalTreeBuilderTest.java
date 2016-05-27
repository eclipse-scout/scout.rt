package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.junit.Test;

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
    IncrementalTreeBuilder<Long> builder = new IncrementalTreeBuilder<Long>(null);
    ArrayList<ILookupRow<Long>> rows = new ArrayList<>();
    List<List<ILookupRow<Long>>> paths = builder.createPaths(rows, tree);
    assertTrue(paths.isEmpty());
  }

  @Test
  public void testCreatePaths_NonEmpty() {
    ITree tree = createTestTree();
    IncrementalTreeBuilder<Long> builder = new IncrementalTreeBuilder<Long>(null);
    ArrayList<ILookupRow<Long>> rows = new ArrayList<>();
    rows.add(new LookupRow<Long>(1L, ""));
    rows.add(new LookupRow<Long>(2L, "").withParentKey(1L));
    List<List<ILookupRow<Long>>> paths = builder.createPaths(rows, tree);
    assertTrue(paths.size() == 2);
  }

  private ITree createTestTree() {
    AbstractTree tree = new AbstractTree() {
    };
    tree.initTree();
    return tree;
  }

  private AbstractTreeNode createNode(Long key) {
    AbstractTreeNode node = new AbstractTreeNode() {
    };
    node.getCellForUpdate().setValue(new LookupRow<Long>(1L, ""));
    return node;
  }

}
