/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.spellchecker;

/**
 * This class is Thread-safe
 */
public interface ISpellingMonitor {

  void validate();

  void validate(String word);

  /**
   * to activate the monitor on the associated UI widget.
   */
  void activate();

  /**
   * to deactivate the monitor on the associated UI widget. Unlike {@link #dispose() dispose} the monitor is not
   * shutdown but still associated with the widget.
   */
  void deactivate();

  /**
   * to shutdown the monitor on the associated UI widget.
   */
  void dispose();
}
