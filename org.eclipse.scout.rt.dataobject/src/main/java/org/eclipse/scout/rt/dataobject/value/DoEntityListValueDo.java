/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.value;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.DoEntityListValue")
public class DoEntityListValueDo extends DoEntity implements IValueDo<List<IDoEntity>> {

  public static DoEntityListValueDo of(IDoEntity value) {
    return BEANS.get(DoEntityListValueDo.class).withValue(value);
  }

  public static DoEntityListValueDo of(IDoEntity... values) {
    return BEANS.get(DoEntityListValueDo.class).withValue(values);
  }

  public static DoEntityListValueDo of(Collection<? extends IDoEntity> values) {
    return BEANS.get(DoEntityListValueDo.class).withValue(values);
  }

  @Override
  public DoList<IDoEntity> value() {
    return doList(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(Collection<? extends IDoEntity> value) {
    value().updateAll(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(IDoEntity... value) {
    value().updateAll(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getValue() {
    return value().get();
  }
}
