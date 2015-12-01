/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.index.AbstractMultiValueIndex;
import org.eclipse.scout.rt.platform.index.AbstractSingleValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

public class IndexedStoreTest {

  private PersonStore m_store;

  private Person m_john1;
  private Person m_anna;
  private Person m_maria;
  private Person m_john2;
  private Person m_frank;
  private Person m_tom;

  @Before
  public void before() {
    m_store = new PersonStore();

    m_store.add(m_john1 = new Person().withId(1).withName("john").withAge(35));
    m_store.add(m_anna = new Person().withId(2).withName("anna").withAge(25));
    m_store.add(m_maria = new Person().withId(3).withName("maria").withAge(80));
    m_store.add(m_john2 = new Person().withId(4).withName("john").withAge(75));
    m_store.add(m_frank = new Person().withId(5).withName("frank").withAge(50));
    m_store.add(m_tom = new Person().withId(6).withName("tom").withAge(35));
  }

  @Test
  public void testSingleValueIndex() {
    assertSame(m_john1, m_store.getById(1));
    assertSame(m_anna, m_store.getById(2));
    assertSame(m_maria, m_store.getById(3));
    assertSame(m_john2, m_store.getById(4));
    assertSame(m_frank, m_store.getById(5));
    assertSame(m_tom, m_store.getById(6));
    assertNull(m_store.getById(7));

  }

  @Test
  public void testMultiValueIndex() {
    assertEquals(CollectionUtility.arrayList(m_john1, m_john2), m_store.getByName("john"));
    assertEquals(CollectionUtility.arrayList(m_anna), m_store.getByName("anna"));
    assertEquals(CollectionUtility.arrayList(m_maria), m_store.getByName("maria"));
    assertEquals(CollectionUtility.arrayList(m_frank), m_store.getByName("frank"));
    assertEquals(CollectionUtility.arrayList(m_tom), m_store.getByName("tom"));

    assertEquals(CollectionUtility.arrayList(m_maria, m_john2), m_store.getRetiredPersons());
  }

  @Test
  public void testRegisterIndex() {
    AbstractMultiValueIndex<Integer, Person> ageIndex = new AbstractMultiValueIndex<Integer, Person>() {

      @Override
      protected Integer calculateIndexFor(Person person) {
        return person.getAge();
      }
    };

    m_store.registerIndex(ageIndex);

    assertEquals(CollectionUtility.arrayList(m_john1, m_tom), ageIndex.get(35));
    assertEquals(CollectionUtility.arrayList(m_anna), ageIndex.get(25));
    assertEquals(CollectionUtility.arrayList(m_maria), ageIndex.get(80));
    assertEquals(CollectionUtility.arrayList(m_john2), ageIndex.get(75));
    assertEquals(CollectionUtility.arrayList(m_frank), ageIndex.get(50));
  }

  @Test
  public void testValues() {
    assertEquals(CollectionUtility.arrayList(m_john1, m_anna, m_maria, m_john2, m_frank, m_tom), m_store.values());
  }

  @Test
  public void testIndexValues() {
    assertEquals(CollectionUtility.hashSet("john", "anna", "maria", "frank", "tom"), m_store.getNames());
    assertEquals(CollectionUtility.hashSet(1L, 2L, 3L, 4L, 5L, 6L), m_store.getIds());
  }

  @Test
  public void testReplace() {
    AbstractMultiValueIndex<Integer, Person> ageIndex = new AbstractMultiValueIndex<Integer, Person>() {

      @Override
      protected Integer calculateIndexFor(Person person) {
        return person.getAge();
      }
    };
    m_store.registerIndex(ageIndex);
    assertEquals(CollectionUtility.arrayList(m_maria), ageIndex.get(80));

    m_maria.withAge(81); // was registered with a age of 80 years
    assertEquals(CollectionUtility.arrayList(m_maria), ageIndex.get(80));
    assertTrue(ageIndex.get(81).isEmpty());

    m_store.add(m_maria);
    assertTrue(ageIndex.get(80).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_maria), ageIndex.get(81));
  }

  @Test
  public void testRemove() {
    assertEquals(CollectionUtility.arrayList(m_anna), m_store.getByName("anna"));
    m_store.remove(m_anna);
    assertTrue(m_store.getByName("anna").isEmpty());
  }

  public class PersonStore extends IndexedStore<Person> {

    private final PersonIdIndex IDX_PERSON_ID = registerIndex(new PersonIdIndex());
    private final PersonNameIndex IDX_PERSON_NAME = registerIndex(new PersonNameIndex());
    private final PersonRetiredIndex IDX_PERSON_RETIRED = registerIndex(new PersonRetiredIndex());

    public Person getById(long id) {
      return IDX_PERSON_ID.get(id);
    }

    public List<Person> getByName(String name) {
      return IDX_PERSON_NAME.get(name);
    }

    public Set<String> getNames() {
      return IDX_PERSON_NAME.indexValues();
    }

    public Set<Long> getIds() {
      return IDX_PERSON_ID.indexValues();
    }

    public List<Person> getRetiredPersons() {
      return IDX_PERSON_RETIRED.get(Boolean.TRUE);
    }

    private class PersonIdIndex extends AbstractSingleValueIndex<Long, Person> {

      @Override
      protected Long calculateIndexFor(Person person) {
        return person.getId();
      }
    }

    private class PersonNameIndex extends AbstractMultiValueIndex<String, Person> {

      @Override
      protected String calculateIndexFor(Person person) {
        return person.getName();
      }
    }

    private class PersonRetiredIndex extends AbstractMultiValueIndex<Boolean, Person> {

      @Override
      protected Boolean calculateIndexFor(Person person) {
        return person.getAge() > 65;
      }
    }
  }
}
