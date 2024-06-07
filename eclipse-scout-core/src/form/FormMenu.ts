/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ActionKeyStroke, aria, CloneOptions, ContextMenuPopup, Device, EnumObject, Event, EventHandler, Form, FormMenuActionKeyStroke, FormMenuEventMap, FormMenuModel, GroupBox, InitModelOf, Menu, MobilePopup, ObjectOrChildModel, Popup, scout,
  WidgetPopup
} from '../index';

export class FormMenu extends Menu implements FormMenuModel {
  declare model: FormMenuModel;
  declare eventMap: FormMenuEventMap;
  declare self: FormMenu;
  declare popup: WidgetPopup;

  form: Form;
  popupStyle: FormMenuPopupStyle;
  popupClosable: boolean;
  popupMovable: boolean;
  popupResizable: boolean;
  protected _formDestroyHandler: EventHandler<Event<Form>>;

  constructor() {
    super();
    this.form = null;
    this.toggleAction = true;
    this.popupStyle = null;
    this.popupClosable = false;
    this.popupMovable = false;
    this.popupResizable = false;
    this._addWidgetProperties('form');
    this._formDestroyHandler = this._onFormDestroy.bind(this);
  }

  static PopupStyle = {
    DEFAULT: 'default',
    MOBILE: 'mobile'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    if (!this.popupStyle) {
      if (this.session.userAgent.deviceType === Device.Type.MOBILE) {
        this.popupStyle = FormMenu.PopupStyle.MOBILE;
      } else {
        this.popupStyle = FormMenu.PopupStyle.DEFAULT;
      }
    }
    this._setSelected(this.selected);
    this._setForm(this.form);
  }

  protected _renderForm() {
    if (!this.rendered) {
      // Don't execute initially since _renderSelected will be executed
      return;
    }
    this._renderSelected();
  }

  override clone(modelOverride: FormMenuModel, options: CloneOptions): this {
    modelOverride = modelOverride || {};
    // If the FormMenu is put into a context menu it will be cloned.
    // Cloning a form is not possible because it may non-cloneable components (Table, TabBox, etc.) -> exclude
    // Luckily, it is not necessary to clone it since the form is never shown multiple times at once -> Just use the same instance
    modelOverride.form = this.form;
    return super.clone(modelOverride, options);
  }

  setForm(form: ObjectOrChildModel<Form>) {
    this.setProperty('form', form);
  }

  protected _setForm(form: Form) {
    if (this.form) {
      this.form.off('destroy', this._formDestroyHandler);
    }
    this._setProperty('form', form);
    if (this.form) {
      this._adaptForm(this.form);
      this.form.one('destroy', this._formDestroyHandler);
    }
  }

  protected _adaptForm(form: Form) {
    form.setShowOnOpen(false);
    form.setDisplayHint(Form.DisplayHint.VIEW);
    form.setModal(false);
    form.setClosable(false);
  }

  /**
   * Called when the popup form is destroyed (e.g. form.close() was called) -> ensure menu is unselected and popup closed.
   */
  protected _onFormDestroy(event: Event<Form>) {
    if (!this.popup || !this.popup.destroying) {
      // Unselect if form is closed (e.g. if a close button on the form itself is pressed. Mainly necessary for Scout JS only)
      // Don't interfere with regular popup lifecycle. If popup is being closed already it will be or is already unselected anyway.
      // Maybe the user already selected the menu again while the close animation runs -> the menu must not be unselected because the user selected it.
      this.setSelected(false);
    }
    this.setForm(null);

    let parentContextMenuPopup = this.findParent(ContextMenuPopup);
    if (parentContextMenuPopup && !(parentContextMenuPopup.destroying || parentContextMenuPopup.removing)) {
      // only explicitly close the popup if it is not already being closed. Otherwise, it is removed twice.
      parentContextMenuPopup.close();
    }
  }

  protected _setSelected(selected: boolean) {
    this._setProperty('selected', selected);
    if (this.popupStyle === FormMenu.PopupStyle.MOBILE && this._doActionTogglesPopup()) {
      // Mobile Popup can be rendered even if menu is not. This is useful if a tool form menu should be opened while the desktop bench is open instead of the outline
      // Open will be called in renderSelected again but won't do anything
      if (this.selected) {
        this._openPopup();
      } else {
        this._closePopup();
      }
    }
  }

  protected override _renderSelected() {
    super._renderSelected();

    // Form menu always has a popup (form could be set later, so super call cannot set the class correctly)
    this.$container.addClass('has-popup');
    aria.hasPopup(this.$container, 'dialog');
  }

  protected override _canOpenPopup(): boolean {
    // A menu can be opened in the menu bar but also in a context menu, where it will be cloned.
    // The form itself won't be cloned, so there can always be only one rendered form.
    // If the menus use a remove animation and a new menu is opened while the other one is still removing, the form rendering will fail
    // (either by an exception if its already open, or it may be rendered into the wrong menu).
    // To prevent that, we ensure the other popup is really closed before opening the new one.
    this._closeOtherPopupsForSameMenu();
    return super._canOpenPopup();
  }

  protected _closeOtherPopupsForSameMenu() {
    this._findOtherPopupsForSameMenu().forEach(popup => {
      if (popup.isRemovalPending()) {
        popup.removeImmediately();
        return;
      }
      // If popup is open but remove animation has not started yet (can only be triggered programmatically, see test FormMenuSpec.js)
      if (popup._rendered) {
        let currentAnimateRemoval = popup.animateRemoval;
        popup.animateRemoval = false;
        popup.close();
        popup.animateRemoval = currentAnimateRemoval;
      }
    });
  }

  protected _findOtherPopupsForSameMenu(): Popup[] {
    return this.session.desktop.getPopups().filter(popup => {
      if (popup === this.popup || popup.has(this)) {
        return false;
      }
      return this._popupBelongsToMenu(popup);
    });
  }

  protected _popupBelongsToMenu(popup: Popup): boolean {
    // Check if the widget popup containing the form is open (parent is always the form menu, if it's in a context menu the parent is a clone)
    if (popup.parent.original() === this.original()) {
      return true;
    }
    // Check if the context menu containing this menu is open (context menus contain clones of the original)
    if (popup.findChild(w => w.original() === this.original())) {
      return true;
    }
    return false;
  }

  protected override _createPopup(): Popup {
    // Menu bar should always be on the bottom
    this.form.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);

    if (this.popupStyle === FormMenu.PopupStyle.MOBILE) {
      return scout.create(MobilePopup, {
        parent: this.session.desktop, // use desktop to make _handleSelectedInEllipsis work (if parent is this and this is not rendered, popup.entryPoint would not work)
        content: this.form,
        title: this.form.title
      });
    }

    return scout.create(WidgetPopup, {
      parent: this,
      content: this.form,
      anchor: this,
      closeOnAnchorMouseDown: false,
      cssClass: 'form-menu-popup',
      horizontalAlignment: this.popupHorizontalAlignment,
      verticalAlignment: this.popupVerticalAlignment,
      closable: this.popupClosable,
      movable: this.popupMovable,
      resizable: this.popupResizable
    });
  }

  protected override _doActionTogglesPopup(): boolean {
    return !!this.form;
  }

  override updateAriaRole() {
    // Always render as menuitem. The form may be installed later, which would falsely render a menuitemcheckbox.
    aria.role(this.$container, 'menuitem');
  }

  protected override _handleSelectedInEllipsis() {
    if (this.popupStyle === FormMenu.PopupStyle.MOBILE) {
      // The mobile popup is not attached to a header -> no need to open the overflow menu (popup is already open due to _setSelected)
      return;
    }
    super._handleSelectedInEllipsis();
  }

  protected override _createActionKeyStroke(): ActionKeyStroke {
    return new FormMenuActionKeyStroke(this);
  }

  setPopupClosable(popupClosable: boolean) {
    if (this.setProperty('popupClosable', popupClosable) && this.popup) {
      this.popup.setClosable(popupClosable);
    }
  }

  setPopupMovable(popupMovable: boolean) {
    if (this.setProperty('popupMovable', popupMovable) && this.popup) {
      this.popup.setMovable(popupMovable);
    }
  }

  setPopupResizable(popupResizable: boolean) {
    if (this.setProperty('popupResizable', popupResizable) && this.popup) {
      this.popup.setResizable(popupResizable);
    }
  }
}

export type FormMenuPopupStyle = EnumObject<typeof FormMenu.PopupStyle>;
