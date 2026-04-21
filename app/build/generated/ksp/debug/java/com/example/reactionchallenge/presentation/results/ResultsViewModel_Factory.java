package com.example.reactionchallenge.presentation.results;

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
public final class ResultsViewModel_Factory implements Factory<ResultsViewModel> {
  private final Provider<GameRepository> repositoryProvider;

  public ResultsViewModel_Factory(Provider<GameRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ResultsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ResultsViewModel_Factory create(Provider<GameRepository> repositoryProvider) {
    return new ResultsViewModel_Factory(repositoryProvider);
  }

  public static ResultsViewModel newInstance(GameRepository repository) {
    return new ResultsViewModel(repository);
  }
}
