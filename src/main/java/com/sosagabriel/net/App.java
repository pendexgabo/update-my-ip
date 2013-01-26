package com.sosagabriel.net;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.sosagabriel.net.service.Updater;

/**
 * Hello world!
 * 
 */
public class App {
	private static final String PROPERTIES_FILE_NAME = "updatemyip.cfg";

	private Properties properties = null;

	public static String HOME_DIR = null;

	protected Updater updaterService = null;
	protected boolean notifyOnIpChange = false;

	protected TrayIcon trayIcon = null;;
	protected PopupMenu popup = null;

	protected CheckboxMenuItem notifyOnIpChangeItem = null;
	protected MenuItem forceUpdateItem = null;
	protected MenuItem exitItem = null;
	
	
	Timer timer = null;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		App app = new App();

		app.setup();

		app.run();

	}

	private void run() {

		if (!SystemTray.isSupported()) {
			System.err.println("SystemTray not supported.");
			exitApplication(1);
		}

			SystemTray tray = SystemTray.getSystemTray();
			Image image = getImage("images/no_ip.png");

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exitApplication(0);
				}
			};

			ActionListener forceUpdateListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateIp(true);
				}
			};

			ItemListener notifyOnIpChangeListener = new ItemListener() {

				
				public void itemStateChanged(ItemEvent e) {
					notifyOnIpChange = notifyOnIpChangeItem.getState();
					
				}
			};

			popup = new PopupMenu();

			forceUpdateItem = new MenuItem("Update now");
			forceUpdateItem.addActionListener(forceUpdateListener);
			popup.add(forceUpdateItem);

			notifyOnIpChangeItem = new CheckboxMenuItem("Notify me on ip change");
			notifyOnIpChangeItem.addItemListener(notifyOnIpChangeListener);
			popup.add(notifyOnIpChangeItem);

			popup.addSeparator();

			exitItem = new MenuItem("Exit");
			exitItem.addActionListener(exitListener);
			popup.add(exitItem);

			trayIcon = new TrayIcon(image, "Update Ip", popup);

			trayIcon.setImageAutoSize(true);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
				exitApplication(1);
			}
	}

	protected boolean updateIp(boolean force) {
		
		if (force) {
			updaterService.setLatestIp(null);
		}
		boolean result = updaterService.updateIp();

		
		notifyUI(result);

		return result;
	}
	
	protected void updateAtFixedRate(long interval) {
		timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateIp(false);
			}
		}, 3000L, interval);
	}
	
	
	protected void exitApplication(int status) {
		System.out.println("exiting...");
		
		if (timer != null) {
			timer.cancel();
		}
		
		System.exit(status);
		
	}

	protected void notifyUI(boolean result) {
		
		trayIcon.setToolTip("Current ip: " + updaterService.getLatestIp());
		
		
		if (notifyOnIpChange && result) {

			trayIcon.displayMessage("Ip changed", "Ip address has changed. New ip: " + updaterService.getLatestIp(),
					TrayIcon.MessageType.INFO);
		}
	}

	protected Image getImage(final String pathAndFileName) {
		URL url = null;
		try {
			url = new URL("file:" + HOME_DIR + File.separator + pathAndFileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return Toolkit.getDefaultToolkit().getImage(url);
	}

	@SuppressWarnings("deprecation")
	private void setup() throws FileNotFoundException, IOException {

		HOME_DIR = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();

		properties = new Properties();
		System.out.println(HOME_DIR);

		properties.load(new FileInputStream(new File(URLDecoder.decode(HOME_DIR) + File.separator
				+ PROPERTIES_FILE_NAME)));

		updaterService = new Updater(properties.getProperty("update_endpoint"));
		
		if (properties.getProperty("checkip_endpoint") != null) {
			Updater.CHECK_IP_ENDPOINT = properties.getProperty("checkip_endpoint");
		}
		
		long updateEvery = Long.parseLong(properties.getProperty("update_every"));
		updateAtFixedRate(updateEvery);
		
	}
}
