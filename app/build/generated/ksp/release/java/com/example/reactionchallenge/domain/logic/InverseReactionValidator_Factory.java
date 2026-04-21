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
public final class InverseReactionValidator_Factory implements Factory<InverseReactionValidator> {
  @Override
  public InverseReactionValidator get() {
    return newInstance();
  }

  public static InverseReactionValidator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InverseReactionValidator newInstance() {
    return new InverseReactionValidator();
  }

  private static final class InstanceHolder {
    private static final InverseReactionValidator_Factory INSTANCE = new InverseReactionValidator_Factory();
  }
}
