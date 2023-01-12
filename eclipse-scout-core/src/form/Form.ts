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
  AbortKeyStroke, Button, ButtonSystemType, DialogLayout, DisabledStyle, DisplayParent, DisplayViewId, EnumObject, Event, FileChooser, FileChooserController, FocusRule, FormController, FormEventMap, FormGrid, FormLayout, FormLifecycle,
  FormModel, FormRevealInvalidFieldEvent, GlassPaneRenderer, GroupBox, HtmlComponent, InitModelOf, KeyStroke, KeyStrokeContext, MessageBox, MessageBoxController, ObjectOrChildModel, Point, PopupWindow, Rectangle, scout, Status,
  StatusOrModel, strings, tooltips, TreeVisitResult, ValidationResult, webstorage, Widget, WrappedFormField
} from '../index';
import $ from 'jquery';

export type DisplayHint = EnumObject<typeof Form.DisplayHint>;

export class Form extends Widget implements FormModel, DisplayParent {
  declare model: FormModel;
  declare eventMap: FormEventMap;
  declare self: Form;

  animateOpening: boolean;
  askIfNeedSave: boolean;
  askIfNeedSaveText: string;
  data: any;
  displayViewId: DisplayViewId;
  displayHint: DisplayHint;
  maximized: boolean;
  headerVisible: boolean;
  modal: boolean;
  displayParent: DisplayParent;
  dialogs: Form[];
  views: Form[];
  messageBoxes: MessageBox[];
  fileChoosers: FileChooser[];
  focusedElement: Widget;
  closable: boolean;
  cacheBounds: boolean;
  cacheBoundsKey: string;
  resizable: boolean;
  movable: boolean;
  rootGroupBox: GroupBox;
  saveNeeded: boolean;
  saveNeededVisible: boolean;
  formController: FormController;
  messageBoxController: MessageBoxController;
  fileChooserController: FileChooserController;
  closeKeyStroke: KeyStroke;
  showOnOpen: boolean;
  initialFocus: Widget;
  renderInitialFocusEnabled: boolean;
  /** set by PopupWindow if this Form has displayHint=Form.DisplayHint.POPUP_WINDOW */
  popupWindow: PopupWindow;

  title: string;
  subTitle: string;
  iconId: string;
  status: Status;
  uiCssClass: string;
  lifecycle: FormLifecycle;
  detailForm: boolean;

  $statusIcons: JQuery[];
  $header: JQuery;
  $statusContainer: JQuery;
  $close: JQuery;
  $saveNeeded: JQuery;
  $icon: JQuery;
  $title: JQuery;
  $subTitle: JQuery;
  $dragHandle: JQuery;
  protected _glassPaneRenderer: GlassPaneRenderer;
  protected _preMaximizedBounds: Rectangle;
  protected _resizeHandler: (Event) => boolean;
  protected _windowResizeHandler: () => void;

  constructor() {
    super();
    this._addWidgetProperties(['rootGroupBox', 'views', 'dialogs', 'initialFocus', 'messageBoxes', 'fileChoosers']);
    this._addPreserveOnPropertyChangeProperties(['initialFocus']);

    this.animateOpening = true;
    this.askIfNeedSave = true;
    this.askIfNeedSaveText = null;
    this.data = {};
    this.displayViewId = null;
    this.displayHint = Form.DisplayHint.DIALOG;
    this.displayParent = null; // only relevant if form is opened, not relevant if form is just rendered into another widget (not managed by a form controller)
    this.maximized = false;
    this.headerVisible = null;
    this.modal = true;
    this.logicalGrid = scout.create(FormGrid);
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
    this.initialFocus = null;
    this.renderInitialFocusEnabled = true;
    this.popupWindow = null;
    this.title = null;
    this.subTitle = null;
    this.iconId = null;

    this.$statusIcons = [];
    this.$header = null;
    this.$statusContainer = null;
    this.$close = null;
    this.$saveNeeded = null;
    this.$icon = null;
    this.$title = null;
    this.$subTitle = null;
    this.$dragHandle = null;
    this._glassPaneRenderer = null;
    this._preMaximizedBounds = null;
    this._resizeHandler = this._onResize.bind(this);
    this._windowResizeHandler = this._onWindowResize.bind(this);
  }

  static DisplayHint = {
    DIALOG: 'dialog',
    POPUP_WINDOW: 'popupWindow',
    VIEW: 'view'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.resolveTextKeys(['title', 'askIfNeedSaveText']);
    this.resolveIconIds(['iconId']);
    this._setDisplayParent(this.displayParent);
    this._setViews(this.views);
    this.formController = scout.create(FormController, {
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

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _render() {
    this._renderForm();
  }

  protected override _renderProperties() {
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

  protected override _postRender() {
    super._postRender();

    // Render attached forms, message boxes and file choosers.
    this.formController.render();
    this.messageBoxController.render();
    this.fileChooserController.render();

    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.renderGlassPanes();
    }
  }

  protected override _destroy() {
    super._destroy();
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer = null;
    }
  }

  protected override _remove() {
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

  protected _renderForm() {
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
      // E.g. clicking a checkbox label on another dialog executes mouse down handler of the checkbox which will focus the box. This only works if the focus context of the dialog is active.
      this.$container[0].addEventListener('mousedown', this._onDialogMouseDown.bind(this), true);
    } else {
      if (this.isPopupWindow()) {
        this.$container.addClass('popup-window');
      }
      layout = new FormLayout(this);
    }

    this.htmlComp.setLayout(layout);
    this.rootGroupBox.render();
  }

  /** @see FormModel.modal */
  setModal(modal: boolean) {
    this.setProperty('modal', modal);
  }

  protected _renderModal() {
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

  protected _installLifecycle() {
    this.lifecycle = this._createLifecycle();
    this.lifecycle.handle('load', this._onLifecycleLoad.bind(this));
    this.lifecycle.handle('save', this._onLifecycleSave.bind(this));
    this.lifecycle.on('postLoad', this._onLifecyclePostLoad.bind(this));
    this.lifecycle.on('reset', this._onLifecycleReset.bind(this));
    this.lifecycle.on('close', this._onLifecycleClose.bind(this));
  }

  protected _createLifecycle(): FormLifecycle {
    return scout.create(FormLifecycle, {
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
   */
  open(): JQuery.Promise<void> {
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
   * @returns promise which is resolved when the form is loaded.
   */
  load(): JQuery.Promise<void> {
    return this.lifecycle.load();
  }

  /**
   * @returns promise which is resolved when the form is loaded, respectively when the 'load' event is triggered.
   */
  whenLoad(): JQuery.Promise<Event<Form>> {
    return this.when('load');
  }

  /**
   * Lifecycle handle function registered for 'load'.
   */
  protected _onLifecycleLoad(): JQuery.Promise<Status> {
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
   * This function is called when an error occurs while the {@link _load} function is called or when the {@link _load} function returns with a rejected promise.
   * By default, the Form is destroyed and the error re-thrown so a caller of {@link Form.load()} may catch the error.
   *
   */
  protected _handleLoadError(error: Error): JQuery.Promise<Status> {
    this.close();
    throw error;
  }

  /**
   * Method may be implemented to load the data.
   * By default, a resolved promise containing the provided {@link this.data} is returned.
   */
  protected _load(): JQuery.Promise<object> {
    return $.resolvedPromise().then(() => {
      return this.data;
    });
  }

  /**
   * @returns promise which is resolved when the form is post loaded, respectively when the 'postLoad' event is triggered.
   */
  whenPostLoad(): JQuery.Promise<Event<Form>> {
    return this.when('postLoad');
  }

  protected _onLifecyclePostLoad(): JQuery.Promise<void> {
    return this._postLoad().then(() => {
      this.trigger('postLoad');
    });
  }

  protected _postLoad(): JQuery.Promise<void> {
    return $.resolvedPromise();
  }

  /** @see FormModel.data */
  setData(data: any) {
    this.setProperty('data', data);
  }

  importData() {
    // NOP
  }

  exportData(): any {
    return null;
  }

  /**
   * Saves and closes the form.
   * @returns promise which is resolved when the form is closed.
   */
  ok(): JQuery.Promise<void> {
    return this.lifecycle.ok();
  }

  /**
   * Saves the changes without closing the form.
   * @returns promise which is resolved when the form is saved
   *    Note: it will be resolved even if the form does not require save and therefore even if {@link @_save} is not called.
   *    If you only want to be informed when save is required and {@link @_save} executed then you could use {@link whenSave()} or {@link on('save')} instead.
   */
  save(): JQuery.Promise<void> {
    return this.lifecycle.save();
  }

  /**
   * @returns promise which is resolved when the form is saved, respectively when the 'save' event is triggered.
   */
  whenSave(): JQuery.Promise<Event<Form>> {
    return this.when('save');
  }

  protected _onLifecycleSave(): JQuery.Promise<Status> {
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
   * logic which is not related to a specific field. For instance, you could validate the state of an
   * internal member variable.
   * <p>
   * You should return a Status object with severity ERROR in case the validation fails.
   * @internal
   */
  _validate(): Status {
    return Status.ok();
  }

  /**
   * This function is called by the lifecycle, when {@link save} is called.
   *
   * The data given to this function is the result of {@link exportData} which was called in advance.
   *
   * @returns a promise which may contain a {@link Status} specifying if the save operation was successful. The promise may be empty which means the save operation was successful.
   */
  protected _save(data: object): JQuery.Promise<Status> {
    return $.resolvedPromise();
  }

  /**
   * Resets the form to its initial state.
   */
  reset(): JQuery.Promise<void> {
    return this.lifecycle.reset();
  }

  /**
   * @returns promise which is resolved when the form is reset, respectively when the 'reset' event is triggered.
   */
  whenReset(): JQuery.Promise<Event<Form>> {
    return this.when('reset');
  }

  protected _onLifecycleReset() {
    this.trigger('reset');
  }

  /**
   * Closes the form if there are no changes made. Otherwise, it shows a message box asking to save the changes.
   */
  cancel(): JQuery.Promise<void> {
    return this.lifecycle.cancel();
  }

  /**
   * Closes the form and discards any unsaved changes.
   */
  close(): JQuery.Promise<void> {
    return this.lifecycle.close();
  }

  /**
   * @returns promise which is resolved when the form is closed, respectively when the 'close' event is triggered.
   */
  whenClose(): JQuery.Promise<Event<Form>> {
    return this.when('close');
  }

  /**
   * Destroys the form and removes it from the desktop.
   */
  protected _onLifecycleClose() {
    let event = new Event();
    this.trigger('close', event);
    if (!event.defaultPrevented) {
      this._close();
    }
  }

  protected _close() {
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
   * @returns promise which is resolved when the form is aborted, respectively when the 'abort' event is triggered.
   */
  whenAbort(): JQuery.Promise<Event<Form>> {
    return this.when('abort');
  }

  /**
   * Will call {@link #close()} if there is a close menu or button, otherwise {@link #cancel()) will be called.
   */
  protected _abort() {
    // Search for a close button in the menus and buttons of the root group box
    let controls: (Widget & { systemType?: ButtonSystemType })[] = this.rootGroupBox.controls;
    controls = controls.concat(this.rootGroupBox.menus);
    let hasCloseButton = controls
      .filter(control => {
        let enabled = control.enabled;
        if (control.enabledComputed !== undefined) {
          enabled = control.enabledComputed; // Menus don't have enabledComputed, only form fields
        }
        return control.visible && enabled && control.systemType;
      })
      .some(control => control.systemType === Button.SystemType.CLOSE);

    if (hasCloseButton) {
      this.close();
    } else {
      this.cancel();
    }
    this._afterAbort();
  }

  /** @internal */
  _afterAbort() {
    if (!this.destroyed && this.isShown()) {
      // If the form is still shown after an abort request, something (e.g. a validation message box) is probably open
      // -> activate the form to show the validation message
      // Checking for destroyed would be sufficient for most cases. But maybe a certain form does not really close the form on an abort request but just hides it. This is where isShown comes in.
      this.activate();
    }
  }

  revealInvalidField(validationResult: ValidationResult) {
    if (!validationResult) {
      return;
    }
    this._revealInvalidField(validationResult);
  }

  protected _revealInvalidField(validationResult: ValidationResult) {
    let event = this._createRevealInvalidFieldEvent(validationResult);
    this.trigger('revealInvalidField', event);
    if (event.defaultPrevented) {
      return;
    }
    validationResult.reveal();
  }

  protected _createRevealInvalidFieldEvent(validationResult: ValidationResult): FormRevealInvalidFieldEvent {
    return new Event({validationResult: validationResult}) as FormRevealInvalidFieldEvent;
  }

  /**
   * Override this method to provide a keystroke which closes the form.
   * The default implementation returns an AbortKeyStroke which handles the ESC key and calls {@link abort}.
   * <p>
   * The key stroke is only active if {@link this.closable} is set to true.
   */
  protected _createCloseKeyStroke(): KeyStroke {
    return new AbortKeyStroke(this, () => this.$close);
  }

  protected _setClosable(closable: boolean) {
    this._setProperty('closable', closable);
    if (this.closable) {
      this.closeKeyStroke = this._createCloseKeyStroke();
      this.keyStrokeContext.registerKeyStroke(this.closeKeyStroke);
    } else {
      this.keyStrokeContext.unregisterKeyStroke(this.closeKeyStroke);
    }
  }

  /** @see FormModel.closable */
  setClosable(closable: boolean) {
    this.setProperty('closable', closable);
  }

  protected _renderClosable() {
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

  protected _onCloseIconClick() {
    this.abort();
  }

  /** @see FormModel.resizable */
  setResizable(resizable: boolean) {
    this.setProperty('resizable', resizable);
  }

  protected _renderResizable() {
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

  protected _onResize(event: Event): boolean {
    let layout = this.htmlComp.layout as DialogLayout,
      autoSizeOld = layout.autoSize;
    layout.autoSize = false;
    this.htmlComp.revalidateLayout();
    layout.autoSize = autoSizeOld;
    this.updateCacheBounds();
    return false;
  }

  /** @see FormModel.movable */
  setMovable(movable: boolean) {
    this.setProperty('movable', movable);
  }

  protected _renderMovable() {
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

  protected _onDialogMouseDown() {
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
   */
  isShown(): boolean {
    return this.session.desktop.isFormShown(this);
  }

  protected _renderHeader() {
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

  protected _removeHeader() {
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

  /** @see FormModel.rootGroupBox */
  setRootGroupBox(rootGroupBox: ObjectOrChildModel<GroupBox>) {
    this.setProperty('rootGroupBox', rootGroupBox);
  }

  protected _setRootGroupBox(rootGroupBox: GroupBox) {
    this._setProperty('rootGroupBox', rootGroupBox);
    if (this.rootGroupBox) {
      this.rootGroupBox.setMainBox(true);
    }
  }

  protected _renderSaveNeeded() {
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

  /** @see FormModel.askIfNeedSave */
  setAskIfNeedSave(askIfNeedSave: boolean) {
    this.setProperty('askIfNeedSave', askIfNeedSave);
    if (this.lifecycle) {
      this.lifecycle.setAskIfNeedSave(askIfNeedSave);
    }
  }

  /** @see FormModel.displayViewId */
  setDisplayViewId(displayViewId: DisplayViewId) {
    this.setProperty('displayViewId', displayViewId);
  }

  /** @see FormModel.displayHint */
  setDisplayHint(displayHint: DisplayHint) {
    this.setProperty('displayHint', displayHint);
  }

  /** @see FormModel.saveNeededVisible */
  setSaveNeededVisible(visible: boolean) {
    this.setProperty('saveNeededVisible', visible);
  }

  protected _renderSaveNeededVisible() {
    this._renderSaveNeeded();
  }

  protected override _renderCssClass(cssClass?: string, oldCssClass?: string) {
    cssClass = cssClass || this.cssClass;
    this.$container.removeClass(oldCssClass);
    this.$container.addClass(cssClass);
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  /** @see FormModel.status */
  setStatus(status: StatusOrModel) {
    this.setProperty('status', status);
  }

  protected _setStatus(status: StatusOrModel) {
    status = Status.ensure(status);
    this._setProperty('status', status);
  }

  protected _renderStatus() {
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

  protected _renderSingleStatus(status: Status, $prevIcon: JQuery): JQuery {
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

  /** @see FormModel.showOnOpen */
  setShowOnOpen(showOnOpen: boolean) {
    this.setProperty('showOnOpen', showOnOpen);
  }

  protected _updateTitleForDom() {
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

  isDialog(): boolean {
    return this.displayHint === Form.DisplayHint.DIALOG;
  }

  isPopupWindow(): boolean {
    return this.displayHint === Form.DisplayHint.POPUP_WINDOW;
  }

  isView(): boolean {
    return this.displayHint === Form.DisplayHint.VIEW;
  }

  protected _onMove(newOffset: { top: number; left: number }) {
    this.trigger('move', newOffset);
    this.updateCacheBounds();
  }

  moveTo(position: Point) {
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

  appendTo($parent: JQuery) {
    this.$container.appendTo($parent);
  }

  /** @see FormModel.headerVisible */
  setHeaderVisible(headerVisible: boolean) {
    this.setProperty('headerVisible', headerVisible);
  }

  protected _renderHeaderVisible() {
    let headerVisible = this.headerVisible === null ? this.isDialog() : this.headerVisible;
    if (headerVisible && !this.$header) {
      this._renderHeader();
    } else if (!headerVisible && this.$header) {
      this._removeHeader();
    }
    // If header contains no title it won't be a real header, it will be in the top right corner just containing icons.
    let noTitleHeader = this.$header && this.$header.hasClass('no-title');
    this.$container.toggleClass('header-visible', headerVisible && !noTitleHeader);
    this.invalidateLayoutTree();
  }

  /** @see FormModel.title */
  setTitle(title: string) {
    this.setProperty('title', title);
  }

  protected _renderTitle() {
    if (this.$header) {
      this.$title.text(this.title);
      this.$header.toggleClass('no-title', !this.title && !this.subTitle);
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  /** @see FormModel.subTitle */
  setSubTitle(subTitle: string) {
    this.setProperty('subTitle', subTitle);
  }

  protected _renderSubTitle() {
    if (this.$header) {
      this.$subTitle.text(this.subTitle);
      this.$header.toggleClass('no-title', !this.title && !this.subTitle);
    }
    // Layout could have been changed, e.g. if subtitle becomes visible
    this.invalidateLayoutTree();
  }

  /** @see FormModel.iconId */
  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
  }

  protected _renderIconId() {
    if (this.$header) {
      this.$icon.icon(this.iconId);
      // Layout could have been changed, e.g. if subtitle becomes visible
      this.invalidateLayoutTree();
    }
  }

  protected _setViews(views: Form[]) {
    if (views) {
      views.forEach(view => {
        view.setDisplayParent(this);
      });
    }
    this._setProperty('views', views);
  }

  override setDisabledStyle(disabledStyle: DisabledStyle) {
    this.rootGroupBox.setDisabledStyle(disabledStyle);
  }

  /** @see FormModel.displayParent */
  setDisplayParent(displayParent: DisplayParent) {
    this.setProperty('displayParent', displayParent);
  }

  protected _setDisplayParent(displayParent: DisplayParent) {
    if (displayParent instanceof Form && displayParent.parent instanceof WrappedFormField && displayParent.isView()) {
      displayParent = Form.findNonWrappedForm(displayParent);
    }
    this._setProperty('displayParent', displayParent);

    if (displayParent) {
      this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
    }
  }

  /** @see FormModel.maximized */
  setMaximized(maximized: boolean) {
    this.setProperty('maximized', maximized);
  }

  protected _renderMaximized() {
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

  protected _onWindowResize() {
    if (this.maximized) {
      this._maximize();
    }
  }

  protected _maximize() {
    if (!this.rendered) {
      return;
    }
    let layout = this.htmlComp.layout as DialogLayout,
      shrinkEnabled = layout.shrinkEnabled;
    layout.shrinkEnabled = true;
    this.revalidateLayoutTree();
    this.position();
    layout.shrinkEnabled = shrinkEnabled;
  }

  prefBounds(): Rectangle {
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

  protected override _attach() {
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
   */
  protected override _postAttach() {
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
   */
  protected override _detach() {
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
      // If the focus widget is outside the view area the browser tries to scroll the widget into view.
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
  protected _initialFocusElement() {
    let focusElement;
    if (this.initialFocus) {
      focusElement = this.initialFocus.getFocusableElement();
    }
    if (!focusElement) {
      focusElement = this.session.focusManager.findFirstFocusableElement(this.$container);
    }
    return focusElement;
  }

  protected override _installFocusContext() {
    if (this.isDialog() || this.isPopupWindow()) {
      this.session.focusManager.installFocusContext(this.$container, FocusRule.NONE);
    }
  }

  protected override _uninstallFocusContext() {
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
   * @returns 'true' if this Form is currently accessible to the user
   */
  inFront(): boolean {
    return this.rendered && this.attached;
  }

  /**
   * Visits all form-fields of this form in pre-order (top-down).
   */
  visitFields(visitor: (FormField) => TreeVisitResult | void) {
    this.rootGroupBox.visitFields(visitor);
  }

  /**
   * Visits all dialogs, messageBoxes and fileChoosers of this form in pre-order (top-down).
   * filter is an optional parameter.
   */
  visitDisplayChildren(visitor: (child: Form | MessageBox | FileChooser) => void, filter?: (child: Form | MessageBox | FileChooser) => boolean) {
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

  storeCacheBounds(bounds: Rectangle) {
    if (this.cacheBounds) {
      let storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
      webstorage.setItemToLocalStorage(storageKey, JSON.stringify(bounds));
    }
  }

  readCacheBounds(): Rectangle {
    if (!this.cacheBounds) {
      return null;
    }

    let storageKey = 'scout:formBounds:' + this.cacheBoundsKey;
    let bounds: string | Rectangle = webstorage.getItemFromLocalStorage(storageKey);
    if (!bounds) {
      return null;
    }
    bounds = JSON.parse(bounds) as Rectangle;
    return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  /**
   * @returns the form the widget belongs to (returns the first parent which is a {@link Form}).
   */
  static findForm(widget: Widget): Form {
    let parent = widget.parent;
    while (parent && !(parent instanceof Form)) {
      parent = parent.parent;
    }
    return parent as Form;
  }

  /**
   * @returns the first form which is not an inner form of a wrapped form field
   */
  static findNonWrappedForm(widget: Widget): Form {
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
