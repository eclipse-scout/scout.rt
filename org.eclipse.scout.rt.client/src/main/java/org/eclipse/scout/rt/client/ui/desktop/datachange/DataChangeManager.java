/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.Bean;

/**
 * @since 7.1
 */
@Bean
public class DataChangeManager implements IDataChangeManager {

  private List<IDataChangeListener> m_listeners = new ArrayList<>();

  @Override
  public void addDataChangeListener(IDataChangeListener listener) {
    m_listeners.add(listener);
  }

  @Override
  public void removeDataChangeListener(IDataChangeListener listener) {
    m_listeners.remove(listener);
  }

  @Override
  public void fireDataChangeEvent(DataChangeEvent event) {
    for (IDataChangeListener listener : m_listeners) {
      listener.dataChanged(event);
    }
  }

}
