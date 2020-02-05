# Release Notes

## 0.6.3
+ Provide Operation interface - not in wide use yet though
+ Move all code to one project to simplify development and releases
+ Add OpenJFX as GUI
+ Additional information saved with Peer relationships in graph
+ Basic DID support started in GUI
+ General GUI frame created
+ Separate desktop into its own module so that the router module can be ran as a sole daemon for environments without graphics support
+ Added CLI module
+ Added API module for simplifying access to Router deamon by both CLI and Desktop modules
+ Moved Data and Utils into a common module to be shared by all projects
+ Moved all Peer information except id to an embedded Derby database to relieve the graph of excess data
+ Greatly improved sensor/network/mancon routing rules
+ Add Network Ops concept, not yet in full use

