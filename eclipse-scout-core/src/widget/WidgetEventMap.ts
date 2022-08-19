import {DisabledStyle, Event, EventMap, LogicalGrid, PropertyChangeEvent, Widget} from '../index';
import {GlassPaneContribution} from './Widget';

export interface WidgetEvent extends Event {
  source: Widget;
}

export interface HierarchyChangeEvent extends WidgetEvent {
  oldParent: Widget;
  parent: Widget;
}

export interface GlassPaneContributionEvent extends WidgetEvent {
  contribution: GlassPaneContribution;
}

export default interface WidgetEventMap extends EventMap {
  'init': WidgetEvent;
  'destroy': WidgetEvent;
  'render': WidgetEvent;
  'remove': WidgetEvent;
  'removing': WidgetEvent;
  'glassPaneContributionAdded': GlassPaneContributionEvent;
  'glassPaneContributionRemoved': GlassPaneContributionEvent;
  'hierarchyChange': HierarchyChangeEvent;
  'propertyChange': PropertyChangeEvent<Widget, any>;
  'propertyChange:enabled': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:enabledComputed': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:trackFocus': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:scrollTop': PropertyChangeEvent<Widget, number>;
  'propertyChange:scrollLeft': PropertyChangeEvent<Widget, number>;
  'propertyChange:inheritAccessibility': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:disabledStyle': PropertyChangeEvent<Widget, DisabledStyle>;
  'propertyChange:visible': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:focused': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:cssClass': PropertyChangeEvent<Widget, string>;
  'propertyChange:loading': PropertyChangeEvent<Widget, boolean>;
  'propertyChange:logicalGrid': PropertyChangeEvent<Widget, LogicalGrid>;
}
