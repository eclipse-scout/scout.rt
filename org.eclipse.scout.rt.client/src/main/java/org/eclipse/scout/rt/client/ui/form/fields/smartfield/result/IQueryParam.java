package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

public interface IQueryParam<T> {

  public enum QueryBy {
    ALL,
    TEXT,
    KEY,
    REC
  }

  QueryBy getQueryBy();

  boolean is(QueryBy parentKey);

  T getKey();

  String getText();

}
