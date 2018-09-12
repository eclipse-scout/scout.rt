/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

@Bean
public class DataChangeManager extends AbstractGroupedListenerList<IDataChangeListener, DataChangeEvent, Object> implements IDataChangeManager {

  @Override
  public void addAll(IDataChangeManager other) {
    super.addAll((DataChangeManager) other);
  }

  @Override
  protected Set<Object> knownEventTypes() {
    return Collections.emptySet();
  }

  @Override
  protected Object otherEventsType() {
    return null;
  }

  @Override
  protected Object eventType(DataChangeEvent event) {
    return event.getDataType();
  }

  @Override
  protected void handleEvent(IDataChangeListener listener, DataChangeEvent event) {
    listener.dataChanged(event);
  }
}
