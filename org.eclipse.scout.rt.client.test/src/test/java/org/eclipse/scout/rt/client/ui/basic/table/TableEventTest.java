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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

/**
 * Tests for {@link TableEvent}
 */
public class TableEventTest {

  @Test
  public void testToString() throws Exception {
    final TableEvent e = new TableEvent(mock(ITable.class), TableEvent.TYPE_ALL_ROWS_DELETED);
    assertTrue(e.toString().contains("TYPE_ALL_ROWS_DELETED"));
  }

}
