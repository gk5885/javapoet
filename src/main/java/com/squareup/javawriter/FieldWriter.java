package com.squareup.javawriter;

import com.google.common.base.Optional;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import javax.lang.model.element.Modifier;

public class FieldWriter extends VariableWriter {
  private Optional<String> initializer;

  FieldWriter(ClassName type, String name) {
    super(type, name);
    this.initializer = Optional.absent();
  }

  public void setInitializer(String initializer) {
    this.initializer = Optional.of(initializer);
  }

  @Override
  public FieldWriter addModifiers(Modifier first, Modifier... rest) {
    return (FieldWriter) super.addModifiers(first, rest);
  }

  @Override
  public FieldWriter withModifiers(Modifier... modifiers) {
    return (FieldWriter) super.withModifiers(modifiers);
  }

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    super.write(appendable, context);
    if (initializer.isPresent()) {
      appendable.append(" = ").append(initializer.get());
    }
    appendable.append(';');
    return appendable;
  }
}
