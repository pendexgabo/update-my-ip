package com.sosagabriel.net.service;

import java.io.IOException;

import us.monoid.web.Resty;

public class Updater {

	private String latestIp = null;

	private String updateUrl = null;

	public static String CHECK_IP_ENDPOINT = "https://indiana.nodolujan.com.ar/ip.php";

	public Updater(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public void setLatestIp(String latestIp) {
		this.latestIp = latestIp;
	}

	public String getLatestIp(boolean force) {
		if (latestIp == null || force == true) {
			latestIp = retrieveIp();
		}
		return latestIp;
	}

	public String getLatestIp() {
		return getLatestIp(false);
	}

	public String retrieveIp() {

		Resty resty = new Resty();

		String raw = "";
		try {
			raw = resty.text(CHECK_IP_ENDPOINT).toString();
		} catch (IOException e) {
			// nothing to do
		}
		return raw.trim();
	}

	public boolean updateIp() {

		boolean result = false;

		String liveIp = retrieveIp();

		if (latestIp == null || latestIp.equals(liveIp) == false) {
			setLatestIp(liveIp);
			System.out.println("ip changed updating...");
			
			Resty resty = new Resty();

			try {
				resty.text(updateUrl).toString();
				result = true;
			} catch (IOException e) {
				// nothing to do
				System.out.println("exception while updating ip to service");
			}

		}

		return result;

	}

}
