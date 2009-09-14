/*
 * FrontlineSMS <http://www.frontlinesms.com>
 * Copyright 2007, 2008 kiwanja
 * 
 * This file is part of FrontlineSMS.
 * 
 * FrontlineSMS is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * FrontlineSMS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
 */
package net.frontlinesms.ui;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.Utils;
import thinlet.Thinlet;

/**
 * @author kadu
 *
 */
public class DateSelecter {
	private static final int LAST_MONTH = 11;
	private static final int FIRST_MONTH = 0;
	private static final String COMPONENT_LB_MONTH = "lbMonth";
	private static final String COMPONENT_BT_NEXT = "btNext";
	private static final String COMPONENT_BT_PREVIOUS = "btPrevious";
	private static final String UI_FILE_DATE_SELECTER_FORM = "/ui/dialog/dateSelecter.xml";
	private static final long serialVersionUID = 689178809019704708L;
	
	private static Logger LOG = Utils.getLogger(DateSelecter.class);
	
	private Calendar current;
	private int curMonth;
	private int curYear;
	private DateFormat format = FrontlineSMSConstants.Dependants.DEFAULT_KEYWORD_ACTION_DATE_FORMAT;
	private UiGeneratorController ui;
	private Object textField;
	private int dayToHighlight;
	private int monthToHighlight;
	private int yearToHighlight;
	
	public DateSelecter(UiGeneratorController ui, Object textField) {
		this.ui = ui;
		this.textField = textField;
	}
	
	/**
	 * Shows the date selecter dialog, showing the previous date or today.
	 * 
	 * @throws IOException
	 */
	public void showSelecter() throws IOException {
		Object dialog = ui.parse(UI_FILE_DATE_SELECTER_FORM);
		init(dialog);
		showMonth(dialog);
		ui.add(dialog);
	}

	/**
	 * @param dialog
	 */
	public void closeDialog(Object dialog) {
		ui.remove(dialog);
	}
	
	/**
	 * Initialises the dialog buttons, set their action method and gets the dte to be shown.
	 * 
	 * @param dialog
	 */
	private void init(Object dialog) {
		LOG.trace("ENTER");
		Object prev = ui.find(dialog, COMPONENT_BT_PREVIOUS);
		ui.setMethod(dialog, Thinlet.CLOSE, "closeDialog(this)", dialog, this);
		ui.setMethod(prev, Thinlet.ATTRIBUTE_ACTION, "previousMonth(dateSelecter)", dialog, this);
		Object next = ui.find(dialog, COMPONENT_BT_NEXT);
		ui.setMethod(next, Thinlet.ATTRIBUTE_ACTION, "nextMonth(dateSelecter)", dialog, this);
		current = Calendar.getInstance();
		setDayToHighlight();
		current.set(Calendar.DATE, 1);
		if (!ui.getText(textField).equals("")) {
			LOG.debug("Previous date is [" + ui.getText(textField) + "]");
			try {
				Date d = format.parse(ui.getText(textField));
				current.setTime(d);
				setDayToHighlight();
			} catch (ParseException e) {}
		}
		LOG.debug("Current date is [" + current.getTime() + "]");
		curMonth = current.get(Calendar.MONTH);
		curYear = current.get(Calendar.YEAR);
		LOG.trace("EXIT");
	}

	/**
	 * Sets the day to be highlighted
	 */
	private void setDayToHighlight() {
		dayToHighlight = current.get(Calendar.DATE);
		monthToHighlight = current.get(Calendar.MONTH);
		yearToHighlight = current.get(Calendar.YEAR);
	}

	/**
	 * Method called when the user clicks the next month button on the dialog.
	 * 
	 * @param dialog
	 */
	public void nextMonth(Object dialog) {
		current.set(Calendar.MONTH, this.curMonth + 1);
		curMonth = current.get(Calendar.MONTH);
		if (curMonth == FIRST_MONTH) {
			current.set(Calendar.YEAR, this.curYear + 1);
		}
		curYear = current.get(Calendar.YEAR);
		showMonth(dialog);
	}
	
	/**
	 * Method called when the user clicks the previous month button on the dialog.
	 * 
	 * @param dialog
	 */
	public void previousMonth(Object dialog) {
		current.set(Calendar.MONTH, this.curMonth - 1);
		curMonth = current.get(Calendar.MONTH);
		if (curMonth == LAST_MONTH) {
			current.set(Calendar.YEAR, this.curYear - 1);
		}
		curYear = current.get(Calendar.YEAR);
		showMonth(dialog);
	}
	
	/**
	 * Fill all the dates in the dialog for the current month.
	 * 
	 * @param dialog
	 */
	private void showMonth(Object dialog) {
		LOG.trace("ENTER");
		current.set(Calendar.DATE, 1);
		String curMonth = getMonthAsString(this.curMonth) + " " + curYear;
		Object lbMonth = ui.find(dialog, COMPONENT_LB_MONTH);
		LOG.debug("Current month [" + curMonth + "]");
		ui.setText(lbMonth, curMonth);
		for (int i = 1; i <= 6; i++)
			fillRow(dialog, "pn" + i);
		LOG.trace("EXIT");
	}

	/**
	 * Sets the button texts for the supplied week.
	 * 
	 * @param dialog
	 * @param pnName
	 */
	private void fillRow(Object dialog, String pnName) {
		Object panel = ui.find(dialog, pnName);
		Object buttons[] = ui.getItems(panel);
		cleanButtons(buttons);
		
		int dayOfWeek;
		while (current.get(Calendar.MONTH) == this.curMonth) {
			dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
			Object button = buttons[dayOfWeek - 1];
			ui.setEnabled(button, true);
			ui.setText(button, String.valueOf(current.get(Calendar.DATE)));
			ui.setMethod(button, Thinlet.ATTRIBUTE_ACTION, "selectionMade(dateSelecter, this.text)", dialog, this);
			if (isDayToHighlight()) {
				ui.setColor(button, Thinlet.FOREGROUND, Color.RED);
			}
			current.set(Calendar.DATE, current.get(Calendar.DATE) + 1);
			if (dayOfWeek == Calendar.SATURDAY) {
				//we've reached the end of the week, so we break;
				break;
			}
		}
		if (current.get(Calendar.YEAR) != curYear) {
			current.set(Calendar.YEAR, curYear);
		}
	}

	private boolean isDayToHighlight() {
		return (current.get(Calendar.DATE) == dayToHighlight 
				&& current.get(Calendar.MONTH) == monthToHighlight
				&& current.get(Calendar.YEAR) == yearToHighlight);
	}

	public void selectionMade(Object dialog, String day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, Integer.parseInt(day));
		c.set(Calendar.MONTH, this.curMonth);
		c.set(Calendar.YEAR, this.curYear);
		String date = format.format(c.getTime());
		ui.setText(textField, date);
		ui.remove(dialog);
		if (ui.getMethod(textField) != null) ui.executeAction(ui.getMethod(textField));
	}
	
	private void cleanButtons(Object[] buttons) {
		for (Object but : buttons) {
			ui.setText(but, "");
			ui.setEnabled(but, false);
			ui.setColor(but, Thinlet.FOREGROUND, Color.BLUE);
		}
	}

	private String getMonthAsString(int i) {
		return FrontlineSMSConstants.Dependants.MONTHS[i];
	}
}
