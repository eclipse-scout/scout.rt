/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.calendar;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.shared.services.common.calendar.HolidayCalendarItemParser;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.IHolidayCalendarService;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For details see {@link HolidayCalendarItemParser}
 */
public class HolidayCalendarService implements IHolidayCalendarService {
  private static final Logger LOG = LoggerFactory.getLogger(HolidayCalendarService.class);

  private final ConcurrentExpiringMap<String/* resourceFileName */, HolidayCalendarItemParser> m_holidayXmlCache = new ConcurrentExpiringMap<>(5, TimeUnit.MINUTES);

  @Override
  public Set<? extends ICalendarItem> getItems(RemoteFile spec, Date minDate, Date maxDate) {
    // load new items
    HolidayCalendarItemParser p = null;
    String key = spec.getPath();
    synchronized (m_holidayXmlCache) {
      p = m_holidayXmlCache.get(key);
      if (p == null) {
        try {
          RemoteFile f = BEANS.get(IRemoteFileService.class).getRemoteFile(spec);
          if (f != null) {
            p = new HolidayCalendarItemParser(f.getDecompressedInputStream(), spec.getPath());
            m_holidayXmlCache.put(key, p);
          }
        }
        catch (Exception e) {
          LOG.warn("parsing remote file: {}", spec, e);
        }
      }
    }
    final Set<? extends ICalendarItem> result;
    if (p != null) {
      result = p.getItems(NlsLocale.get(), minDate, maxDate);
    }
    else {
      result = CollectionUtility.hashSet();
    }
    return result;
  }
}
