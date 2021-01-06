/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.logger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ExpiringDuplicateMessageFilterTest {
  private static final Marker MATCHING_MARKER = MarkerFactory.getMarker("filterDuplicates");
  private static final Marker NON_MATCHING_MARKER = MarkerFactory.getMarker("nonMatching");
  private static final String MESSAGE_FORMAT = "Message {}";

  @Test
  public void testDuplicateFilter() {
    ExpiringDuplicateMessageFilter filter = new ExpiringDuplicateMessageFilter();
    filter.setAllowedRepetitions(2);
    filter.start();

    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, NON_MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, NON_MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, NON_MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, NON_MATCHING_MARKER, MESSAGE_FORMAT));

    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.DENY, decide(filter, MATCHING_MARKER, MESSAGE_FORMAT));
    Assert.assertEquals(FilterReply.DENY, decide(filter, MATCHING_MARKER, MESSAGE_FORMAT));

    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, MATCHING_MARKER, "New message"));
    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, NON_MATCHING_MARKER, MESSAGE_FORMAT));

    Assert.assertEquals(FilterReply.NEUTRAL, decide(filter, MATCHING_MARKER, null));
  }

  protected FilterReply decide(TurboFilter filter, Marker marker, String format) {
    return filter.decide(marker, null, Level.ALL, format, null, null);
  }
}
