import Action from '../Action/Action';

export default class ViewButton extends Action {

  constructor() {
    super();
    this.showTooltipWhenSelected = false;
    this.displayStyle = 'TAB';
    this._renderedAsMenu = false;
  }

  renderAsMenuItem($parent) {
    this._renderedAsMenu = true;
    super.render($parent);
  };
  renderAsTab($parent) {
    this._renderedAsMenu = false;
    super.render($parent);
  };

  _render() {
    if (this._renderedAsMenu) {
      this._renderAsMenuItem();
    } else {
      this._renderAsTab();
    }
  };

  _renderAsMenuItem() {
    this.$container = this.$parent.appendDiv('view-menu-item')
      .on('click', this._onMouseEvent.bind(this));
  };

  _renderAsTab() {
    this.$container = this.$parent.appendDiv('view-button-tab')
      .on('mousedown', this._onMouseEvent.bind(this));
  };

  /**
   * @override Action.js
   */
  _renderText() {
    /*if (this._renderedAsMenu) {*/
    super._renderText();
    //}
  };

  setDisplayStyle(displayStyle) {
    this.setProperty('displayStyle', displayStyle);
  };

  last() {
    this.$container.addClass('last');
  };

  tab() {
    this.$container.addClass('view-tab');
  };

  _onMouseEvent(event) {
    this.doAction();
  };

  /**
   * @override Action.js
   */
  /*_createActionKeyStroke() {
        return new scout.ViewButtonActionKeyStroke(this);
    };

    scout.ViewButtonActionKeyStroke(action) {
        scout.ViewButtonActionKeyStroke.parent.call(this, action);

    };
    scout.inherits(scout.ViewButtonActionKeyStroke, scout.ActionKeyStroke);

    scout.ViewButtonActionKeyStroke.prototype._postRenderKeyBox($drawingArea) {
        if (this.field.iconId && !this.field._isMenuItem) {
            var width = $drawingArea.outerWidth();
            var wKeybox = $drawingArea.find('.key-box').outerWidth();
            var leftKeyBox = width / 2 - wKeybox / 2;
            $drawingArea.find('.key-box').cssLeft(leftKeyBox);
        }
    };

    scout.ViewButtonActionKeyStroke.prototype.renderKeyBox($drawingArea, event) {
        if (this.field._isMenuItem) {
            this.renderingHints.hAlign = scout.hAlign.RIGHT;
        }
        return scout.ViewButtonActionKeyStroke.parent.prototype.renderKeyBox.call(this, $drawingArea, event);
    };
*/
}
