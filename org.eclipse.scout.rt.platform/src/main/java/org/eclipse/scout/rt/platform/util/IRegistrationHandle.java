package org.eclipse.scout.rt.platform.util;

/**
 * Represents a <em>handle</em> for a registration, and can later be used to undo the registration.
 *
 * @since 5.2
 */
@FunctionalInterface
public interface IRegistrationHandle extends IDisposable {

  /**
   * Unregisters the registration represented by this handle. This call has no effect if already disposed.
   */
  @Override
  void dispose();

  /**
   * Handle that does nothing upon {@link #dispose()}.
   */
  IRegistrationHandle NULL_HANDLE = () -> {
    // NOOP
  };
}
