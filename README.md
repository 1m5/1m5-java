# 1M5
A secure open-source decentralized censorship-resistant peer-to-peer application platform with end-to-end encryption
and anonymity as a base layer for creating easy to build and use secure decentralized peer-to-peer
applications requiring no server connections that can be used around the world by any person looking
to protect their communications and personal data from unethical monitoring, interception, intrusion,
and censorship.

## Version
0.6.3-SNAPSHOT

## Licensing
In the interests of ensuring the 1M5 mission, all copyright automatically imposed on the 1M5 project by any and all people
and organizations are removed and thus nullified. We believe this to be encompassed in the [Unlicense](https://unlicense.org/) statement.
All 1M5 services and sensors are created with the Unlicense statement by default unless otherwise specified,

Bouncycastle is embedded in 1M5 Core and its MIT-like license is [here](http://www.bouncycastle.org/licence.html).

## Authors / Developers
* objectorange (Brian Taylor) - [GitHub](https://github.com/objectorange) | [LinkedIn](https://www.linkedin.com/in/decentralizationarchitect/) | PGP: DD08 8658 5380 C7DF 1B4E 04C2 1849 B798 CF36 E2AF
* evok3d (Amin Rafiee) - [Site](https://arafiee.com/) | PGP: D921 C2EE 60BA C264 EA40 4DC5 B6F8 2589 96AA E505
* azad (Erbil Kaplan) - [LinkedIn](https://www.linkedin.com/in/erbil-kaplan-b8971b18/) | PGP: 2EBC 2239 E9B8 2BCA 7176 77FE FD80 A0C2 95FD EBAC
* z0??0z

## Contributions
Contributions can be made by forking 1M5 in GitHub, making a change, and making a pull request with an explanation on what
the change provides. If accepted, it will be merged into the main branch. If a bounty was placed on an issue that was
fixed by the change, it will paid out at that point by requesting a crypto address from you. If many improvements are done
over time, you may be invited to join our team. If you have a large addition you would like to make, please send the team
a proposal via info@1m5.io and we will review and get back with you. Our team is kept semi-private with some members
trying for complete anonymity. We respect your right to privacy whether you decide to be entirely public, completely
anonymous, or somewhere in between. That needs to come from each individual's circumstances.

If you ever see the codebase with no commits for a long period of time with no communications, please fork and continue
the effort. Global free speech is imperative to a free humanity from those few who wish to enslave the rest under any
guise.

Also feel free to fork and go your own way if you desire. All work is in the public domain of humanity; no one
person nor organization has any right to control speech, an inalienable individual natural right, and thus the flow
of information which includes money.

## Bounties
Paid in Bitcoin (BTC), Monero (XMR), or whatever crypto-currency is desired, available, and agreed upon.

## Donations
Please send donation requests to info@1m5.io for an address.

## Opportunities
[**Freedom of Speech**](https://en.wikipedia.org/wiki/Freedom_of_speech) - a principle that supports the freedom of
an individual or a community to articulate their opinions and ideas without fear of retaliation, censorship,
or sanction. The term "freedom of expression" is sometimes used synonymously but includes any act of seeking,
receiving, and imparting information or ideas, regardless of the medium used.

[**Censorship**](https://en.wikipedia.org/wiki/Censorship) - the suppression of speech, public communication,
or other information, on the basis that such material is considered objectionable, harmful, sensitive,
politically incorrect or "inconvenient" as determined by government authorities or by community consensus.

Constraining the free flow of information between people is a direct threat to our freedom and censorship of
communications on-line is growing world-wide.

- https://internetfreedomwatch.org/timeline/
- https://www.wired.com/2017/04/internet-censorship-is-advancing-under-trump/
- https://rsf.org/en/news/more-100-websites-blocked-growing-wave-online-censorship

On-line communications are censored at the point of entrance by Internet Service Providers (ISP).
They act as gateways to the internet providing governments control over speech by having the
ability to restrict usage and track people's usage via their leased IP addresses. In order to make tracking usage much more
difficult, tools have come out that provide techniques called onion-/garlic-routing where the source and destinations of
internet routes can not be determined without breaking encryption, a very expensive feat, sometimes impossible today when
considering the encryption algorithms used.

Two primary tools today that support this are Tor and I2P. Tor provides a browser
that makes it easier to use while I2P is much less known. Both are complementary in that Tor was designed for browsing
today's current web sites anonymously. I2P was designed for peer-to-peer communications within I2P. Neither have good
APIs for developers to embed in their products making uptake slow for many applications.

A third tool on the horizon is one that completely circumvents ISPs by not using them. They're called direct wireless
mesh networks and they can communicate directly device-to-device using technologies such as WiFi Direct. Firechat is an
example used during the 2014 Hong Kong protests after the Chinese government threatened to shutdown the internet in that
area. New mesh solutions are popping up including RightMesh that seek to improve on earlier designs. But the technology
is still in its infancy and needs to be pulled into ever day applications more easily once they've matured.

Even getting these technologies in wide use doesn't solve the problem of online censorship. People in governments, corporations, and
other thieves are constantly finding ways to circumvent these technologies to censor and steal information.

In addition:

- Most organizations today (e.g. Tech, Banks, Governments, Hospitals) track, persist, and use our behavior for their profit not ours.
- Centralized organizations are major targets for theft.
- Closed source software can easily contain hidden back doors for thieves to access our information without our knowledge and many open source applications have closed source libraries embedded in them.
- Whistleblowers, the abused, visible minorities, and a myriad of other people could be emboldened by anonymity to speak out in a manner that would otherwise be unavailable if they were forced to identify themselves.
- Decentralized applications and cryptocurrencies like Bitcoin are helping to wrestle some control from centralized organizations although they are largely used on servers and distributed ledgers are still logically centralized and difficult to maintain anonymity at the network layer.
- Smartphone ownership around the world is greater than PC ownership.
- Smartphones, our primary means of global communication and collaboration, are weak in maintaining our anonymity and privacy - critical to ensuring individual freedom.

## Solution
1M5 works to solve these issues by providing three primary benefits.

1. Intelligent Censorship-Resistant Anonymous Router embedding Tor, I2P, Direct Wireless Ad-Hoc Networks, and other
networks, using them intelligently as one dynamic censorship-resistant, end-to-end encrypted, anonymous mesh network.
2. Offers access to commonly desired decentralized services in an anonymous fashion including
self-sovereign decentralized identities (DID), Bitcoin, and other privacy preserving services.
3. Provides easy to use APIs for developers to embed in their applications to facilitate up-take.

### Routing
We provide a Maneuvering Condition (ManCon) setting to signal what level of maneuvering is likely required to prevent censorship.
This should be set by the end user based on their circumstances. They should also be made aware of recommended ManCon
levels for the jurisdiction they are currently in. These ManCon levels are largely based on [Press Freedom Index](https://en.wikipedia.org/wiki/Press_Freedom_Index)
and updated on the [ManCon page](https://1m5.io/mancon.html).

Setting the ManCon level manually by the end user informs the codebase what based level of ManCon should be used although
final ManCon is determined by blocks encountered during routing and thus how to ratchet up resistance as these blocks occur.

For developers using the API, all requests for services, e.g. Bitcoin, require an Envelope with a ManCon level set. This ManCon level decides
what base level of privacy is desired. Options are Low, Medium, High, Very High, Extreme, and Neo.
All P2P communications use High ManCon as the default resulting in the use of I2P with latency expectations between 200 milliseconds and 2 seconds.
This is the default setting in the Envelope.

When making web requests, remember to set the appropriate ManCon level otherwise all web requests will use the HIGH ManCon
level thereby routing all web requests through the I2P layer to 1M5 nodes with Tor enabled resulting in higher than might be
expected latencies yet very censorship-resistant and private page views. This is the ideal setup for people in China as an example wishing
to view web pages globally without censorship and without getting a knock on their door where Tor is getting heavily blocked.

#### LOW - MANCON 5
Open/normal SSL based communications with no expected censorship or privacy intrusion attempts is the norm.

Examples: Norway, Iceland, Costa Rica, Jamaica, Ireland

* Web: I2P used for .i2p addresses and Tor for other web requests including .onion addresses. If that fails, it will be assumed that the site is down.
* P2P (Messenger): I2P used unless found to be blocked. Then Tor will be used as a tunnel to a peer that has I2P enabled.
If Tor blocked, will ratchet up to 1DN for assistance.

Expect latencies of 500 milliseconds to 2 seconds unless 1DN is needed.

#### MEDIUM - MANCON 4
Normal censorship attempts by states on reading news (public web sites getting blocked, government shutdown of cloud cdn content).
Many moving towards using Tor and/or VPNs although no fear of circumventing censorship attempts.

Examples: Australia, United States, France, United Kingdom

* Web: Tor will be used. If that fails, the request will be forwarded to other peers until a peer can make the request
returning the result directly back to the requesting peer. If those fail, it will be assumed that the site is down.
* P2P unchanged

Expect latencies of 500 milliseconds to 4 seconds unless 1DN is needed.

#### HIGH - MANCON 3
Tor and VPNs are beginning to get blocked. Many beginning to move to I2P. Some self-censorship likely. This is the default setting for 1M5.

Examples: Brazil, Greece, Poland, Panama, Nicaragua

* Web: will use an I2P peer that has access to Tor to make the request.
* P2P unchanged

Expect latencies of 4-10 seconds.

#### VERYHIGH - MANCON 2
I2P is getting attacked slowing the network and people are beginning to get threatened for circumventing censorship attempts resulting in self-censorship.

Examples: Mexico, Venezuela, Russia, India, Turkey

* Web: will use I2P with random delays to forward all requests to a 1M5 peer with Tor access at a lower ManCon. If both I2P and
Tor blocked at end user, 1DN will be used to find a 1M5 peer at a lower ManCon to fulfill the request.
* P2P: will use I2P with random delays of 4-10 seconds. If I2P gets blocked, will attempt to use Tor as a tunnel. If that is blocked,
1DN will be used.

Expect latencies of 6-16 seconds unless 1DN used which could result in very large latencies where only asynchronous messaging
(e.g. Email) and asynchronous web requests are plausible.

#### EXTREME - MANCON 1
Internet has been blocked for end user, e.g. local cellular service towers shutdown or provider turns off access and/or
threats of imprisonment and/or death are made to induce self-censorship with actual evidence of enforcement.

Examples: China, North Korea, East Turkestan, Iran, Saudi Arabia, Iraq, Egypt

* Web: 1DN will be used to forward requests to Tor users with a lower ManCon to fulfill the request.
* P2P: 1DN peers will be used until a peer with I2P access can route the request.

Expect wide-ranging latencies.

#### NEO - MANCON 0
Whistleblower with deep state top secrets or investigative journalist with life-threatening information.

* Web: 1DN is used to forward requests to a peer that will then request another peer using I2P with high delays to make the Tor request.
* P2P: 1DN is used to forward a message through a random number and combination of 1DN/I2P peers at random delays of up to 90 seconds
at the I2P layer and up to 3 months at the 1M5 layer. A random number of copies (3 min 12 max) of the message are sent out
with the end user having a 12 word mnemonic passphrase as the only key to the data.

Wide-ranging latencies but highest privacy and censorship-resistance.

## Design
1M5 is composed of a Service-Oriented Architecture (SOA) design using a minimalistic service bus for micro-services,
a Staged Event-Driven Architecture (SEDA) design for asynchronous multi-threaded inter-service communications,
a service registry, internal core services, and a number of Sensors for advanced intelligent interaction with peers.

Key internal services include:

* Enterprise Applications Integration (EAI) Routing service for providing data-driven inter-service routing patterns
* Key Chain Service with key management and encryption/decryption features

Key add-on services include:

* Network Service for integrating different Sensor Managers for intelligent censorship-resistant routing
* Decentralized IDentification (DID) Service for authentication/authorization and reputation building (Web of Trust)

## [Implementation](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md)
The application is written in Java using JDK 1.8 although some services may support older versions.

Documentation of the Core starts [here](https://github.com/1m5/core/tree/master/src/main/java/io/onemfive/core/README.md).

## Integrations

### [Bisq](https://bisq.network)
Decentralized Exchange for Bitcoin/Fiat as well as other altcoins. [Proposal for future integration added to Bisq](https://github.com/bisq-network/proposals/issues/168).

### [Inkrypt](https://inkrypt.io)

#### Anonymous Decentralized Cloud
An anonymous decentralized Content Delivery Network (DCDN). Use of OpenPGP keys supported as well as
AES 256 encryption for content.

#### nLightn
Inkrypt is building a decentralized censorship resistant network for citizen journalists
to store and publish articles fighting government
oppression of the right to free speech globally. They need censorship resistant identities
to protect journalists from harm yet support their ability to build a reputation as a trusting
source for news and to ensure that information is also not censored nor stolen yet allows
the journalist to release portions of the information as they desire to whom they desire
to share it with to include no one or everyone (global public).

## Support
Support can be purchased with Bitcoin when availability exists. Please request support
at: info@1m5.io

## Fund Raising
1M5 is funded entirely through donations, support, and volunteers.
The following are current and potential donation sources.

### [Alex Jones](https://en.wikipedia.org/wiki/Alex_Jones)
An American radio show host pushing the boundaries on free speech in the United States.

* https://www.infowars.com/

### [American Civil Liberties Union (ACLU)](https://www.aclu.org/)

* https://www.aclu.org/issues/national-security/privacy-and-surveillance/nsa-surveillance

### [Anonymous](https://en.wikipedia.org/wiki/Anonymous_(group))
A decentralized international hacktivist group.
"We are Anonymous. We are Legion. We do not forgive. We do not forget. Expect us."
Broadly speaking, Anons oppose Internet censorship and control and the majority of their actions target governments,
organizations, and corporations that they accuse of censorship.

### [Electronic Frontier Foundation](https://www.eff.org/)

* https://www.eff.org/issues/privacy
* https://ssd.eff.org/

### [Freedom of the Press Foundation](https://freedom.press/)
FPF assists with crowdfunding those projects aiming to improve on journalism tools aimed at security.
The problem is that they only take donations via card and PayPal which is anything but anonymous.

* https://freedom.press/crowdfunding/

### [Freedom's Phoenix](https://www.freedomsphoenix.com/)
Declare your independence with Ernest Hancock.

### [The Guardian Project](https://guardianproject.info/)
Creates easy to use secure apps, open-source software libraries, and customized mobile devices that can
be used around the world by any person looking to protect their communications and personal data
from unjust intrusion, interception and monitoring.

### [Inkrypt](https://www.inkrypt.io/)
Censorship-Resistant Decentralized Content Distribution Network with OpenPGP/AES Encryption and Anonymity as a base.

### [Purism](https://puri.sm/)
A security and freedom-focused computer manufacturer based in San Francisco, founded in 2014 with the
fundamental goals of combining the philosophies of the Free Software movement with the hardware
manufacturing process, and to make it easy for individuals and businesses to have computers that
they can trust and feel safe using, i.e. making security more convenient than not.

### [ODD Reality](https://videofull.net/channel?id=UCuftdXePz6z73Wsg8Ao5lTg)

### [Russell Brand](https://www.russellbrand.com/)

## Threats & Counter Measures
Censorship attempts can be made in a myriad of ways and are ever changing as technology changes and attackers grow in experience.
Below are some of these methods and how 1M5 and composite networks mitigate them.

### General Information

#### I2P
Design of I2P started in 2003 soon after TOR and Freenet came on the scene. It uses many of the features of onion routing
as in TOR but also adds some enhancements which earned its routing the name of 'garlic routing'. The I2P team has identified
threats, some specific to I2P, and mitigations.

Resources
* [Site](https://geti2p.net/en/docs/how/threat-model)

#### TOR

#### 1DN

#### 1M5

### DNS Blocking
Inbound and outbound blocking of IP addresses by DNS servers.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_blocking)

#### I2P

#### TOR

#### 1DN

#### 1M5

### DNS Poisoning
Corruption in a DNS server's resolver cache by swapping out valid IP addresses with invalid addresses resulting in traffic divertion.

Resources
* [Wiki](https://en.wikipedia.org/wiki/DNS_spoofing)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Brute Force
This attack is performed by watching all messages passing between all nodes attempting to correlate messages into paths.

#### I2P
Difficult attack to initiate against I2P as all peers are sending messages frequently across the network (end-to-end and for network maintenance)
and each end-to-end message changes in size and data along its path. The attacker also does not have access to the messages
as inter-router communications are encrypted and in mutiple 'cloves' (garlic) so that two messages each 1kb in size would
appear as one 2kb message.

A bruce force attacker can induce trends by sending an unusually large payload to an I2P destination while monitoring all
network connections eliminating all nodes that didn't receive it. The cost to mount this kind of brute force attack on
I2P would be very expensive so not a high priority.

Preventing a single router or group of routers from attempting to route all I2P traffic is ensured due to each router
placing limits on the number of tunnels that can be routed by a single peer.

#### TOR

#### 1DN

#### 1M5
The 1M5 network is slated to provide random delays across its nodes and extended persistent delays (e.g. months),
and bandwidth throttling on streams to help combat this attack at the application layer.

### Timing
Timing attacks seek to correlate messages across the network based on their latencies combined with expected behavioral
patterns, e.g. HTTP requests receive responses.

#### I2P
I2P uses unidirectional datagrams so there are no replies although this is not the case with streaming and the guaranteed
delivery mode.

#### TOR

#### 1DN

#### 1M5

### Intersection

#### I2P

#### TOR

#### 1DN

#### 1M5

### Tagging

#### I2P

#### TOR

#### 1DN

#### 1M5

### Partitioning

#### I2P

#### TOR

#### 1DN

#### 1M5

### Predecessor

#### I2P

#### TOR

#### 1DN

#### 1M5

### Harvesting

#### I2P

#### TOR

#### 1DN

#### 1M5

### Traffic Analysis Identification

#### I2P

#### TOR

#### 1DN

#### 1M5

### Sybil

#### I2P

#### TOR

#### 1DN

#### 1M5

### Buddy Exhaustion

#### I2P

#### TOR

#### 1DN

#### 1M5

### Cryptographic

#### I2P

#### TOR

#### 1DN

#### 1M5

### Floodfill Anonymity

#### I2P

#### TOR

#### 1DN

#### 1M5

### Central Resource

#### I2P

#### TOR

#### 1DN

#### 1M5

### Development

#### I2P

#### TOR

#### 1DN

#### 1M5

### Implementation (Bugs)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Blocklists

#### I2P

#### TOR

#### 1DN

#### 1M5

### Distributed Denial of Service (DDoS)
A network-attack in which the perpetrator seeks to make a machine or network resource unavailable to its intended users
by temporarily or indefinitely disrupting services of a networked host.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Denial-of-service_attack)

#### Greedy User

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Starvation

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Flooding

#### I2P

#### TOR

#### 1DN

#### 1M5

#### Ping Flood

##### I2P

##### TOR

##### 1DN

##### 1M5

#### CPU Loading

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Floodfill

##### I2P

##### TOR

##### 1DN

##### 1M5

#### ReDoS

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Twinge

##### I2P

##### TOR

##### 1DN

##### 1M5

#### SYN Flood
DDos attacks by initiating TCP/IP handshakes but either not responding with a final ACK or responding with a different IP address.

Resources
* [Wiki](https://en.wikipedia.org/wiki/SYN_flood)

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Layer 7
DDoS attacks on application-layer processes.

Resources

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Ping of Death

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Smurf Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack)

##### I2P

##### TOR

##### 1DN

##### 1M5

#### Fraggle Attack

Resources
* [Wiki](https://en.wikipedia.org/wiki/Smurf_attack#Fraggle_Attack)

##### I2P

##### TOR

##### 1DN

##### 1M5

### Advanced Persistent Threat (APT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains
undetected for an extended period.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_persistent_threat)

#### I2P

#### TOR

#### 1DN

#### 1M5

### Advanced Volatile Threat (AVT)
A stealthy computer network attack in which a person or group gains unauthorized access to a network and remains
undetected in memory never persisting to the hard-drive circumventing investigative techniques.

Resources
* [Wiki](https://en.wikipedia.org/wiki/Advanced_volatile_threat)

#### I2P

#### TOR

#### 1DN

#### 1M5
