/**
 * 
 */
package net.frontlinesms.ui;

import net.frontlinesms.resources.PropertySet;

/**
 * Wrapper class for UI properties file.
 * @author Alex
 */
final class UiProperties extends PropertySet {
//> STATIC CONSTANTS
	/** The name of the {@link PropertySet} which these properties are loaded from and saved in. */
	private static final String PROPERTYSET_NAME = "ui";
	
//> PROPERTY KEYS & VALUES
	/** Property key (String): set view mode to "New View" or "Classic View" */
	private static final String KEY_VIEW_MODE = "view.mode";
	/** Property value for {@link #KEY_VIEW_MODE}: "Classic View" */
	private static final String VIEW_MODE_CLASSIC = "classic";
	/** Property value for {@link #KEY_VIEW_MODE}: "New View" */
	private static final String VIEW_MODE_NEW = "new";

	/** Property key (int): Window Width */
	private static final String KEY_WINDOW_WIDTH = "window.width";
	/** Property key (int): Window Height */
	private static final String KEY_WINDOW_HEIGHT = "window.height";

	/** Property key (String): Window State */
	private static final String KEY_WINDOW_STATE = "window.state";
	/** Property value for {@link #KEY_WINDOW_STATE}: maximised */
	private static final String WINDOW_STATE_MAXIMISED = "maximised";
	/** Property value for {@link #KEY_WINDOW_STATE}: not maximised */
	private static final String WINDOW_STATE_NORMAL = "normal";

	/** Property Key (boolean) indicating if the logo is visible */
	private static final String KEY_HOMETABLOGO_VISIBLE = "hometab.logo.visible";
	/** Property Key (String) indicating the path to image file containing the logo. */
	private static final String KEY_HOMETABLOGO_SOURCE = "hometab.logo.source";

	/** Property key (double) the price per SMS */
	private static final String KEY_SMS_COST = "sms.cost";
	
	/** Singleton instance of this class. */
	private static UiProperties instance;

//> INSTANCE PROPERTIES

//> CONSTRUCTORS
	/** Create a new instance of this class. */
	private UiProperties() {
		super(PROPERTYSET_NAME);
	}

//> STATIC ACCESSORS
	/** @return value for {@link #KEY_VIEW_MODE} */
	private String getViewMode() {
		return super.getProperty(KEY_VIEW_MODE);
	}
	/**
	 * @param viewMode new value for the property {@link #KEY_VIEW_MODE}
	 */
	public void setViewMode(String viewMode) {
		super.setProperty(KEY_VIEW_MODE, viewMode);
	}
	
	/**
	 * Sets the view mode to classic or new view.
	 * @param classic <code>true</code> if classic view should be used; <code>false</code> for new view.
	 */
	public void setViewModeClassic(boolean classic) {
		setViewMode(classic ? VIEW_MODE_CLASSIC : VIEW_MODE_NEW);
	}
	/** @return <code>true</code> if classic view should be used, <code>false</code> if new view should be used. */
	public boolean isViewModeClassic() {
		String viewMode = getViewMode();
		return viewMode != null && viewMode.equalsIgnoreCase(VIEW_MODE_CLASSIC);
	}

	/**
	 * Sets the property to make a tab visible or invisible.
	 * @param tabName The name of the tab.
	 * @param visible <code>true</code> if the tab should be visible, <code>false</code> otherwise.
	 */
	public void setTabVisible(String tabName, boolean visible) {
		super.setProperty(tabName + ".visible", Boolean.toString(visible));
	}
	/** 
	 * @param tabName The name of the tab.
	 * @return <code>true</code> if the tab should be visible, <code>false</code> otherwise.
	 */
	public boolean isTabVisible(String tabName) {
		Boolean visible = super.getPropertyAsBoolean(tabName + ".visible");
		return visible == null || visible.booleanValue();
	}
	
	/** @return value for {@link #KEY_WINDOW_STATE} */
	public boolean isWindowStateMaximized() {
		String windowState = super.getProperty(KEY_WINDOW_STATE);
		return windowState != null
				&& windowState.equals(WINDOW_STATE_MAXIMISED);
	}	
	/** @return the saved width of the window, or <code>null</code> if none was set. */
	public Integer getWindowWidth() {
		String widthAsString = super.getProperty(UiProperties.KEY_WINDOW_WIDTH);
		Integer width = null;
		if(widthAsString != null) {
			try {
				width = Integer.parseInt(widthAsString);
			} catch (NumberFormatException ex) { /* Do nothing - we will return null */ }
		}
		return width;
	}
	/** @return the saved height of the window, or <code>null</code> if none was set. */
	public Integer getWindowHeight() {
		String heightAsString = super.getProperty(UiProperties.KEY_WINDOW_HEIGHT);
		Integer height = null;
		if(heightAsString != null) {
			try {
				height = Integer.parseInt(heightAsString);
			} catch (NumberFormatException ex) { /* Do nothing - we will return null */ }
		}
		return height;
	}
	/**
	 * Set the window state and dimensions.
	 * @param maximized
	 * @param width
	 * @param height
	 */
	public void setWindowState(boolean maximized, int width, int height) {
		super.setProperty(KEY_WINDOW_STATE,
				maximized ? WINDOW_STATE_MAXIMISED : WINDOW_STATE_NORMAL);
		super.setProperty(UiProperties.KEY_WINDOW_WIDTH, String.valueOf(width));
		super.setProperty(UiProperties.KEY_WINDOW_HEIGHT, String.valueOf(height));
	}
	
	/** @return <code>true</code> if the logo should be shown on the home tab; <code>false</code> otherwise */
	public boolean isHometabLogoVisible() {
		Boolean visible = super.getPropertyAsBoolean(KEY_HOMETABLOGO_VISIBLE);
		return visible == null || visible.booleanValue();
	}
	/**
	 * Set visibility of the logo on the home tab.
	 * @param visible value for property {@link #KEY_HOMETABLOGO_VISIBLE}
	 */
	public void setHometabLogoVisible(boolean visible) {
		super.setProperty(KEY_HOMETABLOGO_VISIBLE, String.valueOf(visible));
	}
	
	/** @return the path to the file containing the logo to display on the home tab */
	public String getHomtabLogoPath() {
		return super.getProperty(KEY_HOMETABLOGO_SOURCE);
	}
	/**
	 * Set path of the logo on the home tab.
	 * @param path value for property {@link #KEY_HOMETABLOGO_SOURCE}
	 */
	public void setHometabLogoPath(String path) {
		super.setProperty(KEY_HOMETABLOGO_SOURCE, path);
	}
	/** @return number representing the cost of one SMS for displaying in the UI */
	public double getCostPerSms() {
		// TODO ideally this would be an int in the least significant denomination of the currency, e.g. pennies or cents
		String val = super.getProperty(KEY_SMS_COST);
		double cost = 0.1; // the default cost
		if(val != null) {
			try { cost = Double.parseDouble(val); } catch(NumberFormatException ex) { /* just use the default */ }
		}
		return cost;
	}
	/** @param costPerSms the price of one sms */
	public void setCostPerSms(double costPerSms) {
		super.setProperty(KEY_SMS_COST, Double.toString(costPerSms));
	}
	
//> INSTANCE HELPER METHODS

//> STATIC FACTORIES
	/**
	 * Lazy getter for {@link #instance}
	 * @return The singleton instance of this class
	 */
	public static synchronized UiProperties getInstance() {
		if(instance == null) {
			instance = new UiProperties();
		}
		return instance;
	}

//> STATIC HELPER METHODS
}
