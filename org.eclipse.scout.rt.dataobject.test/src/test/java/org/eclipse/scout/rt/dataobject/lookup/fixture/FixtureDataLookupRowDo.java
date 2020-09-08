/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
