/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;

@TypeName("start.FixtureDataLookupRestriction")
public class FixtureDataLookupRestrictionDo extends AbstractLookupRestrictionDo<FixtureDataLookupRestrictionDo, Long> {

  @Override
  public DoList<Long> ids() {
    return createIdsAttribute(this);
  }

  public DoValue<String> startsWith() {
    return doValue("startsWith");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRestrictionDo withStartsWith(String startsWith) {
    startsWith().set(startsWith);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStartsWith() {
    return startsWith().get();
  }
}
