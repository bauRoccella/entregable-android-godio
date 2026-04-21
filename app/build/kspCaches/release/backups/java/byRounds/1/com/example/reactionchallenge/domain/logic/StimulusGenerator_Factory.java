package com.example.reactionchallenge.domain.logic;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class StimulusGenerator_Factory implements Factory<StimulusGenerator> {
  @Override
  public StimulusGenerator get() {
    return newInstance();
  }

  public static StimulusGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StimulusGenerator newInstance() {
    return new StimulusGenerator();
  }

  private static final class InstanceHolder {
    private static final StimulusGenerator_Factory INSTANCE = new StimulusGenerator_Factory();
  }
}
