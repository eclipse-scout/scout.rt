/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestWithoutTypeNameSubclassWithTypename")
public class TestWithoutTypeNameSubclassWithTypeNameDo extends TestWithoutTypeNameDo {

  public DoValue<String> idSub() {
    return doValue("idSub");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassWithTypeNameDo withIdSub(String idSub) {
    idSub().set(idSub);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIdSub() {
    return idSub().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassWithTypeNameDo withId(String id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameSubclassWithTypeNameDo withValue(String value) {
    value().set(value);
    return this;
  }
}
