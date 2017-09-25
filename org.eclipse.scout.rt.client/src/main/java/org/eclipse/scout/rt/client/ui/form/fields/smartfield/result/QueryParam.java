package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;

public class QueryParam {

  public static IQueryParam createQueryByText(ISmartField smartField, String text) {
    return new ByTextQueryParam(smartField.getWildcard(), text);
  }

  public static IQueryParam createQueryByAll(ISmartField smartField) {
    return new ByTextQueryParam(smartField.getWildcard(), null);
  }

  public static <T> IQueryParam createQueryByKey(ISmartField smartField, T key) {
    return new ByKeyQueryParam<T>(key);
  }

  public static <T> IQueryParam createQueryByParentKey(ISmartField smartField, T parentKey) {
    return new ByParentKeyQueryParam<T>(parentKey);
  }

  public static boolean isAllQuery(IQueryParam queryParam) {
    return queryParam instanceof ByTextQueryParam && ((ByTextQueryParam) queryParam).isBrowseAll();
  }

  public static boolean isTextQuery(IQueryParam queryParam) {
    return queryParam instanceof ByTextQueryParam && !((ByTextQueryParam) queryParam).isBrowseAll();
  }

  public static boolean isParentKeyQuery(IQueryParam queryParam) {
    return queryParam instanceof ByParentKeyQueryParam;
  }

  public static boolean isKeyQuery(IQueryParam queryParam) {
    return queryParam instanceof ByKeyQueryParam;
  }

  public static String getText(IQueryParam queryParam) {
    return ((ByTextQueryParam) queryParam).getText();
  }

  public static <T> T getParentKey(IQueryParam queryParam) {
    return ((ByParentKeyQueryParam<T>) queryParam).getParentKey();
  }

  public static <T> T getKey(IQueryParam queryParam) {
    return ((ByKeyQueryParam<T>) queryParam).getKey();
  }

}
