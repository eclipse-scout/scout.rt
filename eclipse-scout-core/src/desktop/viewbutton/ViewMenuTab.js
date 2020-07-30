/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, graphics, HtmlComponent, icons, KeyStrokeContext, scout, ViewMenuOpenKeyStroke, Widget} from '../../index';

/**
 * Shows a list of view buttons with displayStyle=MENU
 * and shows the title of the active view button, if the view button is one
 * of the view buttons contained in the menu.
 */
export default class ViewMenuTab extends Widget {

  constructor() {
    super();

    this.viewButtons = [];
    this.selected = false;
    this.selectedButton = null;
    this.viewTabVisible = true;
    this.defaultIconId = icons.FOLDER;
    this._addWidgetProperties(['selectedButton']);
  }

  _init(model) {
    super._init(model);
    this.dropdown = scout.create('Menu', {
      parent: this,
      iconId: icons.ANGLE_DOWN,
      tabbable: false,
      cssClass: 'view-menu'
    });
    this.dropdown.on('action', this.togglePopup.bind(this));
    this._setViewButtons(this.viewButtons);
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    this.desktopKeyStrokeContext = new KeyStrokeContext();
    this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke([
      new ViewMenuOpenKeyStroke(this)
    ]);
  }

  _render() {
    this.$container = this.$parent.appendDiv('view-tab');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.dropdown.render(this.$container);
    this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  }

  _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    super._remove();
    if (this.selectedButton) {
      this.selectedButton.remove();
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._updateSelectedButton();
  }

  setViewButtons(viewButtons) {
    this.setProperty('viewButtons', viewButtons);
  }

  _setViewButtons(viewButtons) {
    this._setProperty('viewButtons', viewButtons);
    this.setVisible(this.viewButtons.length > 0);
    let selectedButton = this._findSelectedViewButton();
    if (selectedButton) {
      this.setSelectedButton(selectedButton);
    } else {
      this.setSelectedButton(arrays.find(this.viewButtons, v => v.selectedAsMenu) || this.viewButtons[0]);
    }
    this.setSelected(!!selectedButton);
  }

  setSelectedButton(viewButton) {
    if (this.selectedButton && this.selectedButton.cloneOf === viewButton) {
      return;
    }
    if (viewButton) {
      this.setProperty('selectedButton', viewButton);
    }
  }

  _setSelectedButton(viewButton) {
    this.viewButtons.forEach(vb => vb.setSelectedAsMenu(vb === viewButton));

    viewButton = viewButton.clone({
      parent: this,
      displayStyle: 'TAB'
    }, {
      delegateEventsToOriginal: ['acceptInput', 'action'],
      delegateAllPropertiesToClone: true,
      delegateAllPropertiesToOriginal: true,
      excludePropertiesToOriginal: ['selected']
    });

    // use default icon if outline does not define one.
    viewButton.iconId = viewButton.iconId || this.defaultIconId;
    this._setProperty('selectedButton', viewButton);
  }

  _renderSelectedButton() {
    this._updateSelectedButton();
  }

  _updateSelectedButton() {
    if (!this.selectedButton) {
      return;
    }
    if (this.viewTabVisible) {
      if (!this.selectedButton.rendered) {
        this.selectedButton.render(this.$container);
        this.invalidateLayoutTree();
      }
    } else {
      if (this.selectedButton.rendered) {
        this.selectedButton.remove();
        this.invalidateLayoutTree();
      }
    }
  }

  setViewTabVisible(viewTabVisible) {
    this.setProperty('viewTabVisible', viewTabVisible);
    if (this.rendered) {
      this._updateSelectedButton();
    }
  }

  _renderSelected() {
    this.$container.select(this.selected);
  }

  _findSelectedViewButton() {
    return arrays.find(this.viewButtons, v => v.selected);
  }

  /**
   * Toggles the 'view menu popup', or brings the outline content to the front if in background.
   */
  togglePopup() {
    if (this.popup) {
      this._closePopup();
    } else {
      this._openPopup();
    }
  }

  _openPopup() {
    if (this.popup) {
      // already open
      return;
    }
    let naviBounds = graphics.bounds(this.$container.parent(), true);
    this.popup = scout.create('ViewMenuPopup', {
      parent: this,
      $tab: this.dropdown.$container,
      viewMenus: this.viewButtons,
      naviBounds: naviBounds
    });
    // The class needs to be added to the container before the popup gets opened so that the modified style may be copied to the head.
    this.$container.addClass('popup-open');
    this.popup.headText = this.text;
    this.popup.open();
    this.popup.on('remove', event => {
      this.$container.removeClass('popup-open');
      this.popup = null;
    });
  }

  _closePopup() {
    if (this.popup) {
      this.popup.close();
    }
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  sendToBack() {
    this._closePopup();
  }

  bringToFront() {
    // NOP
  }

  onViewButtonSelected() {
    let viewButton = this._findSelectedViewButton();
    if (viewButton) {
      this.setSelectedButton(viewButton);
    }
    this.setSelected(!!viewButton);
    this._closePopup();
  }
}
