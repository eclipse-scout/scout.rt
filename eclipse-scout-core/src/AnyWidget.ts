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
    ViewMenuTab,
    WidgetField,
    WidgetPopup,
    WidgetTile,
    WidgetTooltip,
    WizardProgressField,
    WrappedFormField,
    YearPanel
} from "./index";

export type AnyWidget =
    Form
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
    | Mode
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
    | ModeSelector
    | Notification
    | BreadcrumbBar
    | CollapseHandle
    | DesktopNavigationHandle
    | any

export default AnyWidget;