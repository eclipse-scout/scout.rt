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
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.Lorem1Fixture")
public class Lorem1FixtureDo extends AbstractLoremFixtureDo {

  public DoValue<String> value1() {
    return doValue("value1");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public Lorem1FixtureDo withValue1(String value1) {
    value1().set(value1);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getValue1() {
    return value1().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public Lorem1FixtureDo withValue(String value) {
    value().set(value);
    return this;
  }
}
