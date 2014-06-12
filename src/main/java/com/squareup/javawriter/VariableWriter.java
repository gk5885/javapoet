package com.squareup.javawriter;

import com.google.common.collect.ImmutableSet;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import java.util.Set;


public class VariableWriter extends Modifiable implements Writable, HasClassReferences {
  private final ClassName type;
  private final String name;

  VariableWriter(ClassName type, String name) {
    this.type = type;
    this.name = name;
  }

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    return writeModifiers(appendable).append(context.sourceReferenceForClassName(type)).append(' ').append(name);
  }

  @Override
  public Set<ClassName> referencedClasses() {
    return ImmutableSet.of(type);
  }
}
