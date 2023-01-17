/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.index;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class Person {
  private long m_id;
  private String m_name;
  private int m_age;
  private Set<String> m_cars = new HashSet<>();

  public long getId() {
    return m_id;
  }

  public Person withId(long id) {
    m_id = id;
    return this;
  }

  public String getName() {
    return m_name;
  }

  public Person withName(String name) {
    m_name = name;
    return this;
  }

  public int getAge() {
    return m_age;
  }

  public Person withAge(int age) {
    m_age = age;
    return this;
  }

  public Set<String> getCars() {
    return m_cars;
  }

  public Person withCar(String car) {
    m_cars.add(car);
    return this;
  }

  public Person withoutCar(String car) {
    m_cars.remove(car);
    return this;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("id", m_id);
    builder.attr("name", m_name);
    builder.attr("age", m_age);
    builder.attr("cars", m_cars);
    return builder.toString();
  }
}
