package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.rt.shared.data.basic.BinaryResource;

/**
 * Used by {@link IDesktop#createDownloadUrl(String, IDownloadHandler)} in order to create a dynamic url for a file
 * download
 * that can be opened with {@link IDesktop#openUrlInBrowser(String)}
 *
 * @since 5.0
 */
public interface IDownloadHandler {
  /**
   * @return true if the download is (still) active, false causes this {@link IDownloadHandler} to be disposed.
   *         <p>
   *         This can be used to handle download timeout
   */
  boolean isActive();

  /**
   * @return the binary content of this download
   */
  BinaryResource getResource();
}
