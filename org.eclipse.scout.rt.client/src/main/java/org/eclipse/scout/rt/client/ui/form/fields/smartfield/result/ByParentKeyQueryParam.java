package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

public class ByParentKeyQueryParam<T> implements IQueryParam {

  private final T m_parentKey;

  public ByParentKeyQueryParam(T parentKey) {
    this.m_parentKey = parentKey;
  }

  public T getParentKey() {
    return m_parentKey;
  }
}
