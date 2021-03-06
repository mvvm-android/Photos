package com.github.sikv.photos.di.component

import android.content.Context
import com.github.sikv.photos.api.ApiClient
import com.github.sikv.photos.di.module.ApiModule
import com.github.sikv.photos.di.module.GlideModule
import com.github.sikv.photos.di.module.RetrofitModule
import com.github.sikv.photos.di.module.RoomModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
// TODO Remove RoomModule.
@Component(modules = [RetrofitModule::class, ApiModule::class, RoomModule::class, GlideModule::class])
interface NetworkComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): NetworkComponent
    }

    fun inject(apiClient: ApiClient)
}