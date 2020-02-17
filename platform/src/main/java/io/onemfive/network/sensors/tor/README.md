# 1M5 TOR Sensor
Tor Sensor

## Tor Embedded
To come...

## Tor External
Tor Sensor requires installing Tor Browser or running Tor as a router.
This is accomplished by the 1M5 Tor Sensor by using the local Tor's SOCKSv5 proxy address and port.
If you do not have Tor installed:

Tor Browser: https://www.torproject.org/download/download.html.en

Or as a daemon router:
1. sudo apt install tor -y
2. in /etc/tor/torrc uncomment line: ControlPort 9051
3. in /etc/tor/torrc uncomment line: CookieAuthentication 1
4. in /etc/tor/torrc replace: CookieAuthentication 1 with CookieAuthentication 0
5. tor
