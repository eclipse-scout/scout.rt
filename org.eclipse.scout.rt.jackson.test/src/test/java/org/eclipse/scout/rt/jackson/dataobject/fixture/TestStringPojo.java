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
