import {Event} from '../index';

export default interface EventMap {
  [type: string]: Event;
}
