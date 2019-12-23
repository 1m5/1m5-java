# 1m5-bitcoin
A censorship resistant full Bitcoin client and a private Simple Payment Verification (SPV) client.

It uses Tor for accessing the Bitcoin blockchain until blocked then sidesteps blocks using
I2P/1DN to get to a 1M5 node that has access to Tor using that node or uses 1DN to complete the
transaction, e.g. satellite.

It can be used as a lightweight (SPV) client to verify that a transaction is included in the Bitcoin blockchain, 
without downloading the entire blockchain. The client only needs to download the block headers, 
which are much smaller than the full blocks. To verify that a transaction is in a block, it requests 
a proof of inclusion, in the form of a Merkle branch.

Or it can be used as a heavyweight client requiring a full Bitcoin node to be present which is the 
most secure setup.

This implementation borrows heavily from the BitcoinJ project on githhub: https://bitcoinj.github.io/

BitcoinJ was not chosen as an implementation itself as it communicates directly with a Bitcoin node over
the wire and 1M5 requires all communications to go through it to ensure communications don't become blocked.
So in essence, this project's goal is a censorship resistant AND private BitcoinJ implementation. BitcoinJ
is maintained under the open source Apache 2.0 License.

1M5-Bitcoin is maintained under the Unlicensed 'license' placing it in the global public domain with no copyright.