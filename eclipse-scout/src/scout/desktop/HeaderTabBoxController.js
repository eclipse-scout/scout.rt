import DesktopTabBoxController from './DesktopTabBoxController';
import * as scout from '../scout';

export default class HeaderTabBoxController extends DesktopTabBoxController {

  constructor() {
    super();
    this.bench;
    this._viewsChangedHandler = this._onViewsChanged.bind(this);

    this.tabAreaCenter;
    this.tabAreaInHeader = false;
  }

  install(bench, tabArea) {
    this.bench = scout.assertParameter('bench', bench);

    var tabBoxCenter = this.bench.getTabBox('C');
    this.tabAreaCenter = tabBoxCenter.tabArea;

    super.install(tabBoxCenter, tabArea);
  };

  _installListeners() {
    super._installListeners.call(this);
    this.bench.on('viewAdd', this._viewsChangedHandler);
    this.bench.on('viewRemove', this._viewsChangedHandler);
  };

  _onViewsChanged() {
    if (this.bench.getViews().some(function(view) {
        return 'C' !== view.displayViewId;
      })) {
      // has views in other view stacks
      this._setViewTabAreaInHeader(false);
    } else {
      // has only views in center
      this._setViewTabAreaInHeader(true);
    }
  };

  _setViewTabAreaInHeader(inHeader) {
    this.tabAreaInHeader = inHeader;
    this.tabAreaCenter.setVisible(!inHeader);
    this.tabArea.setVisible(inHeader);
  };

  getTabs() {
    if (this.tabAreaInHeader) {
      return this.tabArea.getTabs();
    }
    return this.tabAreaCenter.getTabs();
  };

}
