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
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.platform.Replace;

@Replace
public class TestProjectExample1Do extends TestCoreExample1Do {

  public DoValue<String> nameEx() {
    return doValue("nameEx");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestProjectExample1Do withNameEx(String nameEx) {
    nameEx().set(nameEx);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getNameEx() {
    return nameEx().get();
  }
}
