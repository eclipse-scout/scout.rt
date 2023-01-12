/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, arrays, Desktop, EventHandler, HtmlComponent, icons, InitModelOf, KeyStrokeContext, OutlineViewButton, PropertyChangeEvent, scout, ViewButton, ViewMenuOpenKeyStroke, ViewMenuPopup, ViewMenuTabEventMap, ViewMenuTabModel, Widget
} from '../../index';

/**
 * Shows a list of view buttons with displayStyle=MENU
 * and shows the title of the active view button, if the view button is one
 * of the view buttons contained in the menu.
 */
export class ViewMenuTab extends Widget implements ViewMenuTabModel {
  declare model: ViewMenuTabModel;
  declare eventMap: ViewMenuTabEventMap;
  declare self: ViewMenuTab;

  selected: boolean;
  selectedButtonVisible: boolean;
  defaultIconId: string;
  viewButtons: ViewButton[];
  selectedButton: ViewButton;
  dropdown: Action;
  popup: ViewMenuPopup;
  desktopKeyStrokeContext: KeyStrokeContext;
  protected _desktopInBackgroundHandler: EventHandler<PropertyChangeEvent<boolean, Desktop>>;

  constructor() {
    super();

    this.viewButtons = [];
    this.selected = false;
    this.selectedButton = null;
    this.selectedButtonVisible = true;
    this.defaultIconId = icons.FOLDER;
    this._desktopInBackgroundHandler = this._onDesktopInBackgroundChange.bind(this);
    this._addWidgetProperties(['selectedButton']);
    this._addPreserveOnPropertyChangeProperties(['selectedButton']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.dropdown = scout.create(Action, {
      parent: this,
      iconId: icons.ANGLE_DOWN,
      tabbable: false,
      cssClass: 'view-menu',
      toggleAction: true
    });
    this.dropdown.on('action', this.togglePopup.bind(this));
    this._setViewButtons(this.viewButtons);
    this.session.desktop.on('propertyChange:inBackground', this._desktopInBackgroundHandler);
  }

  protected override _destroy() {
    this.session.desktop.off('propertyChange:inBackground', this._desktopInBackgroundHandler);
    super._destroy();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    this.desktopKeyStrokeContext = new KeyStrokeContext();
    this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke(new ViewMenuOpenKeyStroke(this));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('view-tab view-menu-tab');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.dropdown.render(this.$container);
    this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
    this.$container.appendDiv('edge right');
  }

  protected override _remove() {
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    super._remove();
    if (this.selectedButton) {
      this.selectedButton.remove();
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSelectedButtonVisible();
    this._renderSelected();
    this._renderInBackground();
  }

  setViewButtons(viewButtons: ViewButton[]) {
    this.setProperty('viewButtons', viewButtons);
  }

  protected _setViewButtons(viewButtons: ViewButton[]) {
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

  setSelectedButton(viewButton: ViewButton) {
    if (this.selectedButton && this.selectedButton.cloneOf === viewButton) {
      return;
    }
    if (viewButton) {
      this.setProperty('selectedButton', viewButton);
    }
  }

  protected _setSelectedButton(viewButton: ViewButton) {
    this.viewButtons.forEach(vb => vb.setSelectedAsMenu(vb === viewButton));

    // The selectedViewButton is a fake ViewButton but reflects the state of the actually selected one.
    // The fake button is created only once and must not be destroyed when the selected view button changes.
    // This is important to not break the CSS transition (e.g. when desktop is in background and another view selected using ViewMenuPopup).
    let clone = this.selectedButton;
    if (!clone) {
      clone = scout.create(OutlineViewButton, {
        parent: this,
        displayStyle: 'TAB'
      });
    }
    if (clone.cloneOf) {
      clone.cloneOf.unmirror(clone);
    }

    // Link our fake button with the original and apply all the relevant properties (which are stored in cloneProperties, e.g. outline, cssClass, enabled, etc.)
    clone.cloneOf = viewButton;
    viewButton._cloneProperties.forEach(property => clone.callSetter(property, viewButton[property]));

    // Use default icon if outline does not define one.
    clone.setIconId(viewButton.iconId || this.defaultIconId);

    // Mirror the events and property changes
    viewButton.mirror({
      delegateEventsToOriginal: ['acceptInput', 'action'],
      delegateAllPropertiesToClone: true,
      delegateAllPropertiesToOriginal: true,
      excludePropertiesToOriginal: ['selected']
    }, clone);

    this._setProperty('selectedButton', clone);
  }

  protected _renderSelectedButton() {
    this._renderSelectedButtonVisible();
  }

  setSelectedButtonVisible(selectedButtonVisible: boolean) {
    this.setProperty('selectedButtonVisible', selectedButtonVisible);
  }

  protected _renderSelectedButtonVisible() {
    this.$container.toggleClass('selected-button-invisible', !this.selectedButtonVisible);
    if (!this.selectedButton) {
      return;
    }
    if (this.selectedButtonVisible) {
      if (!this.selectedButton.rendered) {
        this.selectedButton.render();
        this.selectedButton.$container.prependTo(this.$container);
        this.invalidateLayoutTree();
      }
    } else {
      if (this.selectedButton.rendered) {
        this.selectedButton.remove();
        this.invalidateLayoutTree();
      }
    }
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    this.$container.select(this.selected);
  }

  protected _findSelectedViewButton(): ViewButton {
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

  protected _openPopup() {
    if (this.popup) {
      // already open
      return;
    }
    this.popup = scout.create(ViewMenuPopup, {
      parent: this,
      viewMenus: this.viewButtons,
      defaultIconId: this.defaultIconId,
      $anchor: this.$parent // use view button box as parent for better alignment
    });
    this.popup.open();
    this.popup.one('destroy', event => {
      this.dropdown.setSelected(false);
      this.popup = null;
    });
  }

  protected _closePopup() {
    if (this.popup) {
      this.popup.close();
    }
  }

  protected _renderInBackground() {
    if (this.session.desktop.displayStyle === Desktop.DisplayStyle.COMPACT) {
      return;
    }
    if (!this.rendering) {
      if (this.session.desktop.inBackground) {
        this.$container.addClassForAnimation('animate-bring-to-back');
      } else {
        this.$container.addClassForAnimation('animate-bring-to-front');
      }
    }
    this.$container.toggleClass('in-background', this.session.desktop.inBackground);
  }

  onViewButtonSelected() {
    let viewButton = this._findSelectedViewButton();
    if (viewButton) {
      this.setSelectedButton(viewButton);
    }
    this.setSelected(!!viewButton);
    this._closePopup();
  }

  protected _onDesktopInBackgroundChange(event: PropertyChangeEvent<boolean, Desktop>) {
    if (this.rendered) {
      this._renderInBackground();
    }
  }
}
