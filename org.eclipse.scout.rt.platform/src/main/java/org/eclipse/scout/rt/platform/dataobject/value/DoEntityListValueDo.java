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
package org.eclipse.scout.rt.platform.dataobject.value;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("DoEntityListValue")
public class DoEntityListValueDo extends DoEntity implements IValueDo<List<IDoEntity>> {

  public static DoEntityListValueDo of(IDoEntity value) {
    return BEANS.get(DoEntityListValueDo.class).withValue(value);
  }

  @Override
  public DoList<IDoEntity> value() {
    return doList(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(Collection<? extends IDoEntity> value) {
    value().updateAll(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(IDoEntity... value) {
    value().updateAll(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getValue() {
    return value().get();
  }
}
