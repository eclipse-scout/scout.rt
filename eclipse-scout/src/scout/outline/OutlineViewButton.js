import ViewButton from '../desktop/ViewButton';

export default class OutlineViewButton extends ViewButton {

  constructor() {
    super();
    this._addWidgetProperties('outline');
    this._addCloneProperties(['outline']);
  }

  _init(model) {
    super._init(model);
    this._setOutline(this.outline);
  };

  _setOutline(outline) {
    this._setProperty('outline', outline);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  };

  _setIconId(iconId) {
    this._setProperty('iconId', iconId);
    if (this.outline) {
      this.outline.setIconId(this.iconId);
    }
  };

  /**
   * @override
   */
  _doAction() {
    super._doAction();
    if (this.outline) {
      this.session.desktop.setOutline(this.outline);
      this.session.desktop.bringOutlineToFront(this.outline);
    }
  };

  onOutlineChange(outline) {
    var selected = !!outline && this.outline === outline;
    this.setSelected(selected);
  };

}
