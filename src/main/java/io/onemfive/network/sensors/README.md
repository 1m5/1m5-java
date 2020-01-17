# 1M5 Sensors
Package for managing registered Sensors.

## Introduction
The radio component uses the full radio electromagnetic spectrum to aid in censorship resistant communications.
It was conceived due to the realization that cellular radios as well as WiFi are easy to jam and thus the need
for a more flexible range of radio options is necessary.
It requires GNU Radio to be installed locally and is in the very early stages of development.
This is its on-going [whitepaper](1M5-Radio-Whitepaper.md).

## Requirements

* Ability to use the full radio electromagnetic spectrum, from 3Hz to 3THz.
* Support sending/receiving messages globally with no infrastructure.
* Be able to use current infrastructure without a SIM card.
* Be able to use current infrastructure with a SIM card.

## SDRs
At this time, the [RTL-SDR](https://www.rtl-sdr.com/) dongle is being used to get initial software working and
it's expected to move towards using [HackRF](https://greatscottgadgets.com/hackrf/one/)
as the main production SDR. Additional SDRs are under review.

### RTL-SDR
https://www.rtl-sdr.com/

### HackRF
https://greatscottgadgets.com/hackrf/one/

### Ettus
70 MHz - 6 GHz: http://www.ettus.com/product-categories/usrp-bus-series/

### Fairwaves
https://fairwaves.co/products/equipment/

### Nuand
70 MHz (47 MHz TX) - 6 GHz: https://www.nuand.com/bladerf-2-0-micro/

## Installation
1. Install GNU Radio: https://wiki.gnuradio.org/index.php/InstallingGR
2, Install BlueCove

## Roadmap


## Potential future Sensors
+ NSTX - IP-over-DNS
+ ICMPTX - IP-over-ICMP
+ Blockstream
