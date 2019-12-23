# io.onemfive.network.sensors.radio package
The radio package consists of the following classes and packages.

## Classes
* [RadioSensor](RadioSensor.java) - this class is the entry point into the whole package.
It is instantiated within the SensorsService when configured so. On startup, this object
loads the configured radio defined in radio-sensor.config, instantiates it, then starts
up the particular Radio. The current default defined in Maven is [GNURadio](vendor/gnuradio/GNURadio.java)
found in the vendor package. Once the radio has started, a [RadioSession](RadioSession.java)
is instantiated and configured for each entry in the signals.json file found on the classpath.


## Packages
* balloons - balloons as a radio relay
* bands - radio bandwidth defined by ITU
* channels - specific radio frequency selected for communications
* contacts - communication codes
* detection - detection of radio communications
* discovery - discovery of 1M5 radio peers
* drones - drones as a radio relay
* filters - filtering of incoming/outgoing messages
* hardware - hardware-specific configuration information
* jamming - detection and handling of jamming
* satellite - satellite-specific communication support
* signals - use of bands for a specific communication need
* spread - methods by which a signal generated with a particular bandwidth is deliberately spread in the frequency domain resulting in a signal with a wider bandwidth.
* tasks - tasks ran in their own thread
* tuning - tuners for tuning a radio to specific frequencies
* vendor - software vendor integrations