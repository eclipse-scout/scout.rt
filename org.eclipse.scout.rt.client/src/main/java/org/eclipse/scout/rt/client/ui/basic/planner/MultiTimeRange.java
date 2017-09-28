/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: Allows to store multiple time ranges. There are no overlapping on the stored ranges.
 * <p>
 * e.g. {'1.1.2006 13:00'-'1.1.2006 14:00', '2.1.2006 12:00'-'3.1.2006 12:00'} Copyright (c) 2006 BSI AG Company: BSI AG
 * - www.bsiag.com
 *
 * @since 03.02.2006
 * @version 1.0
 */
class MultiTimeRange {
  private static final Logger LOG = LoggerFactory.getLogger(MultiTimeRange.class);

  private TreeMap<Date/* fromDate */, TimeRange> m_multipleTimeRangeMap = new TreeMap<>();

  public MultiTimeRange() {
    super();
  }

  protected MultiTimeRange(MultiTimeRange other) {
    m_multipleTimeRangeMap = other.m_multipleTimeRangeMap;
  }

  /**
   * add a time range to this timerange-collection.
   * <p>
   * if the new time range has overlappings with stored timeranges, the collection is updated accordingly.
   * <p>
   * restrictions: from and to must not be <code>null</code>. from < to.
   *
   * @param from
   * @param to
   * @return the really added {@link TimeRange}s.
   * @throws IllegalArgumentException
   * @since 03.02.2006 - tha@bsiag.com
   */
  public MultiTimeRange add(Date from, Date to) {
    checkParams(from, to);
    MultiTimeRange addedTimeRanges = new MultiTimeRange();
    addedTimeRanges.internalPutNoOldEntry(from, to);
    /* check overlapping */
    SortedMap<Date, TimeRange> headMap = m_multipleTimeRangeMap.headMap(to);
    boolean first = true;
    boolean finished = false;
    while (!finished && !headMap.isEmpty()) {
      final Object tailKey = headMap.lastKey();
      final TimeRange tail = headMap.get(tailKey);
      final Date tailTo = tail.getTo();
      if (first) {
        first = false;
        /* check old_to > new_to */
        if (tailTo.after(to)) {
          to = tailTo;
        }
      }
      final Date tailFrom = tail.getFrom();
      /* check no overlap */
      if (tailTo.before(from)) {
        finished = true;
      }
      else {
        /* check old_from < new_from */
        if (tailFrom.before(from)) {
          finished = true;
          from = tailFrom;
        }
        m_multipleTimeRangeMap.remove(tailKey);
        addedTimeRanges.remove(tailFrom, tailTo);
      }
    }
    internalPutNoOldEntry(from, to);
    return addedTimeRanges;
  }

  public void setSingleTimerange(Date from, Date to) {
    m_multipleTimeRangeMap = new TreeMap<>();
    internalPutNoOldEntry(from, to);
  }

  private void internalPutNoOldEntry(Date from, Date to) {
    try {
      checkParams(from, to);
    }
    catch (IllegalArgumentException iaex) {
      LOG.debug("adding [{}, {}] failed", from, to, iaex);
      return;
    }
    Object oldEntry = m_multipleTimeRangeMap.put(from, new TimeRange(from, to));
    if (oldEntry != null) {
      throw new ProcessingException("Error: should not have an entry for key: {}", from);
    }
  }

  private void checkParams(Date from, Date to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to must not be null.");
    }
    if (!to.after(from)) {
      throw new IllegalArgumentException("time range must be > 0.");
    }
  }

  /**
   * removes the given time range from the stored ones.
   * <p>
   * calculates overlapping and rearranges correspondingly.
   * <p>
   * restrictions: from and to must not be <code>null</code>. from < to.
   *
   * @param from
   * @param to
   * @return
   * @since 03.02.2006 - tha@bsiag.com
   */
  public Collection remove(Date from, Date to) {
    checkParams(from, to);
    List<TimeRange> removedTimeRanges = new ArrayList<>();
    /* check overlapping */
    SortedMap<Date, TimeRange> headMap = m_multipleTimeRangeMap.headMap(to);
    boolean finished = false;
    while (!finished && !headMap.isEmpty()) {
      final Object tailKey = headMap.lastKey();
      final TimeRange tail = headMap.get(tailKey);
      final Date tailTo = tail.getTo();
      final Date tailFrom = tail.getFrom();

      if (tailTo.after(to) && tailFrom.before(to)) {
        /* tail range overlaps our to-time. we have to replace the tail element. */
        m_multipleTimeRangeMap.remove(tailKey);
        if (tailFrom.before(from)) {
          /* case that tail is bigger then given range on both sides! */
          removedTimeRanges.add(new TimeRange(from, to));
          internalPutNoOldEntry(tailFrom, from);
          internalPutNoOldEntry(to, tailTo);
          finished = true;
        }
        else {
          /* normal overlapping after given range */
          removedTimeRanges.add(new TimeRange(tailFrom, to));
          internalPutNoOldEntry(to, tailTo);
        }
      }
      else {
        /* tail range does not overlap our to-time. */
        if (from.after(tailTo)) {
          finished = true;
        }
        else if (tailFrom.after(from)) {
          removedTimeRanges.add(m_multipleTimeRangeMap.remove(tailKey));
        }
        else {
          /* tail range overlaps our from-time. we have to replace tail element. */
          m_multipleTimeRangeMap.remove(tailKey);
          internalPutNoOldEntry(tailFrom, from);
          removedTimeRanges.add(new TimeRange(from, tailTo));
          finished = true;
        }
      }
    }

    return removedTimeRanges;
  }

  /**
   * timerangeIterator()
   *
   * @since 06.02.2006 - tha@bsiag.com
   * @return an {@link Iterator} containing elememts of type {@link TimeRange}.
   */
  public TimeRange[] getTimeRanges() {
    return m_multipleTimeRangeMap.values().toArray(new TimeRange[m_multipleTimeRangeMap.size()]);
  }

  /**
   * sumDurationOfTimeranges()
   *
   * @since 06.02.2006 - tha@bsiag.com
   */
  public long sumDurationOfTimeranges() {
    long duration = 0;
    for (TimeRange range : getTimeRanges()) {
      duration += range.getTo().getTime() - range.getFrom().getTime();
    }
    return duration;
  }

  public boolean isEmpty() {
    return m_multipleTimeRangeMap.isEmpty();
  }

  /**
   * getBeginDate()
   *
   * @since 06.02.2006 - tha@bsiag.com
   */
  public Date getBeginDate() {
    if (m_multipleTimeRangeMap.isEmpty()) {
      return null;
    }
    return (m_multipleTimeRangeMap.get(m_multipleTimeRangeMap.firstKey())).getFrom();
  }

  /**
   * getEndDate()
   *
   * @since 06.02.2006 - tha@bsiag.com
   */
  public Date getEndDate() {
    if (m_multipleTimeRangeMap.isEmpty()) {
      return null;
    }
    return (m_multipleTimeRangeMap.get(m_multipleTimeRangeMap.lastKey())).getTo();
  }

  public boolean contains(Date representedDate) {
    if (representedDate == null || m_multipleTimeRangeMap.isEmpty()) {
      return false;
    }
    Date firstKeyAfterRepresentedDate = new Date(representedDate.getTime() + 1);
    SortedMap headMap = m_multipleTimeRangeMap.headMap(firstKeyAfterRepresentedDate);
    if (headMap.isEmpty()) {
      return false;
    }
    return (m_multipleTimeRangeMap.get(headMap.lastKey())).contains(representedDate);
  }

  public MultiTimeRange copy() {
    return new MultiTimeRange(this);
  }
}
