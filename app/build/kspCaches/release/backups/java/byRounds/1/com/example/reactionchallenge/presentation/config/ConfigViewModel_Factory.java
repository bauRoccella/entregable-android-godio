package com.example.reactionchallenge.presentation.config;

import com.example.reactionchallenge.data.repository.GameRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ConfigViewModel_Factory implements Factory<ConfigViewModel> {
  private final Provider<GameRepository> repositoryProvider;

  public ConfigViewModel_Factory(Provider<GameRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ConfigViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ConfigViewModel_Factory create(Provider<GameRepository> repositoryProvider) {
    return new ConfigViewModel_Factory(repositoryProvider);
  }

  public static ConfigViewModel newInstance(GameRepository repository) {
    return new ConfigViewModel(repository);
  }
}
