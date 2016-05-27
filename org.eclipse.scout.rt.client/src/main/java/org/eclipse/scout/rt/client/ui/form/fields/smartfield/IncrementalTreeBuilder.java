package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * <h3>{@link IncrementalTreeBuilder}</h3>
 * <p>
 * Builder to create a tree from a search result. The result may consist of child nodes without their parents.
 * <p>
 * In order to create the complete result, parent nodes may need to be loaded. Optimized in such a way that as few
 * lookups as possible need to be done.
 * <p>
 * Assumes values of type LOOKUP_ROW_TYPE in tree cells
 */
public class IncrementalTreeBuilder<LOOKUP_KEY> {

  private final IKeyLookupProvider<LOOKUP_KEY> m_provider;

  public IncrementalTreeBuilder(IKeyLookupProvider<LOOKUP_KEY> provider) {
    m_provider = provider;
  }

  public Collection<ILookupRow<LOOKUP_KEY>> getRowsWithParents(List<? extends ILookupRow<LOOKUP_KEY>> lookupRows, ITree existingTree) {
    Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> allRows = new HashMap<>();
    List<List<ILookupRow<LOOKUP_KEY>>> paths = createPaths(lookupRows, existingTree);
    for (List<ILookupRow<LOOKUP_KEY>> path : paths) {
      for (ILookupRow<LOOKUP_KEY> row : path) {
        allRows.put(row.getKey(), row);
      }
    }
    return allRows.values();
  }

  /**
   * Collects the path to the root for each node using the existing tree to lookup parent nodes, if possible. Each path
   * starts with the root node.
   */
  public List<List<ILookupRow<LOOKUP_KEY>>> createPaths(List<? extends ILookupRow<LOOKUP_KEY>> lookupRows, ITree existingTree) {
    Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> parentMap = createParentMap(existingTree);
    List<List<ILookupRow<LOOKUP_KEY>>> paths = new ArrayList<>();
    for (ILookupRow<LOOKUP_KEY> row : lookupRows) {
      //build path to root for this row
      List<ILookupRow<LOOKUP_KEY>> path = new ArrayList<>();
      ILookupRow<LOOKUP_KEY> r = row;
      while (r != null) {
        path.add(0, r);
        if (!parentMap.containsKey(r.getKey())) {
          ILookupRow<LOOKUP_KEY> parentRow = m_provider.getLookupRow(r.getParentKey());
          parentMap.put(r.getKey(), parentRow);
        }
        r = parentMap.get(r.getKey());
      }
      paths.add(path);
    }
    return paths;
  }

  /**
   * Assumes values of type LOOKUP_ROW_TYPE in tree cells
   */
  @SuppressWarnings("unchecked")
  private ILookupRow<LOOKUP_KEY> getLookupRow(ITreeNode treeNode) {
    return (ILookupRow<LOOKUP_KEY>) treeNode.getCell().getValue();
  }

  /**
   * Creates a map containing every key in the tree and its parent tree node
   */
  public Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> createParentMap(ITree tree) {
    final Map<LOOKUP_KEY, ILookupRow<LOOKUP_KEY>> map = new HashMap<>();
    tree.visitTree(new ITreeVisitor() {

      @Override
      public boolean visit(ITreeNode node) {
        ITreeNode parent = node.getParentNode();
        ILookupRow<LOOKUP_KEY> row = getLookupRow(node);
        if (row != null) {
          LOOKUP_KEY key = row.getKey();
          map.put(key, getLookupRow(parent));
        }
        return true;
      }
    });
    return map;
  }

}
