/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.tree;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractActionNodeTest {

  @Test
  public void testParentAndContainerAvailable() {
    IMenu m = new AbstractMenu() {
      @Override
      protected void injectActionNodesInternal(OrderedCollection<IMenu> actionNodes) {
        actionNodes.addLast(new AbstractMenu() {
        });
      }
    };

    IWidget container = Mockito.mock(IWidget.class);
    m.setContainerInternal(container);
    m.setParentInternal(container);
    assertSame(container, m.getContainer());
    assertEquals(1, m.getChildActionCount());
    assertSame(container, m.getParent());

    IMenu childAction = m.getChildActions().get(0);
    assertSame(container, childAction.getContainer());
    assertSame(m, childAction.getParent());

    IMenu secondChild = new AbstractMenu() {
    };
    m.addChildAction(secondChild);
    assertEquals(2, m.getChildActionCount());
    assertSame(container, secondChild.getContainer());
    assertSame(m, secondChild.getParent());

    m.removeChildAction(secondChild);
    assertEquals(1, m.getChildActionCount());
    assertNull(secondChild.getContainer());
    assertNull(secondChild.getParent());
  }
}
