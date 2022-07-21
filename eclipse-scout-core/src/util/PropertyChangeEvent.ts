import {Event} from '../index';
import EventEmitter from './EventEmitter';

export interface PropertyChangeEvent<SOURCE extends EventEmitter, PROP_TYPE> extends Event {
  source: SOURCE;
  propertyName: 'string';
  newValue: PROP_TYPE;
  oldValue: PROP_TYPE;
}
