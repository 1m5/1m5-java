# 1M5 Network Peers
Package for managing local peer and known remote peers.

## Example Selecting Peers based on Lowest Latency by Graph Peer Manager
Peer A wishes to send a message to Peer C at lowest latency path:

Using only I2P:

* Peer A to Peer C avg latency with I2P is 10 seconds
* Peer A to Peer B avg latency with I2P is 2 seconds
* Peer B to Peer C avg latency with I2P is 4 seconds

In this case Peer A will use Peer B to get to Peer C with a likely latency result of 6 seconds.

But if Tor was used:

* Peer A to Peer C avg latency with Tor is 5 seconds
* Peer A to Peer B avg latency with Tor is 4 seconds
* Peer B to Peer C avg latency with Tor is 6 seconds

In this case Peer A will send directly to Peer C with Tor at a likely latency of 5 seconds

And Using Bluetooth:

* Peer A to Peer C avg latency with Bluetooth is 1/2 second (they are physically next to each other)
* Peer A to Peer B avg latency with Bluetooth is 30 seconds (many hops)
* Peer B to Peer C avg latency with Bluetooth is 30 seconds

Peer A easily sends directly to Peer C with Bluetooth at a likely latency of 1/2 second

If we use all networks to determine, Bluetooth will be selected using path A -> C.

If then Peer C turns off Bluetooth and all networks are evaluated, Tor will be selected A -> C (5 seconds).

But say Peer C's Tor access gets blocked, then I2P with path A -> B -> C will be selected.

But say Peer B shows up near Peer A and turns on their Bluetooth with a result in avg latency 
with Bluetooth A -> B of 1/2 second, now the path to C will be A -> B using Bluetooth and B -> C using I2P 
with an expected latency of 4.5 seconds.