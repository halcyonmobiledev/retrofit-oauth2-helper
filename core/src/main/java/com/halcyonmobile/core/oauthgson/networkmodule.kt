/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core.oauthgson

import com.halcyonmobile.core.ExampleRemoteSource
import com.halcyonmobile.core.SessionExampleService
import com.halcyonmobile.core.SessionlessExampleService
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthgson.OauthRetrofitContainerWithGson
import com.halcyonmobile.oauthgson.OauthRetrofitWithGsonContainerBuilder
import com.halcyonmobile.oauthmoshikoin.NON_SESSION_RETROFIT
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Example network module which uses the [com.halcyonmobile.oauthgson.OauthRetrofitContainerWithGson] to create the needed two
 * retrofit instance, then provides two example service [SessionExampleService] [SessionlessExampleService].
 */
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        module {
            single { provideAuthenticationLocalStorage() }
            single { provideSessionExpiredEventHandler() }
            single {
                OauthRetrofitWithGsonContainerBuilder(
                    clientId = clientId,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl)
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionlessRetrofit }
            single { get<OauthRetrofitContainerWithGson>().gson }
        }
    )
}