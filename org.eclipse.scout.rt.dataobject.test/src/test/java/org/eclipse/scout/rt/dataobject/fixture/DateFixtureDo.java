/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;

@TypeName("DateFixture")
public class DateFixtureDo extends DoEntity {

  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> date() {
    return doValue("date");
  }

  @AttributeName("list")
  public DoList<Integer> _list() {
    return doList("list");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DateFixtureDo withDate(Date date) {
    date().set(date);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getDate() {
    return date().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateFixtureDo with_list(Collection<? extends Integer> _list) {
    _list().updateAll(_list);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DateFixtureDo with_list(Integer... _list) {
    _list().updateAll(_list);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Integer> get_list() {
    return _list().get();
  }
}
