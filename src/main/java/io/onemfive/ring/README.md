# 1m5-ring
A decentralized metric-based reputation protocol.

## Abstract
In decentralized systems there are some problems that are much easier to solve
in a more centralized manner, e.g. search, availability, caching. 
How do we support centralization of some functionality without losing the benefits of
decentralization? Build a reputation-based cluster acting in a centralized way
yet not completely relied upon for the overall system to function. That is, if the
cluster were to fail as a whole, the system would revert back to a pure decentralized
manner potentially increasing latency but not breaking the system. Ring is an implementation of
this concept. It can be thought of as a decentralized Apache Cassandra with 
membership based on decentralized reputation metrics.

## Requirements

- Anyone can create a ring
- Membership acceptance requirements are defined during creation
- Cryptography is used to identify nodes
- ...