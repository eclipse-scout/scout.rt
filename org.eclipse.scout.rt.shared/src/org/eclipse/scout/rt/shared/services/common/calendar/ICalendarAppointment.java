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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.Serializable;
import java.util.Date;

public interface ICalendarAppointment extends ICalendarItem {

  int STATUS_FREE = 0;
  int STATUS_TENTATIVE = 1;
  int STATUS_BUSY = 2;
  int STATUS_OUTOFOFFICE = 3;

  /**
   * @return the internal person id (as {@link Long} if it is a number)
   * @throws UnsupportedOperationException
   *           if internal person id is not <code>null</code> or a number
   * @deprecated use {@link #getPerson()}. method will be removed in 3.10
   */
  @Deprecated
  Long getPersonId();

  /**
   * @deprecated use {@link #setPerson(Object)}. method will be removed in 3.10
   */
  @Deprecated
  void setPersonId(Long n);

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

  int getBusyStatus();

  void setBusyStatus(int a);

  String[] getRecipientEmail();

  void removeRecipientEmail(String recipientEmail);

  void removeAllRecipientEmail();

  Serializable getExternalKey();

  void setExternalKey(Serializable externalKey);

}
