package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

public class QueryParam<T> implements IQueryParam {

  private QueryBy m_queryBy;

  private String m_text;

  private T m_key;

  QueryParam(QueryBy queryBy, T key, String text) {
    m_queryBy = queryBy;
    m_key = key;
    m_text = text;
  }

  @Override
  public QueryBy getQueryBy() {
    return m_queryBy;
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public T getKey() {
    return m_key;
  }

  @Override
  public boolean is(QueryBy queryBy) {
    return m_queryBy == queryBy;
  }

  @SuppressWarnings("unchecked")
  public static <T> IQueryParam<T> createByText(String text) {
    return new QueryParam<T>(QueryBy.TEXT, null, text);
  }

  @SuppressWarnings("unchecked")
  public static <T> IQueryParam<T> createByAll() {
    return new QueryParam<T>(QueryBy.ALL, null, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> IQueryParam<T> createByKey(T key) {
    return new QueryParam<T>(QueryBy.KEY, key, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> IQueryParam<T> createByRec(T recKey) {
    return new QueryParam<T>(QueryBy.REC, recKey, null);
  }

}
