/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ListEntityContributionFixture")
@ContributesTo(EntityFixtureDo.class)
public final class ListEntityContributionFixtureDo extends DoEntity implements IDoEntityContribution {

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoList<EntityFixtureDo> entities() {
    return doList("entities");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ListEntityContributionFixtureDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ListEntityContributionFixtureDo withEntities(Collection<? extends EntityFixtureDo> entities) {
    entities().updateAll(entities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ListEntityContributionFixtureDo withEntities(EntityFixtureDo... entities) {
    entities().updateAll(entities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<EntityFixtureDo> getEntities() {
    return entities().get();
  }
}
