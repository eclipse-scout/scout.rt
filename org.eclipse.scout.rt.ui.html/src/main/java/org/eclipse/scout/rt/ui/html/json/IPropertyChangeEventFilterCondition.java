package org.eclipse.scout.rt.ui.html.json;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.platform.filter.IFilter;

/**
 * Interface used to filter property change events.
 */
public interface IPropertyChangeEventFilterCondition extends IFilter<PropertyChangeEvent> {

  String getPropertyName();

}
