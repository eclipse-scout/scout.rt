package org.eclipse.scout.rt.jackson.dataobject.fixture;

import org.eclipse.scout.rt.platform.dataobject.DoValue;

public class TestDoValuePojo {

  private DoValue<String> m_stringValue;

  public DoValue<String> getStringValue() {
    return m_stringValue;
  }

  public void setStringValue(DoValue<String> stringValue) {
    this.m_stringValue = stringValue;
  }
}
