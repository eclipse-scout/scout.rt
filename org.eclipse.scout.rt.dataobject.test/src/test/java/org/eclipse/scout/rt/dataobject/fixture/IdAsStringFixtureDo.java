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
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

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
