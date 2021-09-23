/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.value;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.dataobject.value.DateTimeValueDo;
import org.eclipse.scout.rt.dataobject.value.DateValueDo;
import org.eclipse.scout.rt.dataobject.value.IValueDo;

@TypeName("ValueDoFixture")
public class ValueDoFixtureDo extends DoEntity {

  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> date() {
    return doValue("date");
  }

  @ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_PATTERN)
  public DoValue<Date> dateTime() {
    return doValue("dateTime");
  }

  public DoValue<DateValueDo> typedDate() {
    return doValue("typedDate");
  }

  public DoValue<DateTimeValueDo> typedDateTime() {
    return doValue("typedDateTime");
  }

  public DoValue<IValueDo<Date>> partlyTypedDate() {
    return doValue("partlyTypedDate");
  }

  public DoList<IValueDo<?>> typedList() {
    return doList("typedList");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withDate(Date date) {
    date().set(date);
    return this;
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public Date getDate() {
    return date().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  public ValueDoFixtureDo withDateTime(Date dateTime) {
    dateTime().set(dateTime);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  public Date getDateTime() {
    return dateTime().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withTypedDate(DateValueDo typedDate) {
    typedDate().set(typedDate);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateValueDo getTypedDate() {
    return typedDate().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withTypedDateTime(DateTimeValueDo typedDateTime) {
    typedDateTime().set(typedDateTime);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateTimeValueDo getTypedDateTime() {
    return typedDateTime().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withPartlyTypedDate(IValueDo<Date> partlyTypedDate) {
    partlyTypedDate().set(partlyTypedDate);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IValueDo<Date> getPartlyTypedDate() {
    return partlyTypedDate().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withTypedList(Collection<? extends IValueDo<?>> typedList) {
    typedList().updateAll(typedList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withTypedList(IValueDo<?>... typedList) {
    typedList().updateAll(typedList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IValueDo<?>> getTypedList() {
    return typedList().get();
  }
}
