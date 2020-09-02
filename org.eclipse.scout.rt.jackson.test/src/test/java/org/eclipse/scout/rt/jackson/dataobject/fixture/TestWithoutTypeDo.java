/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;

public class TestWithoutTypeDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<TestWithoutTypeDo> nested() {
    return doValue("nested");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeDo withNested(TestWithoutTypeDo nested) {
    nested().set(nested);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeDo getNested() {
    return nested().get();
  }
}
