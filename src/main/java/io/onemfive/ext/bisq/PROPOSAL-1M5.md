# Censorship Resistance with 1M5 Integration
Proposal for Bisq Network to integrate [1M5](https://1m5.io) into its communication stack, inter-node and when accessing remote Bitcoin, seed, and pricing nodes (or any public node).

## Issue
[Censorship](https://en.wikipedia.org/wiki/Censorship) is the suppression of speech, public communication, or other information, on the basis that such material is considered objectionable, harmful, sensitive, politically incorrect or "inconvenient" as determined by government authorities or by community consensus. Bisq, by providing a decentralized exchange not registered in any jurisdiction, [is likely to come under attack by jurisdictions that consider it illegal](https://bitcoinexchangeguide.com/sec-vs-decentralized-exchanges-dexs-are-under-attack-from-the-securities-and-exchange-commission/).

Bisq runs its DApps as Tor hidden services using seeds to discover the Bisq network. Tor can be censored, [as is done in China](https://en.wikipedia.org/wiki/Internet_censorship_in_China#Using_Tor_and_DPI-resistant_tools), by discovering entry nodes and blocking them by IP. Bisq gets around this by offering bridges to bypass the blocks but these bridges can also be blocked, same with VPNs. There are an estimated 7000 Tor nodes in operation today with about 2000 of them entry nodes and 1000 exit nodes. To block 2000 entry nodes is easy work as they are provided publicly and it's not a large list. But Tor is the ideal method to connect to a remote Bitcoin node as they normally are ran in the clear via the internet.

## Opportunities
The [Invisible Internet Project (I2P)](https://geti2p.net/en/) is much less known and thus less targeted. It works somewhat similarly to Tor with a few differences. Where Tor shines when accessing clearnet sites privately, I2P shines accessing other nodes running I2P, e.g. peer-to-peer networking apps like messaging. I2P doesn't have entry nodes, every node is a potential entry node and there are roughly 65,000 I2P peers (all I2P peers by default are contributors to the network). I2P, like any application, has its vulnerabilities, but they are an active team working to constantly improve it. There is no known attempts to block the I2P network globally although when cellular towers are turned off or the user is behind a severely restrictive firewall (e.g. corporate), even I2P will not work as it requires internet access. This is where Bluetooth and WiFi Direct have become helpful.

[Bluetooth](https://en.m.wikipedia.org/wiki/Bluetooth) and [WiFi Direct](https://en.wikipedia.org/wiki/Wi-Fi_Direct) support sending messages to nearby peers to share information. This has been used successfully in demonstrations in Hong Kong among others. But what they do not do is propagate out until a peer with internet access is available and use that peer to complete a request for an internet resource. Bluetooth and WiFi Direct are implemented using low-powered radios in phones and PCs and they're easily jammed (WiFi more so than Bluetooth). When in this position, shortwave communications is necessary.

Using the full [radio spectrum](https://en.wikipedia.org/wiki/Radio_spectrum) is the next major advancement in commercial radio communications. Adding [anti-jamming](https://en.wikipedia.org/wiki/Electronic_counter-countermeasure) capabilities additionally will enhance censorship resistance further. But what happens when you're under serious jamming? LiFi is the next battlefield.

[LiFi](https://en.wikipedia.org/wiki/Li-Fi) is short for Light Fidelity. It is basically wireless light communications. A dongle with a LiFi transceiver can send/receive light as data to/from another LiFi transceiver and the only method for blocking it is to physically block the light. Line-of-sight is required although some bouncing can be supported with degraded bandwidth and range. 

## Proposal
Routing using Tor, I2P, Bluetooth, WiFi Direct, WiFi, Shortwave, and LiFi to ensure censorship resistance is the primary mission behind 1M5 - Invisible Matrix Services. It's designed to support additional future networks like Nym if/when they prove to be helpful. When integrated with Bisq, 1M5 would ensure each node can find each other and maintain communications regardless how attacked, even with a national or global internet shutdown. It would also ensure the same level of censorship-resistance when accessing remote Bitcoin, seed, or price nodes...any public node.

Development would require the ability to expose 1M5 as a proxy to Bisq, testing the integration, then documenting it. In addition, a 1M5 Operator/Admin Role would be added to monitor and report on 1M5 effectiveness fixing any bugs that come up while providing an expected level of support.

1M5 is open source under the Unlicense 'license' and free to use. Long-term it is desired to be managed and developed similarly to Bisq's DAO as a Decentralized Autonomous Mission.

1M5 currently can access the Tor and I2P networks. Ensuring Tor blocks get routed through I2P until a node with 1M5 that has access to Tor can complete the request needs a little extra work and testing. Additional networks will be integrated in the future.

* Integration via Proxy: 520 hours @ 1M Satoshis/hr (~$39k USD 1/2020)
    * Development: ~320 hours
    * Testing: ~160 hours
    * Documentation: ~40 hours
* 1M5 Operator/Admin Role: 25 hours/month @ 1M Satoshis/hr (~$1875 USD/month)
    * Keep 1M5 online and functioning normally: ~5 hrs/month
    * Keep 1M5 up to date with latest versions: ~5 hrs/month
    * Tune 1M5 for optimal performance based on feedback & local peer monitoring: ~5 hrs/month
    * Fix bugs in a timely manner: ~5 hrs/month
    * Be available for support issues: ~5 hrs/month
    * Report on any incidents

## Notes
The best timing for this integration is unknown especially considering budgeting issues, although DEXs are beginning to get attention.

### Resources
* [1M5 Site](https://1m5.io/)
* [1M5 GitHub](https://github.com/1m5)
* [1M5 Info Deck](https://1m5.io/assets/pdf/1m5-info-deck.pdf)
* [1M5 Whitepaper](https://1m5.io/assets/pdf/1m5-wp.pdf)
* [1M5 Docs](https://github.com/1m5/1m5-docs)
* [ObjectOrange GitHub](https://github.com/objectorange)
* [ObjectOrange LinkedIn](https://www.linkedin.com/in/decentralizationarchitect/)

### Keybase Conversations
* 12/14/2019 #chinese: @ff98sha verified I2P not blocked in Shanghai
* 12/14/2019 #chinese: @wiz perhaps believes bridges are good enough (I feel is not true and short-sighted)
* 12/28/2019 #general: @sunerok knows I2P amd favors it
* 12/31/2019 #dev: @mileschet likes 1M5 and has experience with Nym, a potential future integration


### Future Projects
Future 1M5 projects will request contributions from Bisq along with other users of 1M5 to spread costs
across the user community. Some future projects include:

* using Tor when I2P gets blocked
* using Bluetooth to propagate out requests (implementation started but on hold)
* using WiFi Direct for request propagation
* using Shortwave/Full Radio Spectrum (large scope that will be broken down)
* using LiFi (some research completed, vendor relationship in place)
* [Nym](https://nymtech.net/) potentially

## History
* Created and submitted as proposal #168 on 1/12/2020
