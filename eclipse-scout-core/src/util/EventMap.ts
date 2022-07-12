import {Event} from '../index';

export interface EventMap {
  [type: string]: Event
}

/**
 * Excludes all properties that cannot be passed as part of the event model when the event is triggered.
 */
export type EventTypeModel<EVENT extends Event> = {
  [Property in keyof EVENT as Exclude<Property, 'source' | 'type' | 'defaultPrevented' | 'preventDefault'>]: EVENT[Property]
}
