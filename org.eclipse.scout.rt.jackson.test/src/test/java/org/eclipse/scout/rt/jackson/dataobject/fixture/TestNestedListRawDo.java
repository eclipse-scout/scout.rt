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
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestNestedListRaw")
public class TestNestedListRawDo extends DoEntity {

  public DoList<DoEntity> doEntity() {
    return doList("doEntity");
  }

  public DoList<DoEntity> doEntity2() {
    return doList("doEntity2");
  }

  public DoList<IDoEntity> iDoEntity() {
    return doList("iDoEntity");
  }

  public DoList<IDoEntity> iDoEntity2() {
    return doList("iDoEntity2");
  }

  public DoList<IDataObject> iDataObject() {
    return doList("iDataObject");
  }

  public DoList<IDataObject> iDataObject2() {
    return doList("iDataObject2");
  }

  public DoList<ITestTypedUntypedInnerDo> iTestTypedUntypedInner() {
    return doList("iTestTypedUntypedInner");
  }

  public DoList<ITestTypedUntypedInnerDataObjectDo> iTestTypedUntypedInnerDataObject() {
    return doList("iTestTypedUntypedInnerDataObject");
  }

  public DoList<AbstractTestTypedUntypedInnerDo> abstractTestTypedUntypedInner() {
    return doList("abstractTestTypedUntypedInner");
  }

  public DoValue<Map<String, DoEntity>> stringDoEntityMap() {
    return doValue("stringDoEntityMap");
  }

  public DoList<Map<String, IDoEntity>> stringIDoEntityMap() {
    return doList("stringIDoEntityMap");
  }

  public DoList<Map<String, IDataObject>> stringIDataObjectEntityMap() {
    return doList("stringIDataObjectEntityMap");
  }

  public DoList<Map<String, ITestTypedUntypedInnerDo>> stringITestTypedUntypedInnerEntityMap() {
    return doList("stringITestTypedUntypedInnerEntityMap");
  }

  public DoList<Map<String, ITestTypedUntypedInnerDataObjectDo>> stringITestTypedUntypedInnerDataObjectEntityMap() {
    return doList("stringITestTypedUntypedInnerDataObjectEntityMap");
  }

  public DoList<Map<String, AbstractTestTypedUntypedInnerDo>> stringAbstractTestTypedUntypedInnerEntityMap() {
    return doList("stringAbstractTestTypedUntypedInnerEntityMap");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withDoEntity(Collection<? extends DoEntity> doEntity) {
    doEntity().updateAll(doEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withDoEntity(DoEntity... doEntity) {
    doEntity().updateAll(doEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<DoEntity> getDoEntity() {
    return doEntity().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withDoEntity2(Collection<? extends DoEntity> doEntity2) {
    doEntity2().updateAll(doEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withDoEntity2(DoEntity... doEntity2) {
    doEntity2().updateAll(doEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<DoEntity> getDoEntity2() {
    return doEntity2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDoEntity(Collection<? extends IDoEntity> iDoEntity) {
    iDoEntity().updateAll(iDoEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDoEntity(IDoEntity... iDoEntity) {
    iDoEntity().updateAll(iDoEntity);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getIDoEntity() {
    return iDoEntity().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDoEntity2(Collection<? extends IDoEntity> iDoEntity2) {
    iDoEntity2().updateAll(iDoEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDoEntity2(IDoEntity... iDoEntity2) {
    iDoEntity2().updateAll(iDoEntity2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getIDoEntity2() {
    return iDoEntity2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDataObject(Collection<? extends IDataObject> iDataObject) {
    iDataObject().updateAll(iDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDataObject(IDataObject... iDataObject) {
    iDataObject().updateAll(iDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDataObject> getIDataObject() {
    return iDataObject().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDataObject2(Collection<? extends IDataObject> iDataObject2) {
    iDataObject2().updateAll(iDataObject2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withIDataObject2(IDataObject... iDataObject2) {
    iDataObject2().updateAll(iDataObject2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDataObject> getIDataObject2() {
    return iDataObject2().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withITestTypedUntypedInner(Collection<? extends ITestTypedUntypedInnerDo> iTestTypedUntypedInner) {
    iTestTypedUntypedInner().updateAll(iTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withITestTypedUntypedInner(ITestTypedUntypedInnerDo... iTestTypedUntypedInner) {
    iTestTypedUntypedInner().updateAll(iTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ITestTypedUntypedInnerDo> getITestTypedUntypedInner() {
    return iTestTypedUntypedInner().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withITestTypedUntypedInnerDataObject(Collection<? extends ITestTypedUntypedInnerDataObjectDo> iTestTypedUntypedInnerDataObject) {
    iTestTypedUntypedInnerDataObject().updateAll(iTestTypedUntypedInnerDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withITestTypedUntypedInnerDataObject(ITestTypedUntypedInnerDataObjectDo... iTestTypedUntypedInnerDataObject) {
    iTestTypedUntypedInnerDataObject().updateAll(iTestTypedUntypedInnerDataObject);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ITestTypedUntypedInnerDataObjectDo> getITestTypedUntypedInnerDataObject() {
    return iTestTypedUntypedInnerDataObject().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withAbstractTestTypedUntypedInner(Collection<? extends AbstractTestTypedUntypedInnerDo> abstractTestTypedUntypedInner) {
    abstractTestTypedUntypedInner().updateAll(abstractTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withAbstractTestTypedUntypedInner(AbstractTestTypedUntypedInnerDo... abstractTestTypedUntypedInner) {
    abstractTestTypedUntypedInner().updateAll(abstractTestTypedUntypedInner);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractTestTypedUntypedInnerDo> getAbstractTestTypedUntypedInner() {
    return abstractTestTypedUntypedInner().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringDoEntityMap(Map<String, DoEntity> stringDoEntityMap) {
    stringDoEntityMap().set(stringDoEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, DoEntity> getStringDoEntityMap() {
    return stringDoEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringIDoEntityMap(Collection<? extends Map<String, IDoEntity>> stringIDoEntityMap) {
    stringIDoEntityMap().updateAll(stringIDoEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringIDoEntityMap(Map<String, IDoEntity>... stringIDoEntityMap) {
    stringIDoEntityMap().updateAll(stringIDoEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Map<String, IDoEntity>> getStringIDoEntityMap() {
    return stringIDoEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringIDataObjectEntityMap(Collection<? extends Map<String, IDataObject>> stringIDataObjectEntityMap) {
    stringIDataObjectEntityMap().updateAll(stringIDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringIDataObjectEntityMap(Map<String, IDataObject>... stringIDataObjectEntityMap) {
    stringIDataObjectEntityMap().updateAll(stringIDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Map<String, IDataObject>> getStringIDataObjectEntityMap() {
    return stringIDataObjectEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringITestTypedUntypedInnerEntityMap(Collection<? extends Map<String, ITestTypedUntypedInnerDo>> stringITestTypedUntypedInnerEntityMap) {
    stringITestTypedUntypedInnerEntityMap().updateAll(stringITestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringITestTypedUntypedInnerEntityMap(Map<String, ITestTypedUntypedInnerDo>... stringITestTypedUntypedInnerEntityMap) {
    stringITestTypedUntypedInnerEntityMap().updateAll(stringITestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Map<String, ITestTypedUntypedInnerDo>> getStringITestTypedUntypedInnerEntityMap() {
    return stringITestTypedUntypedInnerEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringITestTypedUntypedInnerDataObjectEntityMap(Collection<? extends Map<String, ITestTypedUntypedInnerDataObjectDo>> stringITestTypedUntypedInnerDataObjectEntityMap) {
    stringITestTypedUntypedInnerDataObjectEntityMap().updateAll(stringITestTypedUntypedInnerDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringITestTypedUntypedInnerDataObjectEntityMap(Map<String, ITestTypedUntypedInnerDataObjectDo>... stringITestTypedUntypedInnerDataObjectEntityMap) {
    stringITestTypedUntypedInnerDataObjectEntityMap().updateAll(stringITestTypedUntypedInnerDataObjectEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Map<String, ITestTypedUntypedInnerDataObjectDo>> getStringITestTypedUntypedInnerDataObjectEntityMap() {
    return stringITestTypedUntypedInnerDataObjectEntityMap().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringAbstractTestTypedUntypedInnerEntityMap(Collection<? extends Map<String, AbstractTestTypedUntypedInnerDo>> stringAbstractTestTypedUntypedInnerEntityMap) {
    stringAbstractTestTypedUntypedInnerEntityMap().updateAll(stringAbstractTestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestNestedListRawDo withStringAbstractTestTypedUntypedInnerEntityMap(Map<String, AbstractTestTypedUntypedInnerDo>... stringAbstractTestTypedUntypedInnerEntityMap) {
    stringAbstractTestTypedUntypedInnerEntityMap().updateAll(stringAbstractTestTypedUntypedInnerEntityMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Map<String, AbstractTestTypedUntypedInnerDo>> getStringAbstractTestTypedUntypedInnerEntityMap() {
    return stringAbstractTestTypedUntypedInnerEntityMap().get();
  }
}
