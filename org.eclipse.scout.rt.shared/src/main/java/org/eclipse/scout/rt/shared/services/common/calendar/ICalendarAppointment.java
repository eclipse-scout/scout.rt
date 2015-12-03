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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.Date;

public interface ICalendarAppointment extends ICalendarItem {

  int STATUS_FREE = 0;
  int STATUS_TENTATIVE = 1;
  int STATUS_BUSY = 2;
  int STATUS_OUTOFOFFICE = 3;
  /**
   * Working-Elsewhere status as supported e.g. in Microsoft Outlook 2013 and above
   */
  int STATUS_WORKING_ELSEWHERE = 4;

  void setPerson(Object person);

  Object getPerson();

  Date getStart();

  void setStart(Date a);

  Date getEnd();

  void setEnd(Date a);

  boolean isFullDay();

  void setFullDay(boolean a);

  String getLocation();

  void setLocation(String a);

  /**
   * One of {@link #STATUS_BUSY}, {@link #STATUS_FREE}, {@link #STATUS_OUTOFOFFICE}, {@link #STATUS_TENTATIVE},
   * {@link #STATUS_WORKING_ELSEWHERE}
   */
  int getBusyStatus();

  /**
   * @param a
   *          One of {@link #STATUS_BUSY}, {@link #STATUS_FREE}, {@link #STATUS_OUTOFOFFICE}, {@link #STATUS_TENTATIVE},
   *          {@link #STATUS_WORKING_ELSEWHERE}
   */
  void setBusyStatus(int a);

  String[] getRecipientEmail();

  void removeRecipientEmail(String recipientEmail);

  void removeAllRecipientEmail();

}
