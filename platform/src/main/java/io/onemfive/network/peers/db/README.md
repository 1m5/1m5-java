# Peer DB
Relational database of Peer information.

An embedded Derby instance is being used.
The instance can be found in users's directory at: 1m5/platform/services/io.onemfive.network.NetworkService/peerDB.

If you desire to connect to this instance, download and install tools.
https://db.apache.org/derby/papers/DerbyTut/install_software.html

* Test with: java org.apache.derby.tools.sysinfo

ij: https://db.apache.org/derby/papers/DerbyTut/ij_intro.html
* Launch ij: java org.apache.derby.tools.ij
* Connect in ij: connect 'jdbc:derby:/home/[user]/1m5/platform/services/io.onemfive.network.NetworkService/peerDB';
* select * from Peer;
* disconnect;
* exit;
