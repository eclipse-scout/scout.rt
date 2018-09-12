/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.dataobject.value.DateTimeValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.DateValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.IValueDo;

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

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withDate(Date date) {
    date().set(date);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDate() {
    return date().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withDateTime(Date dateTime) {
    dateTime().set(dateTime);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
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
    typedList().clear();
    typedList().get().addAll(typedList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ValueDoFixtureDo withTypedList(IValueDo<?>... typedList) {
    return withTypedList(Arrays.asList(typedList));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IValueDo<?>> getTypedList() {
    return typedList().get();
  }
}
