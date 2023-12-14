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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("EntityMapperContributionFixture")
@ContributesTo(EntityMapperFixtureDo.class)
public class EntityMapperContributionFixtureDo extends DoEntity implements IDoEntityContribution {

  public DoValue<Long> contributedValue() {
    return doValue("contributedValue");
  }

  public DoValue<Long> contributedSubPeerValue() {
    return doValue("contributedSubPeerValue");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperContributionFixtureDo withContributedValue(Long contributedValue) {
    contributedValue().set(contributedValue);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getContributedValue() {
    return contributedValue().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EntityMapperContributionFixtureDo withContributedSubPeerValue(Long contributedSubPeerValue) {
    contributedSubPeerValue().set(contributedSubPeerValue);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getContributedSubPeerValue() {
    return contributedSubPeerValue().get();
  }
}
