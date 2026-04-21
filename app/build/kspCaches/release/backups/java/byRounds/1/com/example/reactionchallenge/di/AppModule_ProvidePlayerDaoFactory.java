package com.example.reactionchallenge.di;

import com.example.reactionchallenge.data.local.AppDatabase;
import com.example.reactionchallenge.data.local.dao.PlayerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AppModule_ProvidePlayerDaoFactory implements Factory<PlayerDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvidePlayerDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayerDao get() {
    return providePlayerDao(dbProvider.get());
  }

  public static AppModule_ProvidePlayerDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvidePlayerDaoFactory(dbProvider);
  }

  public static PlayerDao providePlayerDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePlayerDao(db));
  }
}
