package com.example.reactionchallenge.data.repository;

import com.example.reactionchallenge.data.local.dao.GameSessionDao;
import com.example.reactionchallenge.data.local.dao.PlayerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GameRepository_Factory implements Factory<GameRepository> {
  private final Provider<PlayerDao> playerDaoProvider;

  private final Provider<GameSessionDao> sessionDaoProvider;

  public GameRepository_Factory(Provider<PlayerDao> playerDaoProvider,
      Provider<GameSessionDao> sessionDaoProvider) {
    this.playerDaoProvider = playerDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
  }

  @Override
  public GameRepository get() {
    return newInstance(playerDaoProvider.get(), sessionDaoProvider.get());
  }

  public static GameRepository_Factory create(Provider<PlayerDao> playerDaoProvider,
      Provider<GameSessionDao> sessionDaoProvider) {
    return new GameRepository_Factory(playerDaoProvider, sessionDaoProvider);
  }

  public static GameRepository newInstance(PlayerDao playerDao, GameSessionDao sessionDao) {
    return new GameRepository(playerDao, sessionDao);
  }
}
