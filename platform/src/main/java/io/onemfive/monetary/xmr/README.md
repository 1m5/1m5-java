# 1M5 Monero
Integration with the Monero platform.

## Monero CLI Installation

https://web.getmonero.org/

    Download and extract the latest Monero CLI for your platform.

    Start Monero router locally: ./monerod --stagenet (or use a remote router).

    Create a wallet file if one does not exist.
        Create new / open existing: ./monero-wallet-cli --router-address http://localhost:38081 --stagenet
        Restore from mnemonic seed: ./monero-wallet-cli --router-address http://localhost:38081 --stagenet --restore-deterministic-wallet

    Start monero-wallet-rpc (requires --wallet-dir to run tests):

    e.g. For wallet name test_wallet_1, user rpc_user, password abc123, stagenet: ./monero-wallet-rpc --router-address http://localhost:38081 --stagenet --rpc-bind-port 38083 --rpc-login rpc_user:abc123 --wallet-dir ./

## Integration
Lots of issues with Kovri/Sekreta in Monero,

Recommended moving to 1M5 [here](https://github.com/monero-project/monero/pull/6276).

https://github.com/monero-project/monero/blob/master/ANONYMITY_NETWORKS.md

Submitted formal proposal to evaluate integrating with 1M5 [here](https://repo.getmonero.org/monero-project/ccs-proposals/merge_requests/127)

### Monero
