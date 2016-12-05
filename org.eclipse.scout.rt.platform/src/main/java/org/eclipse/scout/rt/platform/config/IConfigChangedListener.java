package org.eclipse.scout.rt.platform.config;

import java.util.EventListener;

/**
 * <h3>{@link IConfigChangedListener}</h3><br>
 * Interface for listeners that want to be notified if the state or value of a {@link IConfigProperty} changes.
 */
public interface IConfigChangedListener extends EventListener {
  /**
   * Callback indicating that the given event occurred.
   * 
   * @param event
   *          The event describing the change.
   */
  void configPropertyChanged(ConfigPropertyChangeEvent event);
}
