/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;

@TypeName("start.FixtureEnumLookupRow")
public class FixtureEnumLookupRowDo extends AbstractLookupRowDo<FixtureEnumLookupRowDo, FixtureEnum> {

  @Override
  public DoValue<FixtureEnum> id() {
    return createIdAttribute(this);
  }
}
