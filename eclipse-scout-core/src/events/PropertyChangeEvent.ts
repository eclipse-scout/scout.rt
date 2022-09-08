import {Event, PropertyEventEmitter} from '../index';

export default interface PropertyChangeEvent<Value> extends Event<PropertyEventEmitter> {
  propertyName: string;
  newValue: Value;
  oldValue: Value;
}
