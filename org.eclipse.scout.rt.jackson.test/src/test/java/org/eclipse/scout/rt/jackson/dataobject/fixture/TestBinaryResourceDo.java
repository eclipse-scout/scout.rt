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
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@TypeName("TestBinaryResource")
public class TestBinaryResourceDo extends DoEntity {

  public DoValue<BinaryResource> brDefault() {
    return doValue("brDefault");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestBinaryResourceDo withBrDefault(BinaryResource brDefault) {
    brDefault().set(brDefault);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BinaryResource getBrDefault() {
    return brDefault().get();
  }
}
