/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbortKeyStroke, Button, DialogLayout, Event, FileChooserController, FocusRule, FormLayout, GlassPaneRenderer, GroupBox, HtmlComponent, KeyStrokeContext, MessageBoxController, Rectangle, scout, Status, strings, tooltips, webstorage, Widget, WrappedFormField} from '../index';
import $ from 'jquery';

export default class Form extends Widget {
  constructor() {
    super();
    this._addWidgetProperties(['rootGroupBox', 'views', 'dialogs', 'initialFocus', 'messageBoxes', 'fileChoosers']);
    this._addPreserveOnPropertyChangeProperties(['initialFocus']);

    this.animateOpening = true;
    this.askIfNeedSave = true;
    this.askIfNeedSaveText = null; // if not set, a default text is used (see Lifecycle.js)
    this.data = {};
    this.displayViewId = null;
    this.displayHint = Form.DisplayHint.DIALOG;
    this.displayParent = null; // only relevant if form is opened, not relevant if form is just rendered into another widget (not managed by a form controller)
    this.maximized = false;
    this.headerVisible = null;
    this.modal = true;
    this.logicalGrid = scout.create('FormGrid');
    this.dialogs = [];
    this.views = [];
    this.messageBoxes = [];
    this.fileChoosers = [];
    this.focusedElement = null;
    this.closable = true;
    this.cacheBounds = false;
    this.cacheBoundsKey = null;
    this.resizable = true;
    this.movable = true;
    this.rootGroupBox = null;
    this.saveNeeded = false;
    this.saveNeededVisible = false;
    this.formController = null;
    this.messageBoxController = null;
    this.fileChooserController = null;
    this.closeKeyStroke = null;
    this.showOnOpen = true;
    this._glassPaneRenderer = null;
    this._preMaximizedBounds = null;
    this._resizeHandler = this._onResize.bind(this);
    this._windowResizeHandler = this._onWindowResize.bind(this);
    /**
     * Whether this form should render its initial focus
     */
    this.renderInitialFocusEnabled = true;

    this.$statusIcons = [];
    this.$header = null;
    this.$statusContainer = null;
    this.$close = null;
    this.$saveNeeded = null;
    this.$icon = null;
    this.$title = null;
    this.$subTitle = null;
    this.$dragHandle = null;
  }

  static DisplayHint = {
    DIALOG: 'dialog',
    POPUP_WINDOW: 'popupWindow',
    VIEW: 'view'
  };

  _init(model) {
    super._init(model);

    this.resolveTextKeys(['title', 'askIfNeedSaveText']);
    this.resolveIconIds(['iconId']);
    this._setDisplayParent(this.displayParent);
    this._setViews(this.views);
    this.formController = scout.create('FormController', {
      displayParent: this,
      session: this.session
    });

    this.messageBoxController = new MessageBoxController(this, this.session);
    this.fileChooserController = new FileChooserController(this, this.session);

    this._setRootGroupBox(this.rootGroupBox);
    this._setStatus(this.status);
    this.cacheBoundsKey = scout.nvl(model.cacheBoundsKey, this.objectType);
    this._installLifecycle();
    this._setClosable(this.closable);
  }

  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  _render() {
    this._renderForm();
  }

  /**
   * @override Widget.js
   */
  _renderProperties() {
    super._renderProperties();
    this._renderMaximized();
    this._renderMovable();
    this._renderResizable();
    this._renderHeaderVisible();
    this._renderCssClass();
    this._renderModal();

    this._installFocusContext();
    if (this.renderInitialFocusEnabled) {
      this.renderInitialFocus();
    }
  }

  _postRender() {
    super._postRender();

    // Render attached forms, message boxes and file choosers.
    this.formController.render();
    this.messageBoxController.render();
    this.fileChooserController.render();

    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.renderGlassPanes();
    }
  }

  _destroy() {
    super._destroy();
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer = null;
    }
  }

  _remove() {
    this.formController.remove();
    this.messageBoxController.remove();
    this.fileChooserController.remove();
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.removeGlassPanes();
      this._glassPaneRenderer = null;
    }

    this._uninstallFocusContext();
    this._removeHeader();
    this.$dragHandle = null;
    super._remove();
  }

  _renderForm() {
    let layout;

    this.$container = this.$parent.appendDiv('form')
      .data('model', this);

    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    if (this.isDialog()) {
      this.$container.addClass('dialog');
      layout = new DialogLayout(this);
      this.htmlComp.validateRoot = true;
      // Attach to capture phase to activate focus context before regular mouse down handlers may set the focus.
      // E.g. clicking a check box label on another dialog executes mouse down handler of the check box which will focus the box. This only works if the focus context of the dialog is active.
      this.$container[0].addEventListener('mousedown', this._onDialogMouseDown.bind(this), true);
    } else {
      layout = new FormLayout(this);
    }

    this.htmlComp.setLayout(layout);
    this.rootGroupBox.render();
  }

  _renderFocusedElement() {
    if (this.focusedElement) {
      this.focusedElement.focus();
      this.focusedElement = null;
    }
  }

  setModal(modal) {
    this.setProperty('modal', modal);
  }

  _renderModal() {
    if (this.parent instanceof WrappedFormField) {
      return;
    }
    if (this.modal && !this._glassPaneRenderer) {
      this._glassPaneRenderer = new GlassPaneRenderer(this);
      this._glassPaneRenderer.renderGlassPanes();
    } else if (!this.modal && this._glassPaneRenderer) {
      this._glassPaneRenderer.removeGlassPanes();
      this._glassPaneRenderer = null;
    }
  }

  _installLifecycle() {
    this.lifecycle = this._createLifecycle();
    this.lifecycle.handle('load', this._onLifecycleLoad.bind(this));
    this.lifecycle.handle('save', this._onLifecycleSave.bind(this));
    this.lifecycle.on('postLoad', this._onLifecyclePostLoad.bind(this));
    this.lifecycle.on('reset', this._onLifecycleReset.bind(this));
    this.lifecycle.on('close', this._onLifecycleClose.bind(this));
  }

  _createLifecycle() {
    return scout.create('FormLifecycle', {
      widget: this,
      askIfNeedSave: this.askIfNeedSave,
      askIfNeedSaveText: this.askIfNeedSaveText
    });
  }

  /**
   * Loads the data and renders the form afterwards by adding it to the desktop.
   * <p>
   * Calling this method is equivalent to calling load() first and once the promise is resolved, calling show().
   * <p>
   * Keep in mind that the form won't be rendered immediately after calling {@link open}. Because promises are always resolved asynchronously,
   * {@link show} will be called delayed even if {@link load} does nothing but return a resolved promise.<br>
   * This is only relevant if you need to access properties which are only available when the form is rendered (e.g. $container), which is not recommended anyway.
   * <p>
   *
   * @returns {Promise} the promise returned by the {@link load} function.
   */
  open() {
    return this.load()
      .then(() => {
        if (this.destroyed) {
          // If form has been closed right after it was opened don't try to show it
          return;
        }
        if (this.showOnOpen) {
          this.show();
        }
      });
  }

  /**
   * Initializes the life cycle and calls the {@link _load} function.
   * @returns {Promise} promise which is resolved when the form is loaded.
   */
  load() {
    return this.lifecycle.load();
  }

  /**
   * @returns {Promise} promise which is resolved when the form is loaded, respectively when the 'load' event is triggered'.
   */
  whenLoad() {
    return this.when('load');
  }

  /**
   * Lifecycle handle function registered for 'load'.
   *
   * @returns {Promise<T | void>|void}
   */
  _onLifecycleLoad() {
    try {
      return this._load()
        .then(data => {
          if (this.destroyed) {
            // If form has been closed right after it was opened ignore the load result
            return;
          }
          this.setData(data);
          this.importData();
          this.trigger('load');
        }).catch(error => {
          return this._handleLoadError(error);
        });
    } catch (error) {
      return this._handleLoadError(error);
    }
  }

  /**
   * This function is called when an error occurs while the <code>_load</code> function is called or when the <code>_load</code> function returns with a rejected promise.
   * By default the Form is destroyed and the error re-thrown so a caller of <code>Form.load()</code> may catch the error.
   *
   * @param {Error} error
   */
  _handleLoadError(error) {
    this.destroy();
    throw error;
  }

  /**
   * Method may be implemented to load the data. <br>
   * By default, a resolved promise containing the provided this.data is returned.
   * @returns {Promise}
   */
  _load() {
    return $.resolvedPromise().then(() => {
      return this.data;
    });
  }

  /**
   * @returns {Promise} promise which is resolved when the form is post loaded, respectively when the 'postLoad' event is triggered'.
   */
  whenPostLoad() {
    return this.when('postLoad');
  }

  _onLifecyclePostLoad() {
    return this._postLoad().then(() => {
      this.trigger('postLoad');
    });
  }

  _postLoad() {
    return $.resolvedPromise();
  }

  /**
   * @param {any} data
   */
  setData(data) {
    this.setProperty('data', data);
  }

  importData() {
    // NOP
  }

  /**
   * @returns {any}
   */
  exportData() {
    return null;
  }

  /**
   * Saves and closes the form.
   * @returns {Promise} promise which is resolved when the form is closed.
   */
  ok() {
    return this.lifecycle.ok();
  }

  /**
   * Saves the changes without closing the form.
   * @returns {Promise} promise which is resolved when the form is saved
   *    Note: it will be resolved even if the form does not require save and therefore even if {@link @_save} is not called.
   *    If you only want to be informed when save is required and {@link @_save} executed then you could use {@link whenSave()} or {@link on('save')} instead.
   */
  save() {
    return this.lifecycle.save();
  }

  /**
   * @returns {Promise} promise which is resolved when the form is saved, respectively when the 'save' event is triggered'.
   */
  whenSave() {
    return this.when('save');
  }

  _onLifecycleSave() {
    let data = this.exportData();
    return this._save(data).then(status => {
      this.setData(data);
      this.trigger('save');
      return status;
    });
  }

  /**
   * This function is called by the lifecycle, for instance when the 'ok' function is called.
   * The function is called every time the 'ok' function is called, which means it runs even when
   * there is not a single touched field. The function should be used to implement an overall validate
   * logic which is not related to a specific field. For instance you could validate the state of an
   * internal member variable.
   * <p>
   * You should return a Status object with severity ERROR in case the validation fails.
   *
   * @return {Status}
   */
  _validate() {
    return Status.ok();
  }

  /**
   * This function is called by the lifecycle, when the 'save' function is called.<p>
   * The data given to this function is the result of {@link exportData} which was called in advance.
   *
   * @returns {Promise} promise which may contain a Status specifying if the save operation was successful. The promise may be empty which means the save operation was successful.
   */
  _save(data) {
    return $.resolvedPromise();
  }

  /**
   * Resets the form to its initial state.
   * @returns {Promise}.
   */
  reset() {
    this.lifecycle.reset();
  }

  /**
   * @returns {Promise} promise which is resolved when the form is reset, respectively when the 'reset' event is triggered'.
   */
  whenReset() {
    return this.when('reset');
  }

  _onLifecycleReset() {
    this.trigger('reset');
  }

  /**
   * Closes the form if there are no changes made. Otherwise it shows a message box asking to save the changes.
   * @returns {Promise}.
   */
  cancel() {
    return this.lifecycle.cancel();
  }

  /**
   * @returns {Promise} promise which is resolved when the form is cancelled, respectively when the 'cancel' event is triggered'.
   */
  whenCancel() {
    return this.when('cancel');
  }

  /**
   * Closes the form and discards any unsaved changes.
   * @returns {Promise}.
   */
  close() {
    return this.lifecycle.close();
  }

  /**
   * @returns {Promise} promise which is resolved when the form is closed, respectively when the 'close' event is triggered'.
   */
  whenClose() {
    return this.when('close');
  }

  /**
   * Destroys the form and removes it from the desktop.
   */
  _onLifecycleClose() {
    let event = new Event();
    this.trigger('close', event);
    if (!event.defaultPrevented) {
      this._close();
    }
  }

  _close() {
    this.hide();
    this.destroy();
  }

  /**
   * This function is called when the user presses the "x" icon.<p>
   * It will either call {@link #close()} or {@link #cancel()), depending on the enabled and visible system buttons, see {@link _abort}.
   */
  abort() {
    let event = new Event();
    this.trigger('abort', event);
    if (!event.defaultPrevented) {
      this._abort();
    }
  }

  /**
   * @returns {Promise} promise which is resolved when the form is aborted, respectively when the 'abort' event is triggered'.
   */
  whenAbort() {
    return this.when('abort');
  }

  /**
   * Will call {@link #close()} if there is a close menu or button, otherwise {@link #cancel()) will be called.
   */
  _abort() {
    // Search for a close button in the menus and buttons of the root group box
    let hasCloseButton = this.rootGroupBox.controls
      .concat(this.rootGroupBox.menus)
      .filter(control => {
        let enabled = control.enabled;
        if (control.enabledComputed !== undefined) {
          enabled = control.enabledComputed; // Menus don't have enabledComputed, only form fields
        }
        return control.visible && enabled && control.systemType && control.systemType !== Button.SystemType.NONE;
      })
      .some(control => {
        return control.systemType === Button.SystemType.CLOSE;
      });

    if (hasCloseButton) {
      this.close();
    } else {
      this.cancel();
    }
    this._afterAbort();
  }

  _afterAbort() {
    if (!this.destroyed && this.isShown()) {
      // If the form is still shown after an abort request, something (e.g. a validation message box) is probably open
      // -> activate the form to show the validation message
      // Checking for destroyed would be sufficient for most cases. But maybe a certain form does not really close the form on an abort request but just hides it. This is where isShown comes in.
      this.activate();
    }
  }

  /**
   * @param {ValidationResult} validationResult
   */
  revealInvalidField(validationResult) {
    if (!validationResult) {
      return;
    }
    this._revealInvalidField(validationResult);
  }

  /**
   * @param {ValidationResult} validationResult
   */
  _revealInvalidField(validationResult) {
    let event = this._createRevealInvalidFieldEvent(validationResult);
    this.trigger('revealInvalidField', event);
    if (event.defaultPrevented) {
      return;
    }
    validationResult.reveal();
  }

  /**
   * @param {ValidationResult} validationResult
   */
  _createRevealInvalidFieldEvent(validationResult) {
    return new Event({validationResult: validationResult});
  }

  /**
   * Override this method to provide a key stroke which closes the form.
   * The default implementation returns an AbortKeyStroke which handles the ESC key and calls {@link abort}.
   * <p>
   * The key stroke is only active if {@link this.closable} is set to true.
   * @return KeyStroke
   */
  _createCloseKeyStroke() {
    return new AbortKeyStroke(this, () => this.$close);
  }

  _setClosable(closable) {
    this._setProperty('closable', closable);
    if (this.closable) {
      this.closeKeyStroke = this._createCloseKeyStroke();
      this.keyStrokeContext.registerKeyStroke(this.closeKeyStroke);
    } else {
      this.keyStrokeContext.unregisterKeyStroke(this.closeKeyStroke);
    }
  }

  setClosable(closable) {
    this.setProperty('closable', closable);
  }

  _renderClosable() {
    if (!this.$header) {
      return;
    }
    this.$container.toggleClass('closable');
    if (this.closable) {
      if (this.$close) {
        return;
      }
      this.$close = this.$statusContainer.appendDiv('status closer')
        .on('click', this._onCloseIconClick.bind(this));
    } else {
      if (!this.$close) {
        return;
      }
      this.$close.remove();
      this.$close = null;
    }
  }

  _onCloseIconClick() {
    this.abort();
  }

  setResizable(resizable) {
    this.setProperty('resizable', resizable);
  }

  _renderResizable() {
    if (this.resizable && this.isDialog() && !this.maximized) {
      this.$container
        .resizable()
        .on('resize', this._resizeHandler);
    } else {
      this.$container
        .unresizable()
        .off('resize', this._resizeHandler);
    }
  }

  _onResize(event) {
    let autoSizeOld = this.htmlComp.layout.autoSize;
    this.htmlComp.layout.autoSize = false;
    this.htmlComp.revalidateLayout();
    this.htmlComp.layout.autoSize = autoSizeOld;
    this.updateCacheBounds();
    return false;
  }

  setMovable(movable) {
    this.setProperty('movable', movable);
  }

  _renderMovable() {
    if (this.movable && this.isDialog() && !this.maximized) {
      if (this.$dragHandle) {
        return;
      }
      this.$dragHandle = this.$container.prependDiv('drag-handle');
      this.$container.draggable(this.$dragHandle, $.throttle(this._onMove.bind(this), 1000 / 60)); // 60fps
    } else {
      if (!this.$dragHandle) {
        return;
      }
      this.$container.undraggable(this.$dragHandle);
      this.$dragHandle.remove();
      this.$dragHandle = null;
    }
  }

  _onDialogMouseDown() {
    this.activate();
  }

  activate() {
    this.session.desktop.activateForm(this);
  }

  show() {
    this.session.desktop.showForm(this);
  }

  hide() {
    this.session.desktop.hideForm(this);
  }

  /**
   * Checks whether the form is shown, which means whether a form has been added to the form stack of the display parent, e.g. by using {@link showForm}.<br>
   * It does not necessarily mean the user can see the content of the form for sure,
   * e.g. if the form is opened as a view the tab may be inactive because another view is active, or in case of a dialog it may be hidden behind another dialog or shown in an inactive view.
   *
   * @returns {boolean}
   */
  isShown() {
    return this.session.desktop.isFormShown(this);
  }

  _renderHeader() {
    this.$header = this.$container.prependDiv('header');
    this.$statusContainer = this.$header.appendDiv('status-container');
    this.$icon = this.$header.appendDiv('icon-container');

    this.$title = this.$header.appendDiv('title');
    tooltips.installForEllipsis(this.$title, {
      parent: this
    });

    this.$subTitle = this.$header.appendDiv('sub-title');
    tooltips.installForEllipsis(this.$subTitle, {
      parent: this
    });
    this._renderTitle();
    this._renderSubTitle();
    this._renderIconId();
    this._renderStatus();
    this._renderClosable();
    this._renderSaveNeeded();
  }

  _removeHeader() {
    if (this.$title) {
      tooltips.uninstall(this.$title);
    }
    if (this.$subTitle) {
      tooltips.uninstall(this.$subTitle);
    }
    if (this.$header) {
      this.$header.remove();
      this.$header = null;
    }
    this.$title = null;
    this.$subTitle = null;
    this.$statusContainer = null;
    this.$icon = null;
    this.$close = null;
    this.$saveNeeded = null;
  }

  _setRootGroupBox(rootGroupBox) {
    this._setProperty('rootGroupBox', rootGroupBox);
    if (this.rootGroupBox) {
      this.rootGroupBox.setMainBox(true);

      if (this.isDialog() || this.parent instanceof WrappedFormField) {
        this.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);
      }
    }
  }

  _renderSaveNeeded() {
    if (!this.$header) {
      return;
    }
    if (this.saveNeeded && this.saveNeededVisible) {
      this.$container.addClass('save-needed');
      if (this.$saveNeeded) {
        return;
      }
      if (this.$close) {
        this.$saveNeeded = this.$close.beforeDiv('status save-needer');
      } else {
        this.$saveNeeded = this.$statusContainer
          .appendDiv('status save-needer');
      }
    } else {
      this.$container.removeClass('save-needed');
      if (!this.$saveNeeded) {
        return;
      }
      this.$saveNeeded.remove();
      this.$saveNeeded = null;
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  setAskIfNeedSave(askIfNeedSave) {
    this.setProperty('askIfNeedSave', askIfNeedSave);
    if (this.lifecycle) {
      this.lifecycle.setAskIfNeedSave(askIfNeedSave);
    }
  }

  setDisplayViewId(displayViewId) {
    this.setProperty('displayViewId', displayViewId);
  }

  setDisplayHint(displayHint) {
    this.setProperty('displayHint', displayHint);
  }

  setSaveNeededVisible(visible) {
    this.setProperty('saveNeededVisible', visible);
  }

  _renderSaveNeededVisible() {
    this._renderSaveNeeded();
  }

  _renderCssClass(cssClass, oldCssClass) {
    cssClass = cssClass || this.cssClass;
    this.$container.removeClass(oldCssClass);
    this.$container.addClass(cssClass);
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  setStatus(status) {
    this.setProperty('status', status);
  }

  _setStatus(status) {
    status = Status.ensure(status);
    this._setProperty('status', status);
  }

  _renderStatus() {
    if (!this.$header) {
      return;
    }

    this.$statusIcons.forEach($icn => {
      $icn.remove();
    });

    this.$statusIcons = [];

    if (this.status) {
      let statusList = this.status.asFlatList();
      let $prevIcon;
      statusList.forEach(sts => {
        $prevIcon = this._renderSingleStatus(sts, $prevIcon);
        if ($prevIcon) {
          this.$statusIcons.push($prevIcon);
        }
      });
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  _renderSingleStatus(status, $prevIcon) {
    if (status && status.iconId) {
      let $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
      if (status.cssClass()) {
        $statusIcon.addClass(status.cssClass());
      }
      $statusIcon.prependTo(this.$statusContainer);
      return $statusIcon;
    }
    return $prevIcon;
  }

  setShowOnOpen(showOnOpen) {
    this.setProperty('showOnOpen', showOnOpen);
  }

  _updateTitleForWindow() {
    let formTitle = strings.join(' - ', this.title, this.subTitle),
      applicationTitle = this.session.desktop.title;
    this.popupWindow.title(formTitle || applicationTitle);
  }

  _updateTitleForDom() {
    let titleText = this.title;
    if (!titleText && this.closable) {
      // Add '&nbsp;' to prevent title-box of a closable form from collapsing if title is empty
      titleText = strings.plainText('&nbsp;');
    }
    if (titleText || this.subTitle) {
      let $titles = getOrAppendChildDiv(this.$container, 'title-box');
      // Render title
      if (titleText) {
        getOrAppendChildDiv($titles, 'title')
          .text(titleText)
          .icon(this.iconId);
      } else {
        removeChildDiv($titles, 'title');
      }
      // Render subTitle
      if (strings.hasText(titleText)) {
        getOrAppendChildDiv($titles, 'sub-title').text(this.subTitle);
      } else {
        removeChildDiv($titles, 'sub-title');
      }
    } else {
      removeChildDiv(this.$container, 'title-box');
    }

    // ----- Helper functions -----

    function getOrAppendChildDiv($parent, cssClass) {
      let $div = $parent.children('.' + cssClass);
      if ($div.length === 0) {
        $div = this.$parent.appendDiv(cssClass);
      }
      return $div;
    }

    function removeChildDiv($parent, cssClass) {
      $parent.children('.' + cssClass).remove();
    }
  }

  isDialog() {
    return this.displayHint === Form.DisplayHint.DIALOG;
  }

  isPopupWindow() {
    return this.displayHint === Form.DisplayHint.POPUP_WINDOW;
  }

  isView() {
    return this.displayHint === Form.DisplayHint.VIEW;
  }

  _onMove(newOffset) {
    this.trigger('move', newOffset);
    this.updateCacheBounds();
  }

  moveTo(position) {
    this.$container.cssPosition(position);
    this.trigger('move', {
      left: position.x,
      top: position.y
    });
    this.updateCacheBounds();
  }

  position() {
    let position;
    let prefBounds = this.prefBounds();
    if (prefBounds) {
      position = prefBounds.point();
    } else {
      position = DialogLayout.positionContainerInWindow(this.$container);
    }
    this.moveTo(position);
  }

  updateCacheBounds() {
    if (this.cacheBounds && !this.maximized) {
      this.storeCacheBounds(this.htmlComp.bounds());
    }
  }

  appendTo($parent) {
    this.$container.appendTo($parent);
  }

  setHeaderVisible(headerVisible) {
    this.setProperty('headerVisible', headerVisible);
  }

  _renderHeaderVisible() {
    let headerVisible = this.headerVisible === null ? this.isDialog() : this.headerVisible;
    if (headerVisible && !this.$header) {
      this._renderHeader();
    } else if (!headerVisible && this.$header) {
      this._removeHeader();
    }
    // If header contains no title it won't be a real header, it will be on the top right corner just containing icons.
    let noTitleHeader = this.$header && this.$header.hasClass('no-title');
    this.$container.toggleClass('header-visible', headerVisible && !noTitleHeader);
    this.invalidateLayoutTree();
  }

  setTitle(title) {
    this.setProperty('title', title);
  }

  _renderTitle() {
    if (this.$header) {
      this.$title.text(this.title);
      this.$header.toggleClass('no-title', !this.title && !this.subTitle);
    }
    if (this.isPopupWindow()) {
      this._updateTitleForWindow();
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  setSubTitle(subTitle) {
    this.setProperty('subTitle', subTitle);
  }

  _renderSubTitle() {
    if (this.$header) {
      this.$subTitle.text(this.subTitle);
      this.$header.toggleClass('no-title', !this.title && !this.subTitle);
    }
    if (this.isPopupWindow()) {
      this._updateTitleForWindow();
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  _renderIconId() {
    if (this.$header) {
      this.$icon.icon(this.iconId);
      // Layout could have been changed, e.g. if subtitle becomes visible
      this.invalidateLayoutTree();
    }
  }

  _setViews(views) {
    if (views) {
      views.forEach(view => {
        view.setDisplayParent(this);
      });
    }
    this._setProperty('views', views);
  }

  /**
   * @override Widget.js
   */
  setDisabledStyle(disabledStyle) {
    this.rootGroupBox.setDisabledStyle(disabledStyle);
  }

  setDisplayParent(displayParent) {
    this.setProperty('displayParent', displayParent);
  }

  _setDisplayParent(displayParent) {
    if (displayParent instanceof Form && displayParent.parent instanceof WrappedFormField && displayParent.isView()) {
      displayParent = Form.findNonWrappedForm(displayParent);
    }
    this._setProperty('displayParent', displayParent);

    if (displayParent) {
      this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
    }
  }

  setMaximized(maximized) {
    this.setProperty('maximized', maximized);
  }

  _renderMaximized() {
    if (!this.isDialog()) {
      return;
    }
    if (this.maximized && this.htmlComp.layouted) {
      // Store the current bounds before it is maximized.
      // The layout will read it using this.prefBounds() the first time it is called when maximized is set to false.
      this._preMaximizedBounds = this.htmlComp.bounds();
    }
    this._maximize();
    if (!this.maximized) {
      this._preMaximizedBounds = null;
    }

    if (this.maximized) {
      this.$container.window()
        .on('resize', this._windowResizeHandler);
    } else {
      this.$container.window()
        .off('resize', this._windowResizeHandler);
    }
    if (this.rendered) {
      // Remove move and resize handles when maximized
      this._renderMovable();
      this._renderResizable();
    }
  }

  _onWindowResize() {
    if (this.maximized) {
      this._maximize();
    }
  }

  _maximize() {
    if (!this.rendered) {
      return;
    }
    let shrinkEnabled = this.htmlComp.layout.shrinkEnabled;
    this.htmlComp.layout.shrinkEnabled = true;
    this.revalidateLayoutTree();
    this.position();
    this.htmlComp.layout.shrinkEnabled = shrinkEnabled;
  }

  prefBounds() {
    if (this.maximized) {
      return null;
    }
    if (this._preMaximizedBounds) {
      return this._preMaximizedBounds;
    }
    if (this.cacheBounds) {
      return this.readCacheBounds();
    }
    return null;
  }

  _attach() {
    this.$parent.append(this.$container);

    // If the parent was resized while this view was detached, the view has a wrong size.
    if (this.isView()) {
      this.invalidateLayoutTree(false);
    }

    // form is attached even if children are not yet
    if ((this.isView() || this.isDialog()) && !this.detailForm) {
      // notify model this form is active
      this.session.desktop._setFormActivated(this);
    }
    super._attach();
  }

  /**
   * Method invoked when:
   *  - this is a 'detailForm' and the outline content is displayed;
   *  - this is a 'view' and the view tab is selected;
   *  - this is a child 'dialog' or 'view' and its 'displayParent' is attached;
   * @override Widget.js
   */
  _postAttach() {
    // Attach child dialogs, message boxes and file choosers.
    this.formController.attachDialogs();
    this.messageBoxController.attach();
    this.fileChooserController.attach();

    super._attach();
  }

  /**
   * Method invoked when:
   *  - this is a 'detailForm' and the outline content is hidden;
   *  - this is a 'view' and the view tab is deselected;
   *  - this is a child 'dialog' or 'view' and its 'displayParent' is detached;
   * @override Widget.js
   */
  _detach() {
    // Detach child dialogs, message boxes and file choosers, not views.
    this.formController.detachDialogs();
    this.messageBoxController.detach();
    this.fileChooserController.detach();

    this.$container.detach();
    super._detach();
  }

  renderInitialFocus() {
    let focused = false;
    if (this.initialFocus) {
      focused = this.initialFocus.focus();
    } else {
      // If no explicit focus is requested, try to focus the first focusable element.
      // Do it only if the focus is not already on an element in the form (e.g. focus could have been requested explicitly by a child element)
      // And only if the context belonging to that element is ready. Not ready means, some other widget (probably an outer container) is still preparing the context and will do the initial focus later
      if (!this.$container.isOrHas(this.$container.activeElement())) {
        let focusManager = this.session.focusManager;
        focused = focusManager.requestFocusIfReady(focusManager.findFirstFocusableElement(this.$container));
      }
    }
    if (focused) {
      // If the focus widget is outside of the view area the browser tries to scroll the widget into view.
      // If the scroll area contains large (not absolutely positioned) content it can happen that the browsers scrolls the content even though the focused widget already is in the view area.
      // This is probably because the focus happens while the form is not layouted yet. We should actually refactor this and do the focusing after layouting, but this would be a bigger change.
      // The current workaround is to revert the scrolling done by the browser. Automatic scrolling to the focused widget when the form is not layouted does not work anyway.
      let $scrollParent = this.$container.activeElement().scrollParent();
      $scrollParent.scrollTop(0);
      $scrollParent.scrollLeft(0);
    }
  }

  /**
   * This method returns the HtmlElement (DOM node) which is used by FocusManager/FocusContext/Popup
   * to focus the initial element. The impl. of these classes relies on HtmlElements, so we can not
   * easily use the focus() method of FormField here. Furthermore, some classes like Button
   * are sometimes 'adapted' by a ButtonAdapterMenu, which means the Button itself is not rendered, but
   * we must know the $container of the adapter menu to focus the correct element. That's why we call
   * the getFocusableElement() method.
   */
  _initialFocusElement() {
    let focusElement;
    if (this.initialFocus) {
      focusElement = this.initialFocus.getFocusableElement();
    }
    if (!focusElement) {
      focusElement = this.session.focusManager.findFirstFocusableElement(this.$container);
    }
    return focusElement;
  }

  _installFocusContext() {
    if (this.isDialog() || this.isPopupWindow()) {
      this.session.focusManager.installFocusContext(this.$container, FocusRule.NONE);
    }
  }

  _uninstallFocusContext() {
    if (this.isDialog() || this.isPopupWindow()) {
      this.session.focusManager.uninstallFocusContext(this.$container);
    }
  }

  touch() {
    this.rootGroupBox.touch();
  }

  /**
   * Function required for objects that act as 'displayParent'.
   *
   * @return {boolean} 'true' if this Form is currently accessible to the user
   */
  inFront() {
    return this.rendered && this.attached;
  }

  /**
   * Visits all form-fields of this form in pre-order (top-down).
   */
  visitFields(visitor) {
    this.rootGroupBox.visitFields(visitor);
  }

  /**
   * Visits all dialogs, messageBoxes and fileChoosers of this form in pre-order (top-down).
   * filter is an optional parameter.
   */
  visitDisplayChildren(visitor, filter) {
    if (!filter) {
      filter = displayChild => true;
    }

    let visitorFunc = child => {
      visitor(child);
      // only forms provide a deeper hierarchy
      if (child instanceof Form) {
        child.visitDisplayChildren(visitor, filter);
      }
    };
    this.dialogs.filter(filter).forEach(visitorFunc, this);
    this.messageBoxes.filter(filter).forEach(visitorFunc, this);
    this.fileChoosers.filter(filter).forEach(visitorFunc, this);
  }

  storeCacheBounds(bounds) {
    if (this.cacheBounds) {
      let storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
      webstorage.setItemToLocalStorage(storageKey, JSON.stringify(bounds));
    }
  }

  readCacheBounds() {
    if (!this.cacheBounds) {
      return null;
    }

    let storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
    let bounds = webstorage.getItemFromLocalStorage(storageKey);
    if (!bounds) {
      return null;
    }
    bounds = JSON.parse(bounds);
    return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  /**
   * @returns {Form} the form the widget belongs to (returns the first parent which is a {@link Form}.
   */
  static findForm(widget) {
    let parent = widget.parent;
    while (parent && !(parent instanceof Form)) {
      parent = parent.parent;
    }
    return parent;
  }

  /**
   * @returns {Form} the first form which is not an inner form of a wrapped form field
   */
  static findNonWrappedForm(widget) {
    if (!widget) {
      return null;
    }
    let form = null;
    widget.findParent(parent => {
      if (parent instanceof Form) {
        form = parent;
        // If form is an inner form of a wrapped form field -> continue search
        if (form.parent instanceof WrappedFormField) {
          return false;
        }
        // Otherwise use that form
        return true;
      }
    });
    return form;
  }
}
