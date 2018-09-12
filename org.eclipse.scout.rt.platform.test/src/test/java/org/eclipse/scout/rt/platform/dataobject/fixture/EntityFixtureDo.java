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
package org.eclipse.scout.rt.platform.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("EntityFixture")
public class EntityFixtureDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoList<OtherEntityFixtureDo> otherEntities() {
    return doList("otherEntities");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(Collection<? extends OtherEntityFixtureDo> otherEntities) {
    otherEntities().updateAll(otherEntities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityFixtureDo withOtherEntities(OtherEntityFixtureDo... otherEntities) {
    otherEntities().updateAll(otherEntities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<OtherEntityFixtureDo> getOtherEntities() {
    return otherEntities().get();
  }
}
