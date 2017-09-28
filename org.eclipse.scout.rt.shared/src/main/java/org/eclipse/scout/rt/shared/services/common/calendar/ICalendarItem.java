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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.Serializable;
import java.util.Date;

public interface ICalendarItem {

  boolean exists();

  /**
   * mark item as deleted <br>
   * Note: this will not physically delete the item, just set the marker
   */
  void delete();

  long getLastModified();

  void setLastModified(long b);

  String getCssClass();

  void setCssClass(String cssClass);

  /**
   * @return true iff this item covers or intersects the range [minDate,maxDate]
   */
  boolean isIntersecting(Date minDate, Date maxDate);

  String getSubject();

  void setSubject(String a);

  String getBody();

  void setBody(String a);

  /**
   * @return the user id that is the primary owner of this item
   */
  String getOwner();

  /**
   * set the user id that is the primary owner of this item
   */
  void setOwner(String a);

  RecurrencePattern getRecurrencePattern();

  void setRecurrencePattern(RecurrencePattern p);

  void setItemId(Object itemId);

  Object getItemId();

  Serializable getExternalKey();

  void setExternalKey(Serializable externalKey);

  /**
   * @return a detailed description of this item. The description contains the body and other fields depending on the
   *         item-type.
   */
  String getDescription();

}
