import {Tile} from '../index';
import {HtmlComponent} from '../index';
import * as $ from 'jquery';

export default class BeanTile extends Tile {

constructor() {
  super();

  this.bean = null;
}


_render() {
  this.$container = this.$parent.appendDiv('bean-tile');
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
}

_renderProperties() {
  super._renderProperties();
  this._renderBean();
}

_renderBean() {
  // to be implemented by the subclass
}

triggerAppLinkAction(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
}

_onAppLinkAction(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
}
}
