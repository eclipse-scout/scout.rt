/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.DataObjectSignature")
public class DataObjectSignatureDo extends DoEntity {

  public DoList<EntityDataObjectSignatureDo> entities() {
    return doList("entities");
  }

  public DoList<EnumApiSignatureDo> enums() {
    return doList("enums");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectSignatureDo withEntities(Collection<? extends EntityDataObjectSignatureDo> entities) {
    entities().updateAll(entities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectSignatureDo withEntities(EntityDataObjectSignatureDo... entities) {
    entities().updateAll(entities);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<EntityDataObjectSignatureDo> getEntities() {
    return entities().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectSignatureDo withEnums(Collection<? extends EnumApiSignatureDo> enums) {
    enums().updateAll(enums);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectSignatureDo withEnums(EnumApiSignatureDo... enums) {
    enums().updateAll(enums);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<EnumApiSignatureDo> getEnums() {
    return enums().get();
  }
}
