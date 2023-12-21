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

@TypeName("scout.SerializationRequest")
public class SerializationRequest extends DoEntity {

  public DoValue<IDoEntity> body() {
    return doValue("body");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SerializationRequest withBody(IDoEntity body) {
    body().set(body);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getBody() {
    return body().get();
  }
}
