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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * @see org.eclipse.scout.rt.jackson.dataobject.DataObjectSerializers
 */
@TypeName("scout.BinaryResourceValue")
public class BinaryResourceValueDo extends DoEntity implements IValueDo<BinaryResource> {

  public static BinaryResourceValueDo of(BinaryResource value) {
    return BEANS.get(BinaryResourceValueDo.class).withValue(value);
  }

  @Override
  public DoValue<BinaryResource> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BinaryResourceValueDo withValue(BinaryResource value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BinaryResource getValue() {
    return value().get();
  }
}
