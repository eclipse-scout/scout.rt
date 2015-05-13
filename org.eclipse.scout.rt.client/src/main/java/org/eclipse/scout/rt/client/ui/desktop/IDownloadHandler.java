package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.commons.resource.BinaryResource;

/**
 * Used by {@link IDesktop#createDownloadUrl(String, IDownloadHandler)} in order to create a dynamic url for a file
 * download that can be opened with {@link IDesktop#openUri(String)}
 *
 * @since 5.0
 */
public interface IDownloadHandler {

  /**
   * @return the TTL (time to live).
   */
  long getTTL();

  /**
   * @return the binary content of this download
   */
  BinaryResource getResource();
}
