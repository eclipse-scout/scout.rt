/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * Test {@link DoEntity} for various types of collections using raw {@link IDoEntity} references.
 */
@TypeName("TestCollectionsIDoEntity")
public class TestCollectionsIDoEntityDo extends DoEntity {

  public DoValue<IDoEntity> doEntityAttribute() {
    return doValue("doEntityAttribute");
  }

  public DoValue<List<IDoEntity>> doEntityListAttribute() {
    return doValue("doEntityListAttribute");
  }

  public DoValue<Collection<IDoEntity>> doEntityCollectionAttribute() {
    return doValue("doEntityCollectionAttribute");
  }

  public DoList<IDoEntity> doEntityDoListAttribute() {
    return doList("doEntityDoListAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsIDoEntityDo withDoEntityAttribute(IDoEntity doEntityAttribute) {
    doEntityAttribute().set(doEntityAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getDoEntityAttribute() {
    return doEntityAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsIDoEntityDo withDoEntityListAttribute(List<IDoEntity> doEntityListAttribute) {
    doEntityListAttribute().set(doEntityListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getDoEntityListAttribute() {
    return doEntityListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsIDoEntityDo withDoEntityCollectionAttribute(Collection<IDoEntity> doEntityCollectionAttribute) {
    doEntityCollectionAttribute().set(doEntityCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<IDoEntity> getDoEntityCollectionAttribute() {
    return doEntityCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsIDoEntityDo withDoEntityDoListAttribute(Collection<? extends IDoEntity> doEntityDoListAttribute) {
    doEntityDoListAttribute().updateAll(doEntityDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsIDoEntityDo withDoEntityDoListAttribute(IDoEntity... doEntityDoListAttribute) {
    doEntityDoListAttribute().updateAll(doEntityDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getDoEntityDoListAttribute() {
    return doEntityDoListAttribute().get();
  }
}
