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
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;

@TypeName("start.FixtureDataLookupRow")
public class FixtureDataLookupRowDo extends AbstractLookupRowDo<FixtureDataLookupRowDo, Long> {

  @Override
  public DoValue<Long> id() {
    return createIdAttribute(this);
  }

  public DoValue<String> additionalData() {
    return doValue("additionalData");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withAdditionalData(String text) {
    additionalData().set(text);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getAdditionalData() {
    return additionalData().get();
  }
}
