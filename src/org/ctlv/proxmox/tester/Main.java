package org.ctlv.proxmox.tester;

import java.io.IOException;
import javax.security.auth.login.LoginException;
import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.generator.GeneratorMain;
import org.ctlv.proxmox.lib.ProxmoxLib;
import org.ctlv.proxmox.manager.ManagerMain;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException, InterruptedException {
//		********************
//		First part of the TP
//		********************
//		/!\ DO NOT FORGET TO SET USER_NAME and PASS_WORD in the Constants.java file.
//		********************
		
//		Print information about the node and CTs
		ProxmoxLib.printInfoFor(Constants.SERVER1, true);
		ProxmoxLib.printInfoFor(Constants.SERVER2, true);

//		********************
//		Second part of the TP
//		********************

		ManagerMain.main(null);
		GeneratorMain.main(null);

//		The next two lines are used to remove all the containers we created. You can use them if needed.
//		ProxmoxLib.deleteOurCtsFrom(Constants.SERVER1);
//		ProxmoxLib.deleteOurCtsFrom(Constants.SERVER2);
	}
}
