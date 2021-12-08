/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

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
