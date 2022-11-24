/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.id.TypedId;

@TypeName("DataObjectWithTypedId")
public class DataObjectWithTypedIdDo extends DoEntity {

  public DoValue<TypedId<FixtureStringId>> id() {
    return doValue("id");
  }

  public DoList<TypedId<FixtureStringId>> ids() {
    return doList("ids");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithTypedIdDo withId(TypedId<FixtureStringId> id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TypedId<FixtureStringId> getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithTypedIdDo withIds(Collection<? extends TypedId<FixtureStringId>> ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithTypedIdDo withIds(TypedId<FixtureStringId>... ids) {
    ids().updateAll(ids);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TypedId<FixtureStringId>> getIds() {
    return ids().get();
  }
}
