/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class RequestHistoryTest {

  @Test
  public void testInitialState() {
    RequestHistory history = new RequestHistory();

    // Check that no exceptions are thrown on an empty history
    history.toString();
    history.getUiSession();

    // Inital state
    assertEquals(Long.valueOf(-1), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(-1L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(-9999L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(9999L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());
  }

  @Test(expected = AssertionException.class)
  public void testInvalidAccess() {
    RequestHistory history = new RequestHistory();
    history.isRequestProcessed(null);
  }

  @Test
  public void testRequests() {
    RequestHistory history = new RequestHistory();

    // Insert #0
    history.setRequestProcessed(Long.valueOf(0L));
    assertEquals(Long.valueOf(0L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());

    // Insert #1
    history.setRequestProcessed(Long.valueOf(1L));
    assertEquals(Long.valueOf(1L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(1L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());

    // Insert #5 <-- miss #2, #3, #4
    history.setRequestProcessed(Long.valueOf(5L));
    assertEquals(Long.valueOf(5L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(1L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(2L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(3L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(4L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(5L)));
    assertEquals(3, history.getMissingRequestSequenceNos().size());

    // Insert #3 <-- miss #2, #4
    history.setRequestProcessed(Long.valueOf(3L));
    assertEquals(Long.valueOf(5L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(1L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(2L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(3L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(4L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(5L)));
    assertEquals(2, history.getMissingRequestSequenceNos().size());

    // Insert #2, #4, #6
    history.setRequestProcessed(Long.valueOf(2L));
    history.setRequestProcessed(Long.valueOf(4L));
    history.setRequestProcessed(Long.valueOf(6L));
    assertEquals(Long.valueOf(6L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(1L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(2L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(3L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(4L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(5L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(6L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(7L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());
  }

  @Test
  public void testSingleMissedRequest() {
    RequestHistory history = new RequestHistory();

    // Insert #0
    history.setRequestProcessed(Long.valueOf(0L));
    assertEquals(Long.valueOf(0L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());

    // Insert #2-#200
    for (long i = 2; i <= 200; i++) {
      history.setRequestProcessed(Long.valueOf(i));
    }
    assertEquals(Long.valueOf(200L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(3L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(199L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(200L)));
    assertEquals(1, history.getMissingRequestSequenceNos().size()); // still missing!

    // Insert #300 --> +99 missing numbers
    history.setRequestProcessed(Long.valueOf(300L));
    assertEquals(Long.valueOf(300L), history.getLastProcessedSequenceNo());
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(201L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(299L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(300L)));
    assertEquals(100, history.getMissingRequestSequenceNos().size());

    // Insert #302 --> miss #301, forget #1
    history.setRequestProcessed(Long.valueOf(302L));
    assertEquals(Long.valueOf(302L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(1L))); // <-- !!!
    assertFalse(history.isRequestProcessed(Long.valueOf(201L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(299L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(300L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(301L)));
    assertEquals(100, history.getMissingRequestSequenceNos().size());
  }

  @Test
  public void testHackerAttack() {
    RequestHistory history = new RequestHistory();

    // Insert #0
    history.setRequestProcessed(Long.valueOf(0L));
    assertEquals(Long.valueOf(0L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertEquals(0, history.getMissingRequestSequenceNos().size());

    // Insert #2 --> miss #1
    history.setRequestProcessed(Long.valueOf(2L));
    assertEquals(Long.valueOf(2L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertFalse(history.isRequestProcessed(Long.valueOf(1L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(2L)));
    assertEquals(1, history.getMissingRequestSequenceNos().size());

    // Insert #1000000
    history.setRequestProcessed(Long.valueOf(1000000L));
    assertEquals(Long.valueOf(1000000L), history.getLastProcessedSequenceNo());
    assertTrue(history.isRequestProcessed(Long.valueOf(0L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(1L))); // <-- !!!
    assertTrue(history.isRequestProcessed(Long.valueOf(3L)));
    assertTrue(history.isRequestProcessed(Long.valueOf(888L))); // <-- !!!
    assertTrue(history.isRequestProcessed(Long.valueOf(9999L))); // <-- !!!
    assertTrue(history.isRequestProcessed(Long.valueOf(999899L))); // <-- !!!
    assertFalse(history.isRequestProcessed(Long.valueOf(999900L))); // <-- !!!
    assertFalse(history.isRequestProcessed(Long.valueOf(999998L))); // <-- !!!
    assertFalse(history.isRequestProcessed(Long.valueOf(999999L))); // <-- !!!
    assertTrue(history.isRequestProcessed(Long.valueOf(1000000L)));
    assertEquals(100, history.getMissingRequestSequenceNos().size());
  }
}
