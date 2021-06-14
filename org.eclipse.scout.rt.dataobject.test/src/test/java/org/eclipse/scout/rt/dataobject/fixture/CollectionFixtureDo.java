/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;

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

  public DoList<SimpleFixtureDo> simpleDoList() {
    return doList("simpleDoList");
  }

  public DoSet<SimpleFixtureDo> simpleDoSet() {
    return doSet("simpleDoSet");
  }

  public DoCollection<SimpleFixtureDo> simpleDoCollection() {
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
  public CollectionFixtureDo withSimpleDoList(Collection<? extends SimpleFixtureDo> simpleDoList) {
    simpleDoList().updateAll(simpleDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoList(SimpleFixtureDo... simpleDoList) {
    simpleDoList().updateAll(simpleDoList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<SimpleFixtureDo> getSimpleDoList() {
    return simpleDoList().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoSet(Collection<? extends SimpleFixtureDo> simpleDoSet) {
    simpleDoSet().updateAll(simpleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoSet(SimpleFixtureDo... simpleDoSet) {
    simpleDoSet().updateAll(simpleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<SimpleFixtureDo> getSimpleDoSet() {
    return simpleDoSet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoCollection(Collection<? extends SimpleFixtureDo> simpleDoCollection) {
    simpleDoCollection().updateAll(simpleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoCollection(SimpleFixtureDo... simpleDoCollection) {
    simpleDoCollection().updateAll(simpleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<SimpleFixtureDo> getSimpleDoCollection() {
    return simpleDoCollection().get();
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
