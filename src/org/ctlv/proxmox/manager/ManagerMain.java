package org.ctlv.proxmox.manager;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.lib.ProxmoxLib;
import org.json.JSONException;

public class ManagerMain {

	public static void main(String[] args) {		
		(new Thread(new Runnable() { // create a thread to run our manager in the background
		    public void run() {
		    	while (true) {
					try {
						System.out.println("Watching...");
						if (needToStopOldCTs()) { // if we need to stop old CTs from SERVER1 and SERVER2
							System.out.println("Delete old cts...");
							ProxmoxLib.deleteOldCTOn(Constants.SERVER1);
							ProxmoxLib.deleteOldCTOn(Constants.SERVER2);
						} else if (needToMigrate(Constants.SERVER1, Constants.SERVER2)) { // if we need to migrate a CT from SERVER1 to SERVER2
							System.out.println("Migrating CT from " + Constants.SERVER1 + " to " + Constants.SERVER2 +"..");
							ProxmoxLib.migrateCT(Constants.SERVER1, Constants.SERVER2);
						} else if (needToMigrate(Constants.SERVER2, Constants.SERVER1)) { // if we need to migrate a CT from SERVER1 to SERVER2
							System.out.println("Migrating CT from " + Constants.SERVER2 + " to " + Constants.SERVER1 +"..");
							ProxmoxLib.migrateCT(Constants.SERVER2, Constants.SERVER1);
						}
						Thread.sleep(1000 * Constants.MONITOR_PERIOD);
					} catch (LoginException | JSONException | IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
		    }
		})).start();
	}
	
//	return true when we need to drop old CTs from all servers because the used RAM on both is more than 12% of the total node's RAM
	private static boolean needToStopOldCTs() throws LoginException, JSONException, IOException {
		return allocatedMemGreaterThanDroppingThreshold(Constants.SERVER1) && allocatedMemGreaterThanDroppingThreshold(Constants.SERVER2);
	}
	
//	return true when we need to migrate a CT from a server to another one
	private static boolean needToMigrate(String from, String to) throws LoginException, JSONException, IOException {
		return (allocatedMemGreaterThanMigrationThreshold(from) && !allocatedMemGreaterThanMigrationThreshold(to)) ||
				(allocatedMemGreaterThanDroppingThreshold(from) && !allocatedMemGreaterThanDroppingThreshold(to));
	}
	
//	return false as long as we don't use more than 8 percent of the total node's RAM
	private static boolean allocatedMemGreaterThanMigrationThreshold(String serverName) throws LoginException, JSONException, IOException {
		return ProxmoxLib.getAllocatedMemFor(serverName) > Constants.MIGRATION_THRESHOLD;
	}

//	return false as long as we don't use more than 12 percent of the total node's RAM
	private static boolean allocatedMemGreaterThanDroppingThreshold(String serverName) throws LoginException, JSONException, IOException {
		return ProxmoxLib.getAllocatedMemFor(serverName) > Constants.DROPPING_THRESHOLD;
	}

}
