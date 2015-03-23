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

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEventBuffer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link OutlineEventBuffer}
 */
public class OutlineEventBufferTest {

  private OutlineEventBuffer m_testBuffer;

  @Before
  public void setup() {
    m_testBuffer = new OutlineEventBuffer();
  }

  /**
   * Only the last selection event must be kept.
   */
  @Test
  public void testSelections() {
    final OutlineEvent e1 = new OutlineEvent(mock(IOutline.class), OutlineEvent.TYPE_PAGE_CHANGED);
    final OutlineEvent e2 = new OutlineEvent(mock(IOutline.class), OutlineEvent.TYPE_PAGE_CHANGED);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertSame(e2, coalesced.get(0));
  }
}
