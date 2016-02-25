/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
