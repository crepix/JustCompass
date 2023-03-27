package crepix.java_conf.gr.jp.justcompass.di

import crepix.java_conf.gr.jp.repository.DefaultDirectionRepository
import crepix.java_conf.gr.jp.repository.DirectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelRetainedBindModule {

    @Binds
    abstract fun bindDirectionRepository(repository: DefaultDirectionRepository): DirectionRepository
}
