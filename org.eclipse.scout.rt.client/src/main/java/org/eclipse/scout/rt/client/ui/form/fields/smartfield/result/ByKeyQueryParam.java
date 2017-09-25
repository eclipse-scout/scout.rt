package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

public class ByKeyQueryParam<T> implements IQueryParam {

  private final T m_key;

  public ByKeyQueryParam(T key) {
    this.m_key = key;
  }

  public T getKey() {
    return m_key;
  }

}
