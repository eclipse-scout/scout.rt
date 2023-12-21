/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.SerializationResponse")
public class SerializationResponse extends DoEntity {

  public DoValue<IDoEntity> original() {
    return doValue("original");
  }

  public DoValue<String> serialized() {
    return doValue("serialized");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SerializationResponse withOriginal(IDoEntity original) {
    original().set(original);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getOriginal() {
    return original().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SerializationResponse withSerialized(String serialized) {
    serialized().set(serialized);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getSerialized() {
    return serialized().get();
  }
}
