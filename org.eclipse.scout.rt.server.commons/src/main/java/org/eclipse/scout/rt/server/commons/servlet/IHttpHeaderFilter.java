package org.eclipse.scout.rt.server.commons.servlet;

/**
 * Interface for {@link HttpProxy} to support request/response header filtering.
 */
public interface IHttpHeaderFilter {

  /**
   * Called to filter the particular header.
   *
   * @return New filtered value for the header or <code>null</code> to remove the header. Just return the same value if
   *         filter should keep this header untouched.
   */
  String filter(String name, String value);

}
