# 1M5 Monero
Integration with the Monero platform.

## Monero CLI Installation


    Download and extract the latest Monero CLI for your platform.

    Start Monero daemon locally: ./monerod --stagenet (or use a remote daemon).

    Create a wallet file if one does not exist.
        Create new / open existing: ./monero-wallet-cli --daemon-address http://localhost:38081 --stagenet
        Restore from mnemonic seed: ./monero-wallet-cli --daemon-address http://localhost:38081 --stagenet --restore-deterministic-wallet

    Start monero-wallet-rpc (requires --wallet-dir to run tests):

    e.g. For wallet name test_wallet_1, user rpc_user, password abc123, stagenet: ./monero-wallet-rpc --daemon-address http://localhost:38081 --stagenet --rpc-bind-port 38083 --rpc-login rpc_user:abc123 --wallet-dir ./
