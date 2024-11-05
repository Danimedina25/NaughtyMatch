package com.danifitdev.naughtymatch.di

import com.danifitdev.naughtymatch.data.repository.LoginRepositoryImpl
import com.danifitdev.naughtymatch.domain.repository.LoginRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository
}