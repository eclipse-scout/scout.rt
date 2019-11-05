import {Desktop} from './index';
import {scout} from '@eclipse-scout/core';

scout.addObjectFactories({
  'Desktop': function() {
    return new Desktop();
  }
});
