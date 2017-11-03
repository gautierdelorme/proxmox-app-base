package org.ctlv.proxmox.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class ProxmoxLib {
	private static ProxmoxAPI api = new ProxmoxAPI();
	
//	Display node information in a nice human-readable way
	public static void printInfoFor(String serverName, boolean full) throws LoginException, JSONException, IOException {
		Node server1 = api.getNode(serverName);
		System.out.println("Name: " + serverName);
		System.out.println("CPU usage (%): " + 100*server1.getCpu());
		System.out.println("RAM usage (%): " + 100*(float)server1.getMemory_used()/server1.getMemory_total());
		System.out.println("Disk usage (%): " + 100*(float)server1.getRootfs_used()/server1.getRootfs_total());
		if (full) printCTsInfoFor(serverName);
	}
	
//	Display CT information in a nice human-readable way
	public static void printCTsInfoFor(String serverName) throws LoginException, JSONException, IOException {
		System.out.println("**** CTs on " + serverName + " ****");
		for (LXC lxc : api.getCTs(Constants.SERVER1)) {
			System.out.println("Name: " + lxc.getName());
			System.out.println("Status: " + lxc.getStatus());
			System.out.println("CPU usage (%): " + lxc.getCpu());
			System.out.println("RAM usage (%): " + 100*(float)lxc.getMem()/lxc.getMaxmem());
			System.out.println("Disk usage (%): " + 100*(float)lxc.getDisk()/lxc.getMaxdisk());
		}
	}

//	Return a list including all our own CTs from a node 
	public static List<LXC> selectOurCtsFrom(String serverName) throws LoginException, JSONException, IOException {
		List<LXC> ourCts = new ArrayList<LXC>();
		for (LXC lxc : api.getCTs(serverName)) {
			if (lxc.getName().startsWith(Constants.CT_BASE_NAME)) ourCts.add(lxc);
		}
		return ourCts;
	}
	
//	Return the ratio allocated RAM to our CTs on the total node's RAM
	public static float getAllocatedMemFor(String serverName) throws LoginException, JSONException, IOException {
		long attributedMem = 0;
		for (LXC lxc : selectOurCtsFrom(serverName)) {
			attributedMem += lxc.getMaxmem();
		}
		return (float) attributedMem/api.getNode(serverName).getMemory_total();
	}
	
//	Used to delete all our own CT from a node
	public static void deleteOurCtsFrom(String serverName) throws LoginException, JSONException, IOException, InterruptedException {
		for (LXC lxc : selectOurCtsFrom(serverName)) {
			deleteCtFrom(serverName, lxc);
		}
	}

//	Used to delete a CT from a node
	public static void deleteCtFrom(String serverName, LXC lxc) throws LoginException, JSONException, IOException, InterruptedException {
		if (lxc.getStatus().equals("running")) {
			api.stopCT(serverName, lxc.getVmid());
			Thread.sleep(1000 * Constants.STOP_WAIT_TIME);
		}
		api.deleteCT(serverName, lxc.getVmid());
	}
	
//	Used to migrate a CT from a node to another one
	public static void migrateCT(String from, String to) throws LoginException, JSONException, IOException, InterruptedException {
		LXC lxc = selectOurCtsFrom(from).get(0);
		if (lxc.getStatus().equals("running")) { // we cannot migrate an active container with our ProxmoxAPI implementation so we have to stop it
			api.stopCT(from, lxc.getVmid());
			Thread.sleep(1000 * Constants.STOP_WAIT_TIME);
		}
		api.migrateCT(from, lxc.getVmid(), to);
		Thread.sleep(1000 * Constants.GENERATION_WAIT_TIME);
		api.startCT(to, lxc.getVmid());
	}
	
//	Used to create a CT
	public static void createCT(String serverName, String ctName) throws LoginException, JSONException, IOException, InterruptedException {
		api.createCT(serverName, ctName, Constants.CT_BASE_NAME+ctName, Constants.RAM_SIZE[1]);
	}
	
//	Used to start a CT
	public static void startCT(String serverName, String ctName) throws LoginException, JSONException, IOException, InterruptedException {
		api.startCT(serverName, ctName);
	}
	
//	Used to delete the oldest CT on a node
	public static void deleteOldCTOn(String serverName) throws LoginException, JSONException, IOException, InterruptedException {
		List<LXC> cts = selectOurCtsFrom(serverName);
		LXC olderCT = cts.get(0);
		for (LXC lxc : cts) {
			if (lxc.getUptime() > olderCT.getUptime()) olderCT = lxc;
		}
		deleteCtFrom(serverName, olderCT);
	}
}
