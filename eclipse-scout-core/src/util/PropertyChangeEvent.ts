import {Event} from '../index';
import EventEmitter from './EventEmitter';

export interface PropertyChangeEvent<SOURCE extends EventEmitter, PROP_TYPE> extends Event {
  source: SOURCE,
  newValue: PROP_TYPE,
  oldValue: PROP_TYPE
}
