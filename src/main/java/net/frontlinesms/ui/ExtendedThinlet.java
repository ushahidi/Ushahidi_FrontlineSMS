/**
 * 
 */
package net.frontlinesms.ui;

import java.awt.Image;

import thinlet.Thinlet;

/**
 * Extension of Thinlet which adds accessors for properties and factory methods for creating components.
 * @author Alex
 */
@SuppressWarnings("serial")
public class ExtendedThinlet extends Thinlet {
//> STATIC CONSTANTS

//> INSTANCE PROPERTIES

//> CONSTRUCTORS

//> ACCESSORS

//> INSTANCE HELPER METHODS
	/**
	 * Recursivelty sets a boolean attribute on a UI component and all its sub-components.
	 * @param parent
	 * @param value
	 */
	private final void setEnabledRecursively(Object parent, boolean value) {
		setBoolean(parent, ENABLED, value);
		for(Object component : getItems(parent)) {
			if(!getClass(parent).equals(TABLE)) setEnabledRecursively(component, value);
		}
	}

//> COMPONENT ACCESSORS
	/**
	 * Sets the thinlet name of the supplied thinlet component.
	 * @param component
	 * @param name
	 */
	public final void setName(Object component, String name) {
		setString(component, NAME, name);
	}
	
	/**
	 * Attaches an object to a component.
	 * @param component
	 * @param attachment
	 */
	public final void setAttachedObject(Object component, Object attachment) {
		putProperty(component, PROPERTY_ATTACHED_OBJECT, attachment);
	}
	
	/**
	 * Retrieves the attached object from this component.
	 * @param component
	 * @return the object attached to the component, as set by {@link #setAttachedObject(Object, Object)}
	 */
	public final Object getAttachedObject(Object component) {
		return getProperty(component, PROPERTY_ATTACHED_OBJECT);
	}
	
	/**
	 * Retrieves an attached object from a component with the specified class.
	 * @param <T>
	 * @param component
	 * @param clazz
	 * @return An object of the requested class.
	 */
	public <T extends Object> T getAttachedObject(Object component, Class<T> clazz) {
		return (T) getAttachedObject(component);
	}

	/**
	 * Disables a UI component and all descended components.
	 * @param component
	 */
	public final void deactivate(Object component) {
		setEnabledRecursively(component, false);
	}
	
	/**
	 * Enables a UI component and all descended components.
	 * @param component
	 */
	public final void activate(Object component) {
		setEnabledRecursively(component, true);
	}
	
	/**
	 * Sets the visibility of a component.
	 * @param component
	 * @param visible
	 */
	public void setVisible(Object component, boolean visible) {
		setBoolean(component, VISIBLE, visible);
	}

	/**
	 * Checks if a component is selected.
	 * @param component
	 * @return <code>true</code> if the component is selected
	 */
	public boolean isSelected(Object component) {
		return getBoolean(component, SELECTED);
	}

	/**
	 * Checks if a component is enabled.
	 * @param component
	 * @return <code>true</code> if the component is enabled
	 */
	public boolean isEnabled(Object component) {
		return getBoolean(component, ENABLED);
	}
	
	/**
	 * Sets the number of columns of a thinlet component.
	 * @param component
	 * @param columns
	 */
	public void setColumns(Object component, int columns) {
		setInteger(component, ATTRIBUTE_COLUMNS, columns);
	}

	/**
	 * Sets the colspan attribute of a thinlet component.
	 * @param component
	 * @param colspan number of columns for component to span
	 */
	public void setColspan(Object component, int colspan) {
		setInteger(component, ATTRIBUTE_COLSPAN, colspan);
	}

	/**
	 * Sets the width of a thinlet component.
	 * @param component
	 * @param width
	 */
	public void setWidth(Object component, int width) {
		setInteger(component, Thinlet.ATTRIBUTE_WIDTH, width);
	}

	/**
	 * Sets the gap attribute of a thinlet component.
	 * @param component
	 * @param gap gap to add around this component
	 */
	public void setGap(Object component, int gap) {
		setInteger(component, Thinlet.GAP, gap);
	}

	/**
	 * Sets the gap attribute of a thinlet component.
	 * @param component
	 * @param weightX horizontal weigth to set for this component
	 * @param weightY vertical weigth to set for this component
	 */
	public void setWeight(Object component, int weightX, int weightY) {
		setInteger(component, Thinlet.ATTRIBUTE_WEIGHT_X, weightX);
		setInteger(component, Thinlet.ATTRIBUTE_WEIGHT_Y, weightY);
	}
	
	/**
	 * Sets the icon of a component
	 * @param component
	 * @param icon
	 */
	public void setIcon(Object component, Image icon) {
		setIcon(component, ICON, icon);
	}
	
	/**
	 * Sets the icon of a component from a specified path
	 * @param component
	 * @param iconPath
	 */
	public void setIcon(Object component, String iconPath) {
		setIcon(component, ICON, getIcon(iconPath));
	}
	
	/**
	 * Sets whether a component has a border or not
	 * @param component the component to add/remove border from
	 * @param hasBorder <code>true</code> to add border, <code>false</code> to remove
	 */
	public void setBorder(Object component, boolean hasBorder) {
		setBoolean(component, Thinlet.BORDER, hasBorder);
	}

	/**
	 * Set the action of a component
	 * @param component
	 * @param methodCall
	 * @param root
	 * @param handler
	 */
	public void setAction(Object component, String methodCall, Object root, Object handler) {
		setMethod(component, Thinlet.ATTRIBUTE_ACTION, methodCall, root, handler);
	}
	
	/**
	 * Set the close action of a component
	 * @param component
	 * @param methodCall
	 * @param root
	 * @param handler
	 */
	public void setCloseAction(Object component, String methodCall, Object root, Object handler) {
		setMethod(component, Thinlet.CLOSE, methodCall, root, handler);
	}
	
//> COMPONENT FACTORY METHODS
	/**
	 * Creates a Thinlet UI Component of type NODE, sets the component's TEXT
	 * attribute to the supplied text and attaches the supplied OBJECT. 
	 * @param text
	 * @param attachedObject
	 * @return a tree node with an object attached to it
	 */
	public final Object createNode(String text, Object attachedObject) {
		Object node = Thinlet.create(NODE);
		setString(node, TEXT, text);
		setAttachedObject(node, attachedObject);
		return node;
	}

	/**
	 * Create a Thinlet UI Component of type table row, and attaches the
	 * supplied object to it.
	 * @param attachedObject
	 * @return a table row with an object attached
	 */
	public final Object createTableRow(Object attachedObject) {
    	Object row = Thinlet.create(ROW);
    	setAttachedObject(row, attachedObject);
    	return row;
    }
	
	/**
	 * Create a Thinlet UI component of type table cell containing the supplied number.
	 * @param integerContent
	 * @return a table cell
	 */
	public final Object createTableCell(int integerContent) {
    	return createTableCell(Integer.toString(integerContent));
    }
        
	/**
	 * Creates a UI table cell component containing the specified text. 
	 * @param text
	 * @return a table cell
	 */
	public final Object createTableCell(String text) {
		Object cell = Thinlet.create(CELL);
		setString(cell, TEXT, text);
		return cell;
	}
        
	/**
	 * Creates a Thinlet UI component of type table cell in the table row provided.
	 * The cell text is also set. 
	 * @param row 
	 * @param text
	 * @return The cell I created.
	 */
	protected final Object createTableCell(Object row, String text) {
		Object cell = Thinlet.create(CELL);
		setString(cell, TEXT, text);
		add(row, cell);
		return cell;
	}
	
	/**
	 * Creates a Thinlet UI Component of type LIST ITEM, set's the component's
	 * TEXT attribute to the supplied text and attaches the supplied OBJECT.
	 * @param text
	 * @param attachedObject
	 * @return a list item with the supplied attachment 
	 */
	public final Object createListItem(String text, Object attachedObject) {
		Object item = Thinlet.create(ITEM);
		setString(item, TEXT, text);
		setAttachedObject(item, attachedObject);
		return item;
	}
	
	/**
	 * Creates a Thinlet UI Component of type COMBOBOX CHOICE, set's the component's
	 * TEXT attribute to the supplied text and attaches the supplied OBJECT.
	 * @param text
	 * @param attachedObject
	 * @return
	 */
	protected final Object createComboboxChoice(String text, Object attachedObject) {
		Object item = Thinlet.create(CHOICE);
		setString(item, TEXT, text);
		setAttachedObject(item, attachedObject);
		return item;
	}
	
	/**
	 * Creates a Thinlet UI Component of type COMBOBOX CHOICE, set's the component's
	 * TEXT attribute to the supplied text and attaches the supplied OBJECT.
	 * @param text
	 * @param attachedObject
	 * @return
	 */
	public final Object createColumn(String text, Object attachedObject) {
		Object item = Thinlet.create(COLUMN);
		setString(item, TEXT, text);
		setAttachedObject(item, attachedObject);
		return item;
	}
	
	/**
	 * Create a modal, closeable and resizeable dialog!
	 * @param title
	 * @return
	 */
	protected final Object createDialog(String title) {
		Object dialog = Thinlet.create(DIALOG);
		setString(dialog, TEXT, title);
		setBoolean(dialog, MODAL, true);
		setBoolean(dialog, CLOSABLE, true);
		setBoolean(dialog, "resizable", true);
		return dialog;
	}
	
	/**
	 * Create's a Thinlet UI Component of type BUTTON and set's the button's
	 * action and text label.
	 * @param text
	 * @param action
	 * @param root
	 * @return
	 */
	public final Object createButton(String text, String action, Object root) {
		Object button = Thinlet.create(BUTTON);
		setString(button, TEXT, text);
		setMethod(button, ATTRIBUTE_ACTION, action, root, this);
		return button;
	}
	
	/**
	 * Create's a Thinlet UI Component of type BUTTON and set's the button's
	 * action and text label.
	 * TODO how often is this used?
	 * @param text
	 * @return
	 */
	public final Object createButton(String text) {
		Object button = Thinlet.create(BUTTON);
		setString(button, TEXT, text);
		return button;
	}
	
	/**
	 * Create's a Thinlet UI Component of type LABEL with the supplied TEXT.
	 * @param text The text displayed for this label.
	 * @return
	 */
	public final Object createLabel(String text) {
		Object label = create(LABEL);
		setString(label, TEXT, text);
		return label;
	}
	
	/**
	 * Creates a thinlet Checkbox UI component.
	 * @param text
	 * @param checked
	 * @return
	 */
	public final Object createCheckbox(String name, String text, boolean checked) {
		Object item = create(WIDGET_CHECKBOX);
		setText(item, text);
		setName(item, name);
		setBoolean(item, SELECTED, checked);
		return item;
	}
	
	/**
	 * Creates a thinlet Radio Button UI component.
	 * @param text
	 * @param checked
	 * @return
	 */
	public final Object createRadioButton(String name, String text, String group, boolean selected) {
		Object item = create(WIDGET_CHECKBOX);
		setText(item, text);
		setString(item, GROUP, group);
		setName(item, name);
		setBoolean(item, SELECTED, selected);
		return item;
	}
	
	/**
	 * Creates a thinlet Panel UI component.
	 * @param text
	 * @param checked
	 * @return
	 */
	public final Object createPanel(String name) {
		Object item = Thinlet.create(PANEL);
		setName(item, name);
		return item;
	}
	
	/**
	 * Creates a textfield with the supplied object name and initial text.
	 * @param name
	 * @param initialText
	 * @return
	 */
	public final Object createTextfield(String name, String initialText) {
		Object item = Thinlet.create(TEXTFIELD);
		setText(item, initialText);
		setName(item, name);
		return item;
	}
	
	/**
	 * Creates a passwordfield with the supplied object name and initial text.
	 * @param name
	 * @param initialText
	 * @return
	 */
	public final Object createPasswordfield(String name, String initialText) {
		Object item = Thinlet.create(PASSWORDFIELD);
		setText(item, initialText);
		setName(item, name);
		return item;
	}

//> STATIC FACTORIES

//> STATIC HELPER METHODS
}
