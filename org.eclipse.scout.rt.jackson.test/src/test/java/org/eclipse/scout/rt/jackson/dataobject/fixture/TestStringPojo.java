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
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"string", "string2"})
public class TestStringPojo {

  private String m_string;
  private String m_string2;

  public String getString() {
    return m_string;
  }

  public String getString2() {
    return m_string2;
  }

  @JsonProperty("string")
  public TestStringPojo withString(String string) {
    m_string = string;
    return this;
  }

  public void setString2(String string2) {
    m_string2 = string2;
  }
}
