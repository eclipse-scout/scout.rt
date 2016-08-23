package org.eclipse.scout.rt.platform.util;

/**
 * Represents an object that can be disposed to free associated resources.
 *
 * @since 6.0
 */
public interface IDisposable {

  /**
   * Disposes this object and releases any associated resources.
   */
  void dispose();
}
