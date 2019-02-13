/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject.fixture;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("scout.SimpleFixture")
public class SimpleFixtureDo extends DoEntity {

  public DoValue<UUID> id() {
    return doValue("id");
  }

  public DoValue<Date> createDate() {
    return doValue("createDate");
  }

  public DoValue<String> name1() {
    return doValue("name1");
  }

  public DoValue<String> name2() {
    return doValue("name2");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SimpleFixtureDo withId(UUID id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UUID getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SimpleFixtureDo withCreateDate(Date createDate) {
    createDate().set(createDate);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getCreateDate() {
    return createDate().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SimpleFixtureDo withName1(String name1) {
    name1().set(name1);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName1() {
    return name1().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SimpleFixtureDo withName2(String name2) {
    name2().set(name2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName2() {
    return name2().get();
  }
}
