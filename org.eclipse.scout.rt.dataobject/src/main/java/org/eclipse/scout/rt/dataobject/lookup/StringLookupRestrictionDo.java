/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.lookup;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@TypeName("scout.StringLookupRestriction")
public class StringLookupRestrictionDo extends AbstractLookupRestrictionDo<StringLookupRestrictionDo, String> {

  @Override
  public DoList<String> ids() {
    return createIdsAttribute(this);
  }

  /**
   * @return Specified {@code restriction} if not null, otherwise a new {@link StringLookupRestrictionDo} instance.
   */
  public static StringLookupRestrictionDo ensure(StringLookupRestrictionDo restriction) {
    return ObjectUtility.nvl(restriction, BEANS.get(StringLookupRestrictionDo.class));
  }
}
