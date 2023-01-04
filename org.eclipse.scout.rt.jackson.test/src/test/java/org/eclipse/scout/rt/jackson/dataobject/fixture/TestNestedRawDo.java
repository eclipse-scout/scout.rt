/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestNestedRaw")
public class TestNestedRawDo extends DoEntity {

  public DoValue<DoEntity> doEntity() {
    return doValue("doEntity");
  }

  public DoValue<IDoEntity> iDoEntity() {
    return doValue("iDoEntity");
  }

  public DoValue<IDataObject> iDataObject() {
    return doValue("iDataObject");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withDoEntity(DoEntity doEntity) {
    doEntity().set(doEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntity getDoEntity() {
    return doEntity().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withIDoEntity(IDoEntity iDoEntity) {
    iDoEntity().set(iDoEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getIDoEntity() {
    return iDoEntity().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withIDataObject(IDataObject iDataObject) {
    iDataObject().set(iDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDataObject getIDataObject() {
    return iDataObject().get();
  }
}
