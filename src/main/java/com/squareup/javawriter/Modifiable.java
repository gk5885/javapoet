package com.squareup.javawriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Modifier;

public abstract class Modifiable {
  final Set<Modifier> modifiers;

  Modifiable() {
    this.modifiers = EnumSet.noneOf(Modifier.class);
  }

  public Modifiable addModifiers(Modifier first, Modifier... rest) {
    this.modifiers.addAll(Lists.asList(first, rest));
    return this;
  }

  public Modifiable setModifiers(Iterable<Modifier> modifiers) {
    this.modifiers.clear();
    Iterables.addAll(this.modifiers, modifiers);
    return this;
  }

  public Modifiable withModifiers(Modifier... modifiers) {
    this.modifiers.clear();
    this.modifiers.addAll(Arrays.asList(modifiers));
    return this;
  }

  public Modifiable removeModifiers(Modifier first, Modifier... rest) {
    this.modifiers.removeAll(Lists.asList(first, rest));
    return this;
  }

  public void annotate(Annotation annotation) {

  }

  public void annotate(Class<? extends Annotation> annotation) {

  }

  public void annotate(Class<? extends Annotation> annotation, Object value) {

  }

  public void removeAnnoation(Class<? extends Annotation> annotation) {

  }

  Appendable writeModifiers(Appendable appendable) throws IOException {
    for (Modifier modifier : modifiers) {
      appendable.append(modifier.toString()).append(' ');
    }
    return appendable;
  }
}
