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
package org.eclipse.scout.rt.platform.dataobject.value;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * @see org.eclipse.scout.rt.jackson.dataobject.DataObjectSerializers
 */
@TypeName("BinaryResourceValue")
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
