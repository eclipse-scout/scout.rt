import {HtmlComponent} from '../index';
import {AppLinkKeyStroke} from '../index';
import {strings} from '../index';
import {KeyStrokeContext} from '../index';
import {Widget} from '../index';
import * as $ from 'jquery';

export default class Label extends Widget {

constructor() {
  super();
  this.value = null;
  this.htmlEnabled = false;
  this.scrollable = false;
}


/**
 * @override
 */
_createKeyStrokeContext() {
  return new KeyStrokeContext();
}

/**
 * @override
 */
_initKeyStrokeContext() {
  super._initKeyStrokeContext();

  this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
}

_init(model) {
  super._init( model);
  this.resolveTextKeys(['value']);
}

_render() {
  this.$container = this.$parent.appendDiv();
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
}

_renderProperties() {
  super._renderProperties();
  this._renderValue();
  this._renderScrollable();
}

setValue(value) {
  this.setProperty('value', value);
}

_renderValue() {
  var value = this.value || '';
  if (this.htmlEnabled) {
    this.$container.html(value);

    // Find app links and add handlers
    this.$container.find('.app-link').on('click', this._onAppLinkAction.bind(this));

    // Add handler to images to update the layout when the images are loaded
    this.$container.find('img')
      .on('load', this._onImageLoad.bind(this))
      .on('error', this._onImageError.bind(this));
  } else {
    this.$container.html(strings.nl2br(value));
  }

  // Because this method replaces the content, the scroll bars might have to be added or removed
  if (this.rendered) { // (only necessary if already rendered, otherwise it is done by renderProperties)
    this._uninstallScrollbars();
    this._renderScrollable();
  }

  this.invalidateLayoutTree();
}

setHtmlEnabled(htmlEnabled) {
  this.setProperty('htmlEnabled', htmlEnabled);
}

_renderHtmlEnabled() {
  // Render the value again when html enabled changes dynamically
  this._renderValue();
}

setScrollable(scrollable) {
  this.setProperty('scrollable', scrollable);
}

_renderScrollable() {
  if (this.scrollable) {
    this._installScrollbars();
  } else {
    this._uninstallScrollbars();
  }
}

_onAppLinkAction(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
}

triggerAppLinkAction(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
}

_onImageLoad(event) {
  this.invalidateLayoutTree();
}

_onImageError(event) {
  this.invalidateLayoutTree();
}
}
