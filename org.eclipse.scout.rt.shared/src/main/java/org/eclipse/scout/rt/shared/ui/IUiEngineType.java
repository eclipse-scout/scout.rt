package org.eclipse.scout.rt.shared.ui;

import java.io.Serializable;

/**
 * Browser engine type
 *
 * @since 6.0.0
 */
public interface IUiEngineType extends Serializable {

  /**
   * @return unique id
   */
  String getIdentifier();

}
