import {Event, EventSupport} from '../index';
import {EventMap, EventTypeModel} from './EventMap';

export interface EventHandler<K> {
  (event: K): void
}

export default class EventEmitter {
  events: EventSupport;

  constructor() {
    this.events = this._createEventSupport();
  }

  protected _createEventSupport() {
    return new EventSupport();
  }

  trigger<K extends string & keyof EventMap>(type: K, eventOrModel?: Event | EventTypeModel<EventMap[K]>) { // TODO CGU alternative: // event?: EventMap[K] | { [key: string]: any }) {
    let event:Event;
    if (event instanceof Event) {
      event = eventOrModel as Event;
    } else {
      event = new Event(eventOrModel);
    }
    event.source = this;
    this.events.trigger(type, event);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param type One or more event names separated by space.
   * @param handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one<K extends string & keyof EventMap>(type: K, handler: EventHandler<K>) {
    this.events.one(type, handler);
  }

  on<K extends string & keyof EventMap>(type: K, handler: EventHandler<EventMap[K]>) {
    return this.events.on(type, handler);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param handler The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off<K extends string & keyof EventMap>(type: K, handler?: EventHandler<K>) {
    this.events.off(type, handler);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   * @returns {Promise}
   */
  when(type) {
    return this.events.when(type);
  }
}
