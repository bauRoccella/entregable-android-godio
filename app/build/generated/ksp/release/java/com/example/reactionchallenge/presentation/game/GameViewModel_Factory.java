package com.example.reactionchallenge.presentation.game;

import com.example.reactionchallenge.data.repository.GameRepository;
import com.example.reactionchallenge.domain.logic.InverseReactionValidator;
import com.example.reactionchallenge.domain.logic.StimulusGenerator;
import com.example.reactionchallenge.util.SoundManager;
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
public final class GameViewModel_Factory implements Factory<GameViewModel> {
  private final Provider<GameRepository> repositoryProvider;

  private final Provider<StimulusGenerator> stimulusGeneratorProvider;

  private final Provider<InverseReactionValidator> validatorProvider;

  private final Provider<SoundManager> soundProvider;

  public GameViewModel_Factory(Provider<GameRepository> repositoryProvider,
      Provider<StimulusGenerator> stimulusGeneratorProvider,
      Provider<InverseReactionValidator> validatorProvider, Provider<SoundManager> soundProvider) {
    this.repositoryProvider = repositoryProvider;
    this.stimulusGeneratorProvider = stimulusGeneratorProvider;
    this.validatorProvider = validatorProvider;
    this.soundProvider = soundProvider;
  }

  @Override
  public GameViewModel get() {
    return newInstance(repositoryProvider.get(), stimulusGeneratorProvider.get(), validatorProvider.get(), soundProvider.get());
  }

  public static GameViewModel_Factory create(Provider<GameRepository> repositoryProvider,
      Provider<StimulusGenerator> stimulusGeneratorProvider,
      Provider<InverseReactionValidator> validatorProvider, Provider<SoundManager> soundProvider) {
    return new GameViewModel_Factory(repositoryProvider, stimulusGeneratorProvider, validatorProvider, soundProvider);
  }

  public static GameViewModel newInstance(GameRepository repository,
      StimulusGenerator stimulusGenerator, InverseReactionValidator validator, SoundManager sound) {
    return new GameViewModel(repository, stimulusGenerator, validator, sound);
  }
}
