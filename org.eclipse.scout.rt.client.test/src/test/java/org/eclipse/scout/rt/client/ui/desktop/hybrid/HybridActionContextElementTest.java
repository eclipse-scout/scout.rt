/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HybridActionContextElementTest {

  @Test
  public void testWithNull() {
    assertThrows(AssertionException.class, () -> HybridActionContextElement.of(null));
    assertThrows(AssertionException.class, () -> HybridActionContextElement.of(null, null));
  }

  @Test
  public void testWithWidget() {
    IForm form = new DummyForm();
    HybridActionContextElement contextElement = HybridActionContextElement.of(form);

    assertSame(form, contextElement.getWidget());
    assertSame(form, contextElement.getWidget(DummyForm.class));
    assertNotNull(contextElement.getWidget(DummyForm.class).getDummyField());
    assertTrue(contextElement.getWidget(DummyForm.class) instanceof DummyForm);

    assertNull(contextElement.optElement());
    assertThrows(AssertionException.class, () -> contextElement.getElement());
  }

  @Test
  public void testWithWidgetAndElement() {
    ITree tree = new AbstractTree() {
      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.File;
      }
    };
    ITreeNode treeNode = new AbstractTreeNode() {
    };
    HybridActionContextElement contextElement = HybridActionContextElement.of(tree, treeNode);

    assertSame(tree, contextElement.getWidget());
    assertSame(tree, contextElement.getWidget(ITree.class));
    assertNotNull(contextElement.getWidget(ITree.class).getIconId());
    assertThrows(AssertionException.class, () -> contextElement.getWidget(IForm.class));

    assertSame(treeNode, contextElement.optElement());
    assertSame(treeNode, contextElement.optElement(ITreeNode.class));
    assertTrue(contextElement.optElement(ITreeNode.class).isVisible());
    assertSame(treeNode, contextElement.getElement());
    assertSame(treeNode, contextElement.getElement(ITreeNode.class));
    assertTrue(contextElement.getElement(ITreeNode.class).isVisible());
    assertThrows(AssertionException.class, () -> contextElement.optElement(ITableRow.class));
    assertThrows(AssertionException.class, () -> contextElement.getElement(ITableRow.class));

    // Unrelated elements are ok in model code (but not in JSON layer)
    IForm form = new DummyForm();
    HybridActionContextElement.of(form, new Object());
    HybridActionContextElement.of(form, treeNode);
  }
}
