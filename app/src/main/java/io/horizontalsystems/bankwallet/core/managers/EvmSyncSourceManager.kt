package io.horizontalsystems.bankwallet.core.managers

import io.horizontalsystems.bankwallet.BuildConfig
import io.horizontalsystems.bankwallet.core.providers.AppConfigProvider
import io.horizontalsystems.bankwallet.core.storage.BlockchainSettingsStorage
import io.horizontalsystems.bankwallet.entities.EvmSyncSource
import io.horizontalsystems.ethereumkit.models.RpcSource
import io.horizontalsystems.ethereumkit.models.TransactionSource
import io.horizontalsystems.marketkit.models.BlockchainType
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.net.URL

class EvmSyncSourceManager(
    appConfigProvider: AppConfigProvider,
    private val blockchainSettingsStorage: BlockchainSettingsStorage
    ) {

    private val syncSourceSubject = PublishSubject.create<BlockchainType>()

    val syncSourceObservable: Observable<BlockchainType>
        get() = syncSourceSubject

    val defaultSyncSources: Map<BlockchainType, List<EvmSyncSource>> =
        mapOf(
            BlockchainType.Ethereum to listOf(
                if (BuildConfig.DEBUG)
                    getSyncSource(
                        BlockchainType.Ethereum,
                        "TestNet Websocket",
                        RpcSource.goerliInfuraWebSocket(appConfigProvider.infuraProjectId, appConfigProvider.infuraProjectSecret),
                        TransactionSource.goerliEtherscan(appConfigProvider.etherscanApiKey)
                    )
                else
                    getSyncSource(
                        BlockchainType.Ethereum,
                        "MainNet Websocket",
                        RpcSource.ethereumInfuraWebSocket(appConfigProvider.infuraProjectId, appConfigProvider.infuraProjectSecret),
                        TransactionSource.ethereumEtherscan(appConfigProvider.etherscanApiKey)
                    ),

                if (BuildConfig.DEBUG)
                    getSyncSource(
                        BlockchainType.Ethereum,
                        "TestNet HTTP",
                        RpcSource.goerliInfuraHttp(appConfigProvider.infuraProjectId, appConfigProvider.infuraProjectSecret),
                        TransactionSource.goerliEtherscan(appConfigProvider.etherscanApiKey)
                    )
                else
                    getSyncSource(
                        BlockchainType.Ethereum,
                        "MainNet HTTP",
                        RpcSource.ethereumInfuraHttp(appConfigProvider.infuraProjectId, appConfigProvider.infuraProjectSecret),
                        TransactionSource.ethereumEtherscan(appConfigProvider.etherscanApiKey)
                    )
            ),

            BlockchainType.BinanceSmartChain to listOf(
                getSyncSource(BlockchainType.BinanceSmartChain, "Default HTTP", RpcSource.binanceSmartChainHttp(), TransactionSource.bscscan(appConfigProvider.bscscanApiKey)),
                getSyncSource(BlockchainType.BinanceSmartChain, "BSC-RPC HTTP", RpcSource.bscRpcHttp(), TransactionSource.bscscan(appConfigProvider.bscscanApiKey)),
                getSyncSource(BlockchainType.BinanceSmartChain, "Default WebSocket", RpcSource.binanceSmartChainWebSocket(), TransactionSource.bscscan(appConfigProvider.bscscanApiKey))
            ),

            if (BuildConfig.DEBUG)
                BlockchainType.Polygon to listOf(
                    getSyncSource(BlockchainType.Polygon, "Polygon-RPC HTTP",
                        RpcSource.Http(listOf(URL("https://matic-mumbai.chainstacklabs.com")), null), TransactionSource(
                        "mumbai.polygonscan.com",
                        TransactionSource.SourceType.Etherscan("https://api-testnet.polygonscan.com", "https://mumbai.polygonscan.com", appConfigProvider.snowtraceApiKey)
                    ))
                )
            else
                BlockchainType.Polygon to listOf(
                    getSyncSource(BlockchainType.Polygon, "Polygon-RPC HTTP", RpcSource.polygonRpcHttp(), TransactionSource.polygonscan(appConfigProvider.polygonscanApiKey))
                ),

            if (BuildConfig.DEBUG)
                BlockchainType.Avalanche to listOf(
                    getSyncSource(BlockchainType.Avalanche, "Avax.network", RpcSource.Http(listOf(URL("https://api.avax-test.network/ext/bc/C/rpc")), null), TransactionSource(
                        "testnet.snowtrace.io",
                        TransactionSource.SourceType.Etherscan("https://api-testnet.snowtrace.io", "https://testnet.snowtrace.io", appConfigProvider.snowtraceApiKey)
                    ))
                )
            else
                BlockchainType.Avalanche to listOf(
                    getSyncSource(BlockchainType.Avalanche, "Avax.network", RpcSource.avaxNetworkHttp(), TransactionSource.snowtrace(appConfigProvider.snowtraceApiKey))
                ),


            BlockchainType.Optimism to listOf(
                getSyncSource(BlockchainType.Optimism, "Optimism.io HTTP", RpcSource.optimismRpcHttp(), TransactionSource.optimisticEtherscan(appConfigProvider.optimisticEtherscanApiKey))
            ),

            BlockchainType.ArbitrumOne to listOf(
                getSyncSource(BlockchainType.ArbitrumOne, "Arbitrum.io HTTP", RpcSource.arbitrumOneRpcHttp(), TransactionSource.arbiscan(appConfigProvider.arbiscanApiKey))
            )
        )

    private fun getSyncSource(blockchainType: BlockchainType, name: String, rpcSource: RpcSource, transactionSource: TransactionSource) =
        EvmSyncSource(
            "${blockchainType.uid}|${name}|${transactionSource.name}|${rpcSource.urls.joinToString(separator = ",") { it.toString() }}",
            name,
            rpcSource,
            transactionSource
        )

    fun getAllBlockchains(blockchainType: BlockchainType): List<EvmSyncSource> =
        defaultSyncSources[blockchainType] ?: listOf()

    fun getSyncSource(blockchainType: BlockchainType): EvmSyncSource {
        val syncSources = getAllBlockchains(blockchainType)

        val syncSourceName = blockchainSettingsStorage.evmSyncSourceName(blockchainType)
        val syncSource = syncSources.firstOrNull { it.name == syncSourceName }

        return syncSource ?: syncSources[0]
    }

    fun save(syncSource: EvmSyncSource, blockchainType: BlockchainType) {
        blockchainSettingsStorage.save(syncSource.name, blockchainType)
        syncSourceSubject.onNext(blockchainType)
    }

}
