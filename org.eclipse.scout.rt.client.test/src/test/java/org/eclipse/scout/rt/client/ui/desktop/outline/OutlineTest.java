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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractOutline}
 */
@RunWith(ClientTestRunner.class)
public class OutlineTest {

  @Test
  public void testEvents() {
    AbstractOutline o = new AbstractOutline() {
    };
    final SavingTreeListener listener = new SavingTreeListener();
    o.addTreeListener(listener);
    o.setTreeChanging(true);
    o.firePageChanged(mock(IPage.class));
    o.firePageChanged(mock(IPage.class));
    o.setTreeChanging(false);
    final List<? extends TreeEvent> batch = listener.getBatch();
    assertEquals(1, batch.size());
    final TreeEvent e = batch.get(0);
    assertEquals(OutlineEvent.TYPE_PAGE_CHANGED, e.getType());
  }

  private static class SavingTreeListener extends TreeAdapter {
    private List<? extends TreeEvent> m_batch = null;

    @Override
    public void treeChangedBatch(List<? extends TreeEvent> batch) {
      m_batch = batch;
    }

    public List<? extends TreeEvent> getBatch() {
      return m_batch;
    }

  }

}
