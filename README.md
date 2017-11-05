# Proxmox LXC Manager

## Test the app

1. Set `USER_NAME` and `PASS_WORD` constants in `src/org/ctlv/proxmox/api/Constants.java`.
2. Run `src/org/ctlv/proxmox/tester/Main.java`.

## Project Overview

### org.ctlv.proxmox.api

- The `ProxmoxAPI` class was modified to introduce the `deleteCT()` method in order to remove programmatically a container from a node.
- The `RestClient` class was also slightly modified to make the `DefaultHttpClient` non-static in order to resolve multithreading issues.


### org.ctlv.proxmox.tester

The app's main file, composed of two parts:
- Information about nodes and CTs
- Manager and Generator executions

**To try the app you have to run only this file.**


### org.ctlv.proxmox.lib

An additional layer between the `ProxmoxAPI` and the app. Used to simplify the `Manager` and `Generator` while isolating some logic.


### org.ctlv.proxmox.generator

Create a new container on a node while the limit is not reached.

- The node is selected randomly between two servers (chances are 66% on `srv-px1` and 33% on `srv-px2`).
- The chosen limit is when our allocated memory reaches 16% of the total node memory.
  - We chose to use the allocated memory and not the used memory to achieve the limit more quickly.
  - Based on `LX`'s `maxmem` attribute (and not `LX`'s `mem` attribute which represents the used memory).
- All successful CT generations are separated with a 5s periodic event.
- When we create a new CT we wait 30s to start it. This value was found based on our tests.
- When a server is overloaded we also wait 30s before starting the generation process again.


### org.ctlv.proxmox.manager

Manage (Migrate / Remove) CTs from nodes.

- All monitoring cycles are separated with a 10s periodic event.
- Ran in a separated thread in parallel with the `Generator`.
- Check if a CT needs to be migrated from a node to another one (our allocated memory on a node reaches 8% of the total node memory)
- Check if CTs need to be deleted from both nodes (our allocated memory on both nodes reaches 12% of the total node memory)
- We chose to use the allocated memory and not the used memory to achieve the limit more quickly and be consistent with the `Generator`.
- Because our system is based on the allocated memory and not the used memory we don't just stop the CT but we also remove it from the node. We understand it's not a viable solution for a production-ready service. But it makes sense for us in order to demonstrate our proof-of-work app in its specific context.


## Credits

INSA Toulouse

Valerian Cazanave et Gautier Delorme
