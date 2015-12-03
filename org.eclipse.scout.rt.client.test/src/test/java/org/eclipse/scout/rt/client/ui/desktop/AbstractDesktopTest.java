/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractDesktopTest {

  private IOutline m_outline;

  private AbstractDesktop m_desktop;

  @Before
  public void setUp() {
    ITreeNode node = Mockito.mock(ITreeNode.class);
    Mockito.when(node.getChildNodes()).thenReturn(Collections.<ITreeNode> emptyList());

    m_outline = Mockito.mock(IOutline.class);
    Mockito.when(m_outline.getActivePage()).thenReturn(null);
    Mockito.when(m_outline.getRootNode()).thenReturn(node);

    m_desktop = new AbstractDesktop(false) {
      @Override
      public List<IOutline> getAvailableOutlines() {
        return Collections.singletonList(m_outline);
      }
    };
  }

  @Test
  public void testIsOutlineChanging_Default() {
    assertFalse(m_desktop.isOutlineChanging());
  }

  /**
   * Tests whether the outlineChanged flag is set to true while setOutline() is running.
   */
  @Test
  public void testIsOutlineChanging_setOutline() {
    final boolean[] called = {false};
    assertFalse(m_desktop.isOutlineChanging());
    m_desktop.addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        if (DesktopEvent.TYPE_OUTLINE_CHANGED == e.getType()) {
          called[0] = true;
          assertTrue(m_desktop.isOutlineChanging());
        }
      }
    });
    m_desktop.setOutline(m_outline);
    assertTrue(called[0]);
    assertFalse(m_desktop.isOutlineChanging());
  }
}
