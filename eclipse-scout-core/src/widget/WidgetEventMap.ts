import {Widget, Event} from '../index';
import {PropertyChangeEvent} from '../util/PropertyChangeEvent';
import {EventMap} from '../util/EventMap';

export interface WidgetEvent extends Event {
  source: Widget;
}

export interface HierarchyChangeEvent extends WidgetEvent {
  oldParent: Widget;
  parent: Widget;
}

export interface WidgetEventMap extends EventMap{
  'render': WidgetEvent,
  'remove': WidgetEvent,
  'hierarchyChange': HierarchyChangeEvent,
  'propertyChange:enabled': PropertyChangeEvent<Widget, boolean>
}
