import {WidgetEvent, WidgetEventMap} from '../widget/WidgetEventMap';
import {Action} from '../index';
import {PropertyChangeEvent} from '../util/PropertyChangeEvent';

export interface ActionEvent extends WidgetEvent {
  source: Action
}

export interface ActionEventMap extends WidgetEventMap {
  'action': ActionEvent,
  'propertyChange:iconId': PropertyChangeEvent<Action, string>
}
