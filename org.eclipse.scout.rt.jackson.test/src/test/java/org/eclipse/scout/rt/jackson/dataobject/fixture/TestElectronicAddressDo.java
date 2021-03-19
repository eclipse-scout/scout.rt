/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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

@TypeName("TestElectronicAddress")
public class TestElectronicAddressDo extends AbstractTestAddressDo {

  public DoValue<String> email() {
    return doValue("email");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestElectronicAddressDo withEmail(String email) {
    email().set(email);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getEmail() {
    return email().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestElectronicAddressDo withId(String id) {
    id().set(id);
    return this;
  }
}
