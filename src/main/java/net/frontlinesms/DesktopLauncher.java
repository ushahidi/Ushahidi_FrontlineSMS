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
package net.frontlinesms;

import java.io.File;
import java.io.InputStream;

import javax.swing.UIManager;

import net.frontlinesms.resources.ResourceUtils;
import net.frontlinesms.ui.FirstTimeWizard;
import net.frontlinesms.ui.ThinletDatabaseConnectionTestHandler;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.i18n.InternationalisationUtils;
import net.frontlinesms.ui.i18n.LanguageBundle;

import org.apache.log4j.Logger;

import thinlet.Thinlet;

/**
 * This class is the Launcher for FrontlineSMS as a desktop application.  It will
 * start the Frontline service and then open the graphical user interface to
 * control the service.  If any unhandled exceptions occur either starting the
 * FrontlineSMS service or opening the GUI, they will be diaplyed in an AWT window
 * so that they can easily be reported back to the development team.
 * 
 * N.B. Error messages CANNOT be i18ned in this class, as there may have been an error
 * loading language packs.
 * 
 * @author Alex Anderson alex(at)masabi(dot)com
 * @author Carlos Eduardo Genz kadu(at)masabi(dot)com
 */
public class DesktopLauncher {
	/** Logging object */
	private static final Logger LOG = Utils.getLogger(DesktopLauncher.class);
	
	/**
	 * Main class for launching the FrontlineSMS project.
	 * @param args
	 */
	public static void main(String[] args) {
		FrontlineSMS frontline = null;
		try {
			AppProperties appProperties = AppProperties.getInstance();
			final String VERSION = BuildProperties.getInstance().getVersion();
			LOG.info("FrontlineSMS version [" + VERSION + "]");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			String lastVersion = appProperties.getLastRunVersion();
			InputStream defaultResourceArchive = ResourceUtils.class.getResourceAsStream("/resources.zip");
			if(defaultResourceArchive == null) {
				LOG.fatal("Default resources archive could not be found!");
				throw new Exception("Default resources archive could not be found!");
			}

			ResourceUtils.unzip(defaultResourceArchive, new File(ResourceUtils.getConfigDirectoryPath()), !VERSION.equals(lastVersion));

			// This should always get the English bundle, as other languages are only included in
			// resources.zip rather than in the resources/languages directory
			LanguageBundle englishBundle = InternationalisationUtils.getLanguageBundleFromClasspath("/resources/languages/frontlineSMS.properties");
			Thinlet.DEFAULT_ENGLISH_BUNDLE = englishBundle.getProperties();

			boolean showWizard = appProperties.isShowWizard();
			appProperties.setLastRunVersion(VERSION);
			appProperties.saveToDisk();

			frontline = new FrontlineSMS(new ThinletDatabaseConnectionTestHandler());
			if (showWizard) {
				new FirstTimeWizard(frontline);
			} else {
				// Auto-detect phones.
				new UiGeneratorController(frontline, true);
			}
		} catch(Throwable t) {
			if (frontline != null) 
				frontline.destroy();
			
			// Rather than swallowing the error, we now display it to the user
			// so that they can give us some feedback :)
			ErrorUtils.showErrorDialog("Fatal error starting FrontlineSMS!", "A problem ocurred during FrontlineSMS startup.", t, true);
		} 
	}
}
