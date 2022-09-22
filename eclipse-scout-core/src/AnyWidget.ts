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
import {
  Accordion,
  AccordionField,
  Action,
  BeanTile,
  BenchColumn,
  BreadcrumbBar,
  BreadcrumbBarField,
  BreadcrumbItem,
  BusyIndicator,
  Button,
  Calendar,
  CalendarComponent,
  CalendarField,
  Carousel,
  CarouselField,
  CellEditorPopup,
  CollapseHandle,
  CompactTree,
  Composite,
  CompositeField,
  CompositeTile,
  ContextMenuPopup,
  DatePicker,
  DatePickerPopup,
  Desktop,
  DesktopBench,
  DesktopHeader,
  DesktopLogo,
  DesktopNavigation,
  DesktopNavigationHandle,
  DesktopTab,
  DesktopTabArea,
  DesktopToolBox,
  FieldStatus,
  FileChooser,
  FileInput,
  Form,
  FormField,
  GlassPane,
  Group,
  HtmlTile,
  Icon,
  IFrame,
  Image,
  ImageField,
  Label,
  Menu,
  MenuBar,
  MenuBarBox,
  MenuBox,
  MessageBox,
  Mode,
  ModeSelector,
  Notification,
  NullWidget,
  Outline,
  OutlineOverview,
  OutlineTileField,
  PageTileGrid,
  PlaceholderField,
  PlaceholderTile,
  Planner,
  PlannerField,
  PlannerHeader,
  Popup,
  PopupManager,
  ProposalChooser,
  Scrollbar,
  SimpleTab,
  SimpleTabArea,
  SimpleTabBox,
  Slider,
  SmartFieldPopup,
  Splitter,
  StatusMenuMapping,
  Switch,
  Tab,
  TabArea,
  TabBoxHeader,
  Table,
  TableControl,
  TableField,
  TableFooter,
  TableHeader,
  TableHeaderMenu,
  TableHeaderMenuButton,
  TableHeaderMenuGroup,
  TableInfoFilterTooltip,
  TableInfoLoadTooltip,
  TableInfoSelectionTooltip,
  TableProposalChooser,
  TableRowDetail,
  TableRowTileMapping,
  TableTileGridMediator,
  TableTooltip,
  TagBar,
  TagChooserPopup,
  Tile,
  TileAccordion,
  TileField,
  TileGrid,
  TileOutlineOverview,
  TileOverviewForm,
  TimePicker,
  TimePickerPopup,
  Tooltip,
  TouchPopup,
  Tree,
  TreeField,
  TreeProposalChooser,
  UnsavedFormChangesForm,
  ValueField,
  ViewButton,
  ViewButtonBox,
  ViewMenuTab, Widget,
  WidgetField,
  WidgetPopup,
  WidgetTile,
  WidgetTooltip,
  WizardProgressField,
  WrappedFormField,
  YearPanel
} from './index';

export type AnyWidget =
  Widget & (Form
  | TileOverviewForm
  | UnsavedFormChangesForm
  | FieldStatus
  | StatusMenuMapping
  | FormField
  | CompositeField
  | ValueField
  | WidgetField
  | Button
  | WizardProgressField
  | CarouselField
  | TileField
  | TreeField
  | ImageField
  | TableField
  | PlaceholderField
  | WrappedFormField
  | PlannerField
  | CalendarField
  | AccordionField
  | BreadcrumbBarField
  | OutlineTileField
  | TabArea
  | TabBoxHeader
  | Tab
  | ProposalChooser
  | TableProposalChooser
  | TreeProposalChooser
  | MenuBarBox
  | MenuBar
  | MenuBox
  | DesktopToolBox
  | Tile
  | BeanTile
  | CompositeTile
  | WidgetTile
  | PlaceholderTile
  | HtmlTile
  | TileGrid
  | PageTileGrid
  | Tree
  | CompactTree
  | Outline
  | Group
  | Image
  | Icon
  | Label
  | Popup
  | TagChooserPopup
  | SmartFieldPopup
  | ContextMenuPopup
  | TouchPopup
  | WidgetPopup
  | TableHeaderMenu
  | CellEditorPopup
  | DatePickerPopup
  | TimePickerPopup
  | PopupManager
  | TableFooter
  | TableHeader
  | TableRowTileMapping
  | TableRowDetail
  | Table
  | TableTileGridMediator
  | TableHeaderMenuGroup
  | Action
  | Menu
  | TableHeaderMenuButton
  | TableControl
  | ViewButton
  | Mode<any>
  | BreadcrumbItem
  | IFrame
  | Slider
  | Switch
  | SimpleTab
  | DesktopTab
  | SimpleTabArea
  | DesktopTabArea
  | SimpleTabBox
  | TagBar
  | Composite
  | NullWidget
  | Desktop
  | DesktopLogo
  | BenchColumn
  | DesktopBench
  | DesktopHeader
  | OutlineOverview
  | TileOutlineOverview
  | DesktopNavigation
  | ViewButtonBox
  | ViewMenuTab
  | Planner
  | PlannerHeader
  | BusyIndicator
  | Tooltip
  | TableInfoSelectionTooltip
  | TableTooltip
  | TableInfoLoadTooltip
  | TableInfoFilterTooltip
  | WidgetTooltip
  | CalendarComponent
  | Calendar
  | YearPanel
  | Carousel
  | Splitter
  | Accordion
  | TileAccordion
  | GlassPane
  | Scrollbar
  | DatePicker
  | MessageBox
  | TimePicker
  | FileInput
  | FileChooser
  | ModeSelector<any>
  | Notification
  | BreadcrumbBar
  | CollapseHandle
  | DesktopNavigationHandle
  | unknown
  );

export default AnyWidget;
