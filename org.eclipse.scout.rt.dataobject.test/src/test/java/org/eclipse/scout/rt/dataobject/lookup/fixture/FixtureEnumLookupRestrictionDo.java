/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;

@TypeName("start.FixtureEnumLookupRestriction")
public class FixtureEnumLookupRestrictionDo extends AbstractLookupRestrictionDo<FixtureEnumLookupRestrictionDo, FixtureEnum> {

  @Override
  public DoList<FixtureEnum> ids() {
    return createIdsAttribute(this);
  }
}
