import {Event, EventEmitter, PropertyEventEmitter} from '../index';

export default interface PropertyChangeEvent<SOURCE extends PropertyEventEmitter, PROP_TYPE> extends Event {
  source: SOURCE;
  propertyName: 'string';
  newValue: PROP_TYPE;
  oldValue: PROP_TYPE;
}
