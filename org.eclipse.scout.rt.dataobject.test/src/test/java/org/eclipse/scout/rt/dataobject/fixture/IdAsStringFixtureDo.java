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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("IdAsStringFixture")
public class IdAsStringFixtureDo extends DoEntity {

  /**
   * Bad pattern, always use proper types, just used as an example for a data object visitor extension that acts on a
   * data object.
   */
  public DoValue<String> idAsString() {
    return doValue("idAsString");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #idAsString()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public IdAsStringFixtureDo withIdAsString(String idAsString) {
    idAsString().set(idAsString);
    return this;
  }

  /**
   * See {@link #idAsString()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public String getIdAsString() {
    return idAsString().get();
  }
}
