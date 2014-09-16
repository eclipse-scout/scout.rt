/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic.tree;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.eclipse.scout.rt.testing.shared.HandlerAdapter;
import org.eclipse.scout.rt.ui.swing.basic.AbstractHtmlLinkDetector;
import org.junit.Test;

/**
 * Test for {@link TreeHtmlLinkDetector}
 *
 * @since 4.1.0
 */
public class TreeHtmlLinkDetectorTest {

  @Test
  public void testNoNPEinDetect() {
    TreeHtmlLinkDetector detector = new TreeHtmlLinkDetector();
    installLogInterceptor();
    JTree container = mock(JTree.class);
    when(container.getCellRenderer()).thenReturn(mock(TreeCellRenderer.class));
    when(container.getPathForLocation(0, 0)).thenReturn(null);

    detector.detect(container, new Point(0, 0)); //this should not lead to a NPE
  }

  private void installLogInterceptor() {
    Logger logger = LogManager.getLogManager().getLogger(AbstractHtmlLinkDetector.class.getName());
    logger.addHandler(new P_LogHandler());
  }

  private class P_LogHandler extends HandlerAdapter {

    @Override
    public void publish(LogRecord record) {
      if (record.getThrown() != null) {
        fail("An exception ocurred! " + record.getThrown());
      }
    }
  }
}
