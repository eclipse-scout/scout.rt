package org.eclipse.scout.rt.jackson.dataobject.fixture;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("sub-pojo")
public class TestSubPojo {
  private String bar;

  public String getBar() {
    return bar;
  }

  public void setBar(String bar) {
    this.bar = bar;
  }
}
