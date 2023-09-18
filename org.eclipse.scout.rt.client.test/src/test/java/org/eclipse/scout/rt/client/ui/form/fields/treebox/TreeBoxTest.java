/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link AbstractTreeBox}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TreeBoxTest {

  private static List<IBean<?>> m_beans;

  private HashSet<Long> testValue;

  @BeforeClass
  public static void beforeClass() {
    m_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(TreeBoxLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
  }

  @Before
  public void setUp() {
    testValue = new HashSet<>();
    testValue.add(1L);
  }

  /**
   * Select a parent node in a tree box and check whether only this node is selected..
   * {@link AbstractTreeBox#getConfiguredAutoCheckChildNodes} returns false on the {@link SimpleTreeBox}. Bug 368107 -
   * Check child nodes when parent node is checked
   */
  @Test
  public void testDefaultBehavior() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    ITree tree = treeBox.getTree();

    ITreeNode node = tree.findNode(1L); // A
    assertNotNull(node);
    tree.setNodeChecked(node, true);

    Set<Long> valueSet = new HashSet<>(treeBox.getValue());

    // only one node selected
    assertEquals(1, valueSet.size());

    // and the selected one is the node explicitly set before
    assertEquals(true, valueSet.contains(1L)); // A
  }

  /**
   * Tests that a initially checked node which is removed from the tree is not contained in the set of checked nodes
   * after removal.
   */
  @Test
  public void testCheckedNodesRemovedOn() {
    HashSet<Long> initialValues = CollectionUtility.hashSet(1L, 4L, 5L);
    SimpleTreeBox treeBox = createSimpleTreeBoxWithInitialSelection(initialValues);
    ITree tree = treeBox.getTree();

    // ensure initial value is set and checked nodes equals initial values
    assertEquals(initialValues.size(), tree.getCheckedNodesCount());
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(initialValues), tree.getCheckedNodes()));

    // re-initialize field and ensure checked nodes are equal to the initial values
    treeBox.reinit();
    assertEquals(initialValues.size(), tree.getCheckedNodesCount());
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(initialValues), tree.getCheckedNodes()));
  }

  @Test
  public void testCheckKeysByModel() {
    SimpleTreeBox treeBox = new SimpleTreeBox();
    treeBox.init();
    ITree tree = treeBox.getTree();

    treeBox.checkAllKeys();
    assertEquals(14, treeBox.getCheckedKeyCount());

    treeBox.uncheckAllKeys();
    assertEquals(0, treeBox.getCheckedKeyCount());

    treeBox.checkKey(9L);

    Set<Long> valueSet = new HashSet<>(treeBox.getValue());
    assertEquals(1, valueSet.size());
    assertEquals(true, valueSet.contains(9L)); // C-B
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(CollectionUtility.hashSet(9L)), tree.getCheckedNodes()));

    treeBox.checkKeys(CollectionUtility.hashSet(9L, 12L));

    valueSet = new HashSet<>(treeBox.getValue());
    assertEquals(2, valueSet.size());
    assertEquals(true, valueSet.contains(9L)); // C-B
    assertEquals(true, valueSet.contains(12L)); // C-B-B
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(CollectionUtility.hashSet(9L, 12L)), tree.getCheckedNodes()));
  }

  @Test
  public void testAutoCheckKeysByModel() {
    AutoSelectTreeBox treeBox = new AutoSelectTreeBox();
    treeBox.init();
    ITree tree = treeBox.getTree();

    treeBox.checkAllKeys();
    assertEquals(14, treeBox.getCheckedKeyCount());

    treeBox.uncheckAllKeys();
    assertEquals(0, treeBox.getCheckedKeyCount());

    // Checking nodes by model should not auto-check child nodes
    treeBox.checkKey(9L);

    Set<Long> valueSet = new HashSet<>(treeBox.getValue());
    assertEquals(1, valueSet.size());
    assertEquals(true, valueSet.contains(9L)); // C-B
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(CollectionUtility.hashSet(9L)), tree.getCheckedNodes()));

    treeBox.checkKeys(CollectionUtility.hashSet(9L, 12L));

    valueSet = new HashSet<>(treeBox.getValue());
    assertEquals(2, valueSet.size());
    assertEquals(true, valueSet.contains(9L)); // C-B
    assertEquals(true, valueSet.contains(12L)); // C-B-B
    assertTrue(CollectionUtility.equalsCollection(tree.findNodes(CollectionUtility.hashSet(9L, 12L)), tree.getCheckedNodes()));
  }

  @Test
  public void testNullKeys() {
    AutoSelectTreeBox treeBox = new AutoSelectTreeBox();
    treeBox.init();
    treeBox.checkAllKeys();
    assertEquals(14, treeBox.getCheckedKeyCount()); // assert the null key is not available
  }

  /**
   * Test {@link #execIsEmpty} empty field
   */
  @Test
  public void testEmpty() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    assertTrue(treeBox.isEmpty());
    assertTrue(treeBox.getValue().isEmpty());
    assertEquals(0, treeBox.getCheckedKeyCount());
    assertEquals(null, treeBox.getCheckedKey());
  }

  /**
   * Test {@link #execIsEmpty} non empty
   */
  @Test
  public void testNonEmpty() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    treeBox.setValue(testValue);
    assertFalse(treeBox.isEmpty());
    ScoutAssert.assertSetEquals(testValue, treeBox.getValue());
    assertEquals(1, treeBox.getCheckedKeyCount());
    assertEquals(Long.valueOf(1L), treeBox.getCheckedKey());
  }

  /**
   * Tests that the content is valid for a filled mandatory field. {@link #isContentValid()}
   */
  @Test
  public void testContentValid() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    treeBox.setValue(testValue);
    treeBox.setMandatory(true);
    assertTrue(treeBox.isMandatory());
    assertTrue(treeBox.isContentValid());
  }

  /**
   * Tests that the content is valid for an empty mandatory field. {@link #isContentValid()}
   */
  @Test
  public void testContentInvalid() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    treeBox.setMandatory(true);
    assertTrue(treeBox.isMandatory());
    assertFalse(treeBox.isContentValid());
  }

  /**
   * Tests that the content is valid for an empty mandatory field. {@link #isContentValid()}
   */
  @Test
  public void testContentInvalidError() {
    SimpleTreeBox treeBox = createSimpleTreeBox();
    treeBox.addErrorStatus("Error");
    assertFalse(treeBox.isContentValid());
  }

  @Test
  public void testValidationOfNullValue() {
    ValidatingTreeBox treeBox = createValidatingTreeBox();
    findAndSetNodeChecked(treeBox, null, true);
    assertEquals(treeBox.getTree().getCheckedNodes().size(), 0);
  }

  @Test
  public void testValidation() {
    ValidatingTreeBox treeBox = createValidatingTreeBox();
    findAndSetNodeChecked(treeBox, 1L, true);
    findAndSetNodeChecked(treeBox, 5L, true);
    findAndSetNodeChecked(treeBox, 10L, true);
    //up to 16, all is well:

    assertEquals(treeBox.getTree().getCheckedNodes().size(), 3);
    assertTrue(CollectionUtility.equalsCollection(getCheckedTreeNodeKeys(treeBox), CollectionUtility.hashSet(1L, 5L, 10L)));

    findAndSetNodeChecked(treeBox, 6L, true);
    //validation now adjusts value.

    assertEquals(treeBox.getTree().getCheckedNodes().size(), 2);
    assertTrue(CollectionUtility.equalsCollection(getCheckedTreeNodeKeys(treeBox), CollectionUtility.hashSet(6L, 10L)));
  }

  private SimpleTreeBox createSimpleTreeBox() {
    SimpleTreeBox treeBox = new SimpleTreeBox();
    treeBox.init();
    return treeBox;
  }

  private SimpleTreeBox createSimpleTreeBoxWithInitialSelection(final Set<Long> initialValues) {
    SimpleTreeBox treeBox = new SimpleTreeBox() {

      @Override
      public Set<Long> getInitValue() {
        return initialValues;
      }

    };
    treeBox.init();
    treeBox.resetValue();
    return treeBox;
  }

  private ValidatingTreeBox createValidatingTreeBox() {
    ValidatingTreeBox treeBox = new ValidatingTreeBox();
    treeBox.init();
    return treeBox;
  }

  private <T> void findAndSetNodeChecked(ITreeBox<T> treeBox, T pk, boolean check) {
    ITreeNode node = treeBox.getTree().findNode(pk);
    if (node != null) {
      node.setChecked(check);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> getCheckedTreeNodeKeys(ITreeBox<T> treeBox) {
    Set<T> checkedKeys = CollectionUtility.hashSet();
    for (ITreeNode n : treeBox.getTree().getCheckedNodes()) {
      checkedKeys.add((T) n.getPrimaryKey());
    }
    return checkedKeys;
  }

  public class SimpleTreeBox extends AbstractTreeBox<Long> {

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return TreeBoxLookupCall.class;
    }

  }

  public class AutoSelectTreeBox extends AbstractTreeBox<Long> {

    @Override
    protected boolean getConfiguredAutoCheckChildNodes() {
      return true;
    }

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return TreeBoxLookupCall.class;
    }
  }

  public class ValidatingTreeBox extends AbstractTreeBox<Long> {

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return TreeBoxLookupCall.class;
    }

    @Override
    protected Set<Long> execValidateValue(Set<Long> rawValue) {

      // only allow multi check if the sum of the keys is below 20.
      // if it exceeds 20, the smallest keys are thrown out, until
      // condition is satisfied.

      //Does not accept null key.

      rawValue.remove(null);

      long sum = 0L;
      for (Long l : rawValue) {
        sum += l;
      }

      if (sum >= 20) {
        List<Long> sorted = CollectionUtility.arrayList(rawValue);
        Collections.sort(sorted);
        while (sum >= 20) {
          Long elem = sorted.remove(0);
          sum -= elem;
        }
        return CollectionUtility.hashSet(sorted);
      }
      else {
        return rawValue;
      }

    }

    public class ValidatingTreeBoxTree extends DefaultTreeBoxTree {

      @Override
      protected boolean getConfiguredMultiCheck() {
        return true;
      }

    }
  }

  public static class TreeBoxLookupCall extends LocalLookupCall<Long> {

    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Long>> execCreateLookupRows() {
      List<ILookupRow<Long>> list = new ArrayList<>();
      list.add(new LookupRow<>(1L, "A"));
      list.add(new LookupRow<>(2L, "B"));
      list.add(new LookupRow<>(3L, "C"));
      list.add(new LookupRow<>(4L, "D"));
      list.add(new LookupRow<>(5L, "A-A").withParentKey(1L));
      list.add(new LookupRow<>(6L, "A-B").withParentKey(1L));
      list.add(new LookupRow<>(7L, "A-C").withParentKey(1L));
      list.add(new LookupRow<>(8L, "C-A").withParentKey(3L));
      list.add(new LookupRow<>(9L, "C-B").withParentKey(3L));
      list.add(new LookupRow<>(10L, "C-C").withParentKey(3L));
      list.add(new LookupRow<>(11L, "C-B-A").withParentKey(9L));
      list.add(new LookupRow<>(12L, "C-B-B").withParentKey(9L));
      list.add(new LookupRow<>(13L, "C-B-C").withParentKey(9L));
      list.add(new LookupRow<>(14L, "C-B-D").withParentKey(9L));
      list.add(new LookupRow<Long>(null, "null key").withParentKey(9L));
      return list;
    }
  }

}
