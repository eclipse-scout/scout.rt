package org.eclipse.scout.rt.client.ui.desktop.datachange;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

@Bean
public class DataChangeManager extends AbstractGroupedListenerList<IDataChangeListener, DataChangeEvent, Object> implements IDataChangeManager {

  @Override
  public void addAll(IDataChangeManager other) {
    super.addAll((DataChangeManager) other);
  }

  @Override
  protected Object allEventsType() {
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
