/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject.value;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

/**
 * Note: value has to be (de-)serializable without additional type information.
 */
@TypeName("ObjectValue")
public class ObjectValueDo extends DoEntity implements IValueDo<Object> {

  public static ObjectValueDo of(Object value) {
    return BEANS.get(ObjectValueDo.class).withValue(value);
  }

  @Override
  public DoValue<Object> value() {
    return doValue(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ObjectValueDo withValue(Object value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Object getValue() {
    return value().get();
  }
}
