package thinlet;

public interface ThinletText {
	/** The name of the OBJECT property of a component.  This is used for attaching Java Objects to a component that are then used by the UI to keep track of what object a UI component represents. */
	public static final String PROPERTY_ATTACHED_OBJECT = "obj";
	
	public static final String TOOLTIP = "tooltip";
	public static final String BOOLEAN = "boolean";
	public static final String BEAN = "bean";
	public static final String COMPONENT = "component";
	public static final String CHOICE = "choice";
	public static final String MENU = "menu";
	public static final String INTEGER = "integer";
	public static final String STRING = "string";
	public static final String SELECTED = "selected";
	public static final String METHOD = "method";
	public static final String PAINT = "paint";
	public static final String NODE = "node";
	public static final String MENUBAR ="menubar";
	public static final String TOP = "top"; 
	public static final String BOTTOM = "bottom";
	public static final String MNEMONIC = "mnemonic";
	public static final String MENUITEM = "menuitem";
	public static final String KEYSTROKE = "keystroke";
	public static final String ACCELERATOR = "accelerator";
	public static final String GROUP = "group";
	public static final String MENUSHOWN = "menushown";
	public static final String ICON = "icon";
	public static final String COLOR = "color";
	public static final String FONT = "font";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String CENTER = "center";
	public static final String TEXTAREA = "textarea";
	public static final String TEXTFIELD = "textfield";
	public static final String PANEL = "panel";
	public static final String DESKTOP = "desktop";
	public static final String TEXT = "text";
	public static final String SLIDER = "slider";
	public static final String BORDER = "border";
	public static final String PLACEMENT = "placement";
	public static final String GAP = "gap";
	public static final String SCROLLABLE = "scrollable";
	public static final String LINE = "line";
	public static final String ANGLE = "angle";
	public static final String EXPANDED = "expanded";
	public static final String VALUE = "value";
	public static final String DIVIDER = "divider";
	public static final String MAXIMUM = "maximum";
	public static final String UNIT = "unit";
	public static final String PROGRESSBAR = "progressbar";
	public static final String MINIMUM = "minimum";
	public static final String ORIENTATION = "orientation";
	public static final String COLLAPSE = "collapse";
	public static final String EXPAND = "expand";
	public static final String VALIDATE = "validate";
	public static final String PERFORM = "perform";
	public static final String SELECTION = "selection";
	public static final String MODAL = "modal";
	public static final String CLOSE = "close";
	public static final String STEP = "step";
	public static final String RESIZABLE = "resizable";
	public static final String MAXIMIZABLE = "maximizable";
	public static final String ICONIFIABLE = "iconifiable";
	public static final String LAYOUT = "layout";
	public static final String BLOCK = "block";
	public static final String SORT = "sort";
	public static final String CELL = "cell";
	public static final String SEPERATOR = "seperator";
	public static final String NONE = "none";
	public static final String ASCENT = "ascent";
	public static final String DESCENT = "descent";
	public static final String PASSWORDFIELD = "passwordfield";
	public static final String INSERT = "insert";
	public static final String REMOVE = "remove";
	public static final String CARET = "caret";
	public static final String BUTTON = "button";
	public static final String NAME = "name";
	public static final String ROWS = "rows";
	public static final String ENABLED = "enabled";
	public static final String BOUNDS = "bounds";
	public static final String VISIBLE = "visible";
	public static final String FOREGROUND = "foreground";
	public static final String HALIGN = "halign";
	public static final String ALIGNMENT = "alignment";
	public static final String WRAP = "wrap";
	public static final String CLOSABLE = "closable";
	
	public static final String LABEL = "label";
	public static final String SPINBOX = "spinbox";
	public static final String TABBEDPANE = "tabbedpane";
	public static final String TREE = "tree";
	public static final String POPUPMENU = "popupmenu";
	public static final String WIDGET_TAB = "tab";
	public static final String WIDGET_LIST = "list";
	public static final String WIDGET_CHECKBOX = "checkbox";
	public static final String DIALOG = "dialog";
	public static final String ITEM = "item";
	public static final String HEADER = "header";
	public static final String TABLE = "table";
	public static final String COLUMN = "column";
	public static final String ROW = "row";
	public static final String SPLITPANE = "splitpane";
	public static final String COMBOBOX = "combobox";
	public static final String CHECKBOXMENUITEM = "checkboxmenuitem";

	public static final String ATTRIBUTE_ACTION = "action";
	public static final String ATTRIBUTE_DELETE = "delete";
	public static final String ATTRIBUTE_COLUMNS = "columns";
	public static final String ATTRIBUTE_COLSPAN = "colspan";
	public static final String ATTRIBUTE_ROWSPAN = "rowspan";
	/** Component attribute key: vertical weighting */
	public static final String ATTRIBUTE_WEIGHT_Y = "weighty";
	/** Component attribute key: horizontal weighting */
	public static final String ATTRIBUTE_WEIGHT_X = "weightx";
	/** Component attribute key: width */
	public static final String ATTRIBUTE_WIDTH = "width";

	static final String ATTRIBUTE_I18N = "i18n";
	/** Prefix applied to a text label to denote an internationalised string */
	public static final String TEXT_I18N_PREFIX = "i18n.";
}
