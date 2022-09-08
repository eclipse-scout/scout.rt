import {DisabledStyle, Event, LogicalGrid, PropertyChangeEvent, Widget} from '../index';
import {GlassPaneContribution} from './Widget';
import PropertyEventMap from '../events/PropertyEventMap';

export interface HierarchyChangeEvent extends Event {
  oldParent: Widget;
  parent: Widget;
}

export interface GlassPaneContributionEvent extends Event {
  contribution: GlassPaneContribution;
}

export default interface WidgetEventMap extends PropertyEventMap {
  'init': Event;
  'destroy': Event;
  'render': Event;
  'remove': Event;
  'removing': Event;
  'glassPaneContributionAdded': GlassPaneContributionEvent;
  'glassPaneContributionRemoved': GlassPaneContributionEvent;
  'hierarchyChange': HierarchyChangeEvent;
  'propertyChange:enabled': PropertyChangeEvent<boolean>;
  'propertyChange:enabledComputed': PropertyChangeEvent<boolean>;
  'propertyChange:trackFocus': PropertyChangeEvent<boolean>;
  'propertyChange:scrollTop': PropertyChangeEvent<number>;
  'propertyChange:scrollLeft': PropertyChangeEvent<number>;
  'propertyChange:inheritAccessibility': PropertyChangeEvent<boolean>;
  'propertyChange:disabledStyle': PropertyChangeEvent<DisabledStyle>;
  'propertyChange:visible': PropertyChangeEvent<boolean>;
  'propertyChange:focused': PropertyChangeEvent<boolean>;
  'propertyChange:cssClass': PropertyChangeEvent<string>;
  'propertyChange:loading': PropertyChangeEvent<boolean>;
  'propertyChange:logicalGrid': PropertyChangeEvent<LogicalGrid>;
}
