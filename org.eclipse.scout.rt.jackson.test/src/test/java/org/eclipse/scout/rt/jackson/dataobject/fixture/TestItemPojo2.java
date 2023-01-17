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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Entity object with same fields as {@link TestItemDo} but using POJO style getter/setter and plain jackson features.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("TestItem2")
public class TestItemPojo2 {

  private String id;
  private String stringAttribute;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStringAttribute() {
    return stringAttribute;
  }

  public void setStringAttribute(String stringAttribute) {
    this.stringAttribute = stringAttribute;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((stringAttribute == null) ? 0 : stringAttribute.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TestItemPojo2 other = (TestItemPojo2) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (stringAttribute == null) {
      if (other.stringAttribute != null) {
        return false;
      }
    }
    else if (!stringAttribute.equals(other.stringAttribute)) {
      return false;
    }
    return true;
  }
}
