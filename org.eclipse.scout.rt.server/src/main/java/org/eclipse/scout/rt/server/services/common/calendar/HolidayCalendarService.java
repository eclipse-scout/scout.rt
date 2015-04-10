/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.calendar;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.shared.services.common.calendar.HolidayCalendarItemParser;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.IHolidayCalendarService;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * For details see {@link HolidayCalendarItemParser}
 */
@Priority(-1)
public class HolidayCalendarService extends AbstractService implements IHolidayCalendarService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HolidayCalendarService.class);

  private TTLCache<String/* resourceFileName */, HolidayCalendarItemParser> m_holidayXmlCache = new TTLCache<String, HolidayCalendarItemParser>(TimeUnit.MINUTES.toMillis(5));// 5 minutes

  @Override
  public Set<? extends ICalendarItem> getItems(RemoteFile spec, Date minDate, Date maxDate) throws ProcessingException {
    // load new items
    HolidayCalendarItemParser p = null;
    String key = spec.getPath();
    synchronized (m_holidayXmlCache) {
      if (m_holidayXmlCache.containsKey(key)) {
        p = m_holidayXmlCache.get(key);
      }
      else {
        RemoteFile f = null;
        try {
          f = BEANS.get(IRemoteFileService.class).getRemoteFile(spec);
          if (f != null) {
            p = new HolidayCalendarItemParser(f.getDecompressedInputStream(), spec.getPath());
            m_holidayXmlCache.put(key, p);
          }
        }
        catch (Exception e) {
          LOG.warn("parsing remote file:" + spec, e);
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
