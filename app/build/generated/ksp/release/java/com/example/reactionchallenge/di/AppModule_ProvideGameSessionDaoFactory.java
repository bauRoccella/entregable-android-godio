package com.example.reactionchallenge.di;

import com.example.reactionchallenge.data.local.AppDatabase;
import com.example.reactionchallenge.data.local.dao.GameSessionDao;
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
public final class AppModule_ProvideGameSessionDaoFactory implements Factory<GameSessionDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideGameSessionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GameSessionDao get() {
    return provideGameSessionDao(dbProvider.get());
  }

  public static AppModule_ProvideGameSessionDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideGameSessionDaoFactory(dbProvider);
  }

  public static GameSessionDao provideGameSessionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGameSessionDao(db));
  }
}
