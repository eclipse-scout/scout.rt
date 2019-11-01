import {TileAdapter} from '../index';

export default class BeanTileAdapter extends TileAdapter {

constructor() {
  super();
}


_onWidgetAppLinkAction(event) {
  this._send('appLinkAction', {
    ref: event.ref
  });
}

_onWidgetEvent(event) {
  if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else {
    super._onWidgetEvent( event);
  }
}
}
