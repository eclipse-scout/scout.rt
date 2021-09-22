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
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;

@TypeName("TestDate")
public class TestDateDo extends DoEntity {

  public DoValue<Date> dateDefault() {
    return doValue("dateDefault");
  }

  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> dateOnly() {
    return doValue("dateOnly");
  }

  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoList<Date> dateOnlyDoList() {
    return doList("dateOnlyDoList");
  }

  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<List<Date>> dateOnlyList() {
    return doValue("dateOnlyList");
  }

  @ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_PATTERN)
  public DoValue<Date> dateWithTimestamp() {
    return doValue("dateWithTimestamp");
  }

  @ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN)
  public DoValue<Date> dateWithTimestampWithTimezone() {
    return doValue("dateWithTimestampWithTimezone");
  }

  @ValueFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // timestamp with UTC/Zulu timezone, without white spaces
  public DoValue<Date> dateWithTimestampZulu() {
    return doValue("dateWithTimestampZulu");
  }

  @ValueFormat(pattern = "yy-MM")
  public DoValue<Date> dateYearMonth() {
    return doValue("dateYearMonth");
  }

  @ValueFormat(pattern = "abcd")
  public DoValue<Date> invalidDateFormat() {
    return doValue("invalidDateFormat");
  }

  @ValueFormat(pattern = "yyyyMMdd")
  @AttributeName("customDateFormatCustomAttributeName")
  public DoValue<Date> customDateFormat() {
    return doValue("customDateFormatCustomAttributeName");
  }

  @ValueFormat(pattern = "yyyyMMddHHmm")
  public DoList<Date> customDateDoList() {
    return doList("customDateDoList");
  }

  public DoValue<Set<String>> aDummySet() {
    return doValue("aDummySet");
  }

  public DoValue<Set<String>> zDummySet() {
    return doValue("zDummySet");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateDefault(Date dateDefault) {
    dateDefault().set(dateDefault);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateDefault() {
    return dateDefault().get();
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateOnly(Date dateOnly) {
    dateOnly().set(dateOnly);
    return this;
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateOnly() {
    return dateOnly().get();
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateOnlyDoList(Collection<? extends Date> dateOnlyDoList) {
    dateOnlyDoList().updateAll(dateOnlyDoList);
    return this;
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateOnlyDoList(Date... dateOnlyDoList) {
    dateOnlyDoList().updateAll(dateOnlyDoList);
    return this;
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public List<Date> getDateOnlyDoList() {
    return dateOnlyDoList().get();
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateOnlyList(List<Date> dateOnlyList) {
    dateOnlyList().set(dateOnlyList);
    return this;
  }

  @ValueFormat(pattern = "yyyy-MM-dd")
  @Generated("DoConvenienceMethodsGenerator")
  public List<Date> getDateOnlyList() {
    return dateOnlyList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  public TestDateDo withDateWithTimestamp(Date dateWithTimestamp) {
    dateWithTimestamp().set(dateWithTimestamp);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
  public Date getDateWithTimestamp() {
    return dateWithTimestamp().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
  public TestDateDo withDateWithTimestampWithTimezone(Date dateWithTimestampWithTimezone) {
    dateWithTimestampWithTimezone().set(dateWithTimestampWithTimezone);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
  public Date getDateWithTimestampWithTimezone() {
    return dateWithTimestampWithTimezone().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  public TestDateDo withDateWithTimestampZulu(Date dateWithTimestampZulu) {
    dateWithTimestampZulu().set(dateWithTimestampZulu);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  @ValueFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  public Date getDateWithTimestampZulu() {
    return dateWithTimestampZulu().get();
  }

  @ValueFormat(pattern = "yy-MM")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withDateYearMonth(Date dateYearMonth) {
    dateYearMonth().set(dateYearMonth);
    return this;
  }

  @ValueFormat(pattern = "yy-MM")
  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateYearMonth() {
    return dateYearMonth().get();
  }

  @ValueFormat(pattern = "abcd")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withInvalidDateFormat(Date invalidDateFormat) {
    invalidDateFormat().set(invalidDateFormat);
    return this;
  }

  @ValueFormat(pattern = "abcd")
  @Generated("DoConvenienceMethodsGenerator")
  public Date getInvalidDateFormat() {
    return invalidDateFormat().get();
  }

  @ValueFormat(pattern = "yyyyMMdd")
  @Generated("DoConvenienceMethodsGenerator")
  @AttributeName("customDateFormatCustomAttributeName")
  public TestDateDo withCustomDateFormat(Date customDateFormat) {
    customDateFormat().set(customDateFormat);
    return this;
  }

  @ValueFormat(pattern = "yyyyMMdd")
  @Generated("DoConvenienceMethodsGenerator")
  @AttributeName("customDateFormatCustomAttributeName")
  public Date getCustomDateFormat() {
    return customDateFormat().get();
  }

  @ValueFormat(pattern = "yyyyMMddHHmm")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withCustomDateDoList(Collection<? extends Date> customDateDoList) {
    customDateDoList().updateAll(customDateDoList);
    return this;
  }

  @ValueFormat(pattern = "yyyyMMddHHmm")
  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withCustomDateDoList(Date... customDateDoList) {
    customDateDoList().updateAll(customDateDoList);
    return this;
  }

  @ValueFormat(pattern = "yyyyMMddHHmm")
  @Generated("DoConvenienceMethodsGenerator")
  public List<Date> getCustomDateDoList() {
    return customDateDoList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withADummySet(Set<String> aDummySet) {
    aDummySet().set(aDummySet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getADummySet() {
    return aDummySet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDateDo withZDummySet(Set<String> zDummySet) {
    zDummySet().set(zDummySet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getZDummySet() {
    return zDummySet().get();
  }
}
