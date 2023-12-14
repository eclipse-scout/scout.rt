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
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.CollectionFixture")
public class CollectionFixtureDo extends DoEntity {

  public DoList<String> stringDoList() {
    return doList("stringDoList");
  }

  public DoSet<String> stringDoSet() {
    return doSet("stringDoSet");
  }

  public DoCollection<String> stringDoCollection() {
    return doCollection("stringDoCollection");
  }

  public DoList<TestCoreExample1Do> exampleDoList() {
    return doList("simpleDoList");
  }

  public DoSet<TestCoreExample1Do> exampleDoSet() {
    return doSet("simpleDoSet");
  }

  public DoCollection<TestCoreExample1Do> exampleDoCollection() {
    return doCollection("simpleDoCollection");
  }

  public DoCollection<AnotherCollectionFixtureDo> anotherDoCollection() {
    return doCollection("anotherDoCollection");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoList(Collection<? extends String> stringDoList) {
    stringDoList().updateAll(stringDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoList(String... stringDoList) {
    stringDoList().updateAll(stringDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringDoList() {
    return stringDoList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoSet(Collection<? extends String> stringDoSet) {
    stringDoSet().updateAll(stringDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoSet(String... stringDoSet) {
    stringDoSet().updateAll(stringDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<String> getStringDoSet() {
    return stringDoSet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoCollection(Collection<? extends String> stringDoCollection) {
    stringDoCollection().updateAll(stringDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withStringDoCollection(String... stringDoCollection) {
    stringDoCollection().updateAll(stringDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<String> getStringDoCollection() {
    return stringDoCollection().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoList(Collection<? extends TestCoreExample1Do> exampleDoList) {
    exampleDoList().updateAll(exampleDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoList(TestCoreExample1Do... exampleDoList) {
    exampleDoList().updateAll(exampleDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestCoreExample1Do> getExampleDoList() {
    return exampleDoList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoSet(Collection<? extends TestCoreExample1Do> exampleDoSet) {
    exampleDoSet().updateAll(exampleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoSet(TestCoreExample1Do... exampleDoSet) {
    exampleDoSet().updateAll(exampleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestCoreExample1Do> getExampleDoSet() {
    return exampleDoSet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoCollection(Collection<? extends TestCoreExample1Do> exampleDoCollection) {
    exampleDoCollection().updateAll(exampleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withExampleDoCollection(TestCoreExample1Do... exampleDoCollection) {
    exampleDoCollection().updateAll(exampleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestCoreExample1Do> getExampleDoCollection() {
    return exampleDoCollection().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withAnotherDoCollection(Collection<? extends AnotherCollectionFixtureDo> anotherDoCollection) {
    anotherDoCollection().updateAll(anotherDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withAnotherDoCollection(AnotherCollectionFixtureDo... anotherDoCollection) {
    anotherDoCollection().updateAll(anotherDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<AnotherCollectionFixtureDo> getAnotherDoCollection() {
    return anotherDoCollection().get();
  }
}
