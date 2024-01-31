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

import java.util.Map;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

import jakarta.annotation.Generated;

@TypeName("TestNestedRaw")
public class TestNestedRawDo extends DoEntity {

  public DoValue<DoEntity> doEntity() {
    return doValue("doEntity");
  }

  public DoValue<DoEntity> doEntity2() {
    return doValue("doEntity2");
  }

  public DoValue<IDoEntity> iDoEntity() {
    return doValue("iDoEntity");
  }

  public DoValue<IDoEntity> iDoEntity2() {
    return doValue("iDoEntity2");
  }

  public DoValue<IDataObject> iDataObject() {
    return doValue("iDataObject");
  }

  public DoValue<IDataObject> iDataObject2() {
    return doValue("iDataObject2");
  }

  public DoValue<ITestTypedUntypedInnerDo> iTestTypedUntypedInner() {
    return doValue("iTestTypedUntypedInner");
  }

  public DoValue<ITestTypedUntypedInnerDataObjectDo> iTestTypedUntypedInnerDataObject() {
    return doValue("iTestTypedUntypedInnerDataObject");
  }

  public DoValue<AbstractTestTypedUntypedInnerDo> abstractTestTypedUntypedInner() {
    return doValue("abstractTestTypedUntypedInner");
  }

  public DoValue<Map<String, DoEntity>> stringDoEntityMap() {
    return doValue("stringDoEntityMap");
  }

  public DoValue<Map<String, IDoEntity>> stringIDoEntityMap() {
    return doValue("stringIDoEntityMap");
  }

  public DoValue<Map<String, IDataObject>> stringIDataObjectEntityMap() {
    return doValue("stringIDataObjectEntityMap");
  }

  public DoValue<Map<String, ITestTypedUntypedInnerDo>> stringITestTypedUntypedInnerEntityMap() {
    return doValue("stringITestTypedUntypedInnerEntityMap");
  }

  public DoValue<Map<String, ITestTypedUntypedInnerDataObjectDo>> stringITestTypedUntypedInnerDataObjectEntityMap() {
    return doValue("stringITestTypedUntypedInnerDataObjectEntityMap");
  }

  public DoValue<Map<String, AbstractTestTypedUntypedInnerDo>> stringAbstractTestTypedUntypedInnerEntityMap() {
    return doValue("stringAbstractTestTypedUntypedInnerEntityMap");
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
  public TestNestedRawDo withDoEntity2(DoEntity doEntity2) {
    doEntity2().set(doEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntity getDoEntity2() {
    return doEntity2().get();
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
  public TestNestedRawDo withIDoEntity2(IDoEntity iDoEntity2) {
    iDoEntity2().set(iDoEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getIDoEntity2() {
    return iDoEntity2().get();
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

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withIDataObject2(IDataObject iDataObject2) {
    iDataObject2().set(iDataObject2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDataObject getIDataObject2() {
    return iDataObject2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withITestTypedUntypedInner(ITestTypedUntypedInnerDo iTestTypedUntypedInner) {
    iTestTypedUntypedInner().set(iTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ITestTypedUntypedInnerDo getITestTypedUntypedInner() {
    return iTestTypedUntypedInner().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withITestTypedUntypedInnerDataObject(ITestTypedUntypedInnerDataObjectDo iTestTypedUntypedInnerDataObject) {
    iTestTypedUntypedInnerDataObject().set(iTestTypedUntypedInnerDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ITestTypedUntypedInnerDataObjectDo getITestTypedUntypedInnerDataObject() {
    return iTestTypedUntypedInnerDataObject().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withAbstractTestTypedUntypedInner(AbstractTestTypedUntypedInnerDo abstractTestTypedUntypedInner) {
    abstractTestTypedUntypedInner().set(abstractTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractTestTypedUntypedInnerDo getAbstractTestTypedUntypedInner() {
    return abstractTestTypedUntypedInner().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringDoEntityMap(Map<String, DoEntity> stringDoEntityMap) {
    stringDoEntityMap().set(stringDoEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, DoEntity> getStringDoEntityMap() {
    return stringDoEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringIDoEntityMap(Map<String, IDoEntity> stringIDoEntityMap) {
    stringIDoEntityMap().set(stringIDoEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, IDoEntity> getStringIDoEntityMap() {
    return stringIDoEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringIDataObjectEntityMap(Map<String, IDataObject> stringIDataObjectEntityMap) {
    stringIDataObjectEntityMap().set(stringIDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, IDataObject> getStringIDataObjectEntityMap() {
    return stringIDataObjectEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringITestTypedUntypedInnerEntityMap(Map<String, ITestTypedUntypedInnerDo> stringITestTypedUntypedInnerEntityMap) {
    stringITestTypedUntypedInnerEntityMap().set(stringITestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, ITestTypedUntypedInnerDo> getStringITestTypedUntypedInnerEntityMap() {
    return stringITestTypedUntypedInnerEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringITestTypedUntypedInnerDataObjectEntityMap(Map<String, ITestTypedUntypedInnerDataObjectDo> stringITestTypedUntypedInnerDataObjectEntityMap) {
    stringITestTypedUntypedInnerDataObjectEntityMap().set(stringITestTypedUntypedInnerDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, ITestTypedUntypedInnerDataObjectDo> getStringITestTypedUntypedInnerDataObjectEntityMap() {
    return stringITestTypedUntypedInnerDataObjectEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedRawDo withStringAbstractTestTypedUntypedInnerEntityMap(Map<String, AbstractTestTypedUntypedInnerDo> stringAbstractTestTypedUntypedInnerEntityMap) {
    stringAbstractTestTypedUntypedInnerEntityMap().set(stringAbstractTestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, AbstractTestTypedUntypedInnerDo> getStringAbstractTestTypedUntypedInnerEntityMap() {
    return stringAbstractTestTypedUntypedInnerEntityMap().get();
  }
}
