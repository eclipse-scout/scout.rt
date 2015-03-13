/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link TreeEventBuffer}
 */
public class TreeEventBufferTest {

  private TreeEventBuffer m_testBuffer;

  @Before
  public void setup() {
    m_testBuffer = new TreeEventBuffer();
  }

  /**
   * Some events should not be coalesced: selected, updated, row_action.
   */
  @Test
  public void testNoCoalesce() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_ACTION);
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED);
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_DRAG_REQUEST);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    final List<TreeEvent> coalesced = m_testBuffer.removeEvents();
    assertEquals(3, coalesced.size());
    assertSame(e1, coalesced.get(0));
    assertSame(e2, coalesced.get(1));
    assertSame(e3, coalesced.get(2));
  }

  /**
   * Only the last selection event must be kept.
   */
  @Test
  public void testSelections() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_SELECTED);
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED);
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_SELECTED);
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_SELECTED);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.removeEvents();
    assertEquals(2, coalesced.size());
    assertSame(e2, coalesced.get(0));
    assertSame(e4, coalesced.get(1));
  }

  private TreeEvent mockEvent(int type) {
    return new TreeEvent(mock(ITree.class), type);
  }

}
