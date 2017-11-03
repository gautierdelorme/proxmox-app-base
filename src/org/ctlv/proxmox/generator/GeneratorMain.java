package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.lib.ProxmoxLib;
import org.json.JSONException;

public class GeneratorMain {
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		Random rndServer = new Random(new Date().getTime());
		int lambda = 5; // used for the periodic event every 5s
		int uuid = 0; // used to give a unique (and incremental) identifier to each CT

		while (true) {
//			Select the server randomly based on the creation ratio rule
			String serverName = rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1 ? Constants.SERVER1 : Constants.SERVER2;
			System.out.println("Server selected: " + serverName);
			if (notOverloaded(serverName)) { // if the server it not overloaded we're going to create and start a new CT
				String ctName = Long.toString(Constants.CT_BASE_ID + uuid);
				ProxmoxLib.createCT(serverName, ctName);
				System.out.println("Waiting before starting...");
				Thread.sleep(1000 * Constants.GENERATION_WAIT_TIME); // wait 30s for the CT to be built before starting it
				ProxmoxLib.startCT(serverName, ctName);
				System.out.println("CT" + ctName + " started on " + serverName);
				uuid += 1;
				Thread.sleep(1000 * lambda);
			} else {
				System.out.println("Server is loaded, waiting...");
				Thread.sleep(1000 * Constants.GENERATION_WAIT_TIME);
			}
		}
	}
	
//	return true as long as we don't allocate more than 16 percent of the node's RAM
	private static boolean notOverloaded(String serverName) throws LoginException, JSONException, IOException {
		return ProxmoxLib.getAllocatedMemFor(serverName) < Constants.MAX_THRESHOLD;
	}
}
