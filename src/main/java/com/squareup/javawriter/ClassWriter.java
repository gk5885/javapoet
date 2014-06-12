package com.squareup.javawriter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public final class ClassWriter extends TypeWriter {
  private final List<TypeWriter> nestedTypeWriters;
  private final List<FieldWriter> fieldWriters;
  private final List<ConstructorWriter> constructorWriters;
  private final List<MethodWriter> methodWriters;

  ClassWriter(ClassName className) {
    super(className);
    this.nestedTypeWriters = Lists.newArrayList();
    this.fieldWriters = Lists.newArrayList();
    this.constructorWriters = Lists.newArrayList();
    this.methodWriters = Lists.newArrayList();
  }

  FieldWriter addField(Class<?> type, String name) {
    FieldWriter fieldWriter = new FieldWriter(ClassName.fromClass(type), name);
    fieldWriters.add(fieldWriter);
    return fieldWriter;
  }

  ConstructorWriter addConstructor() {
    ConstructorWriter constructorWriter = new ConstructorWriter(name.simpleName());
    constructorWriters.add(constructorWriter);
    return constructorWriter;
  }

  ClassWriter addNestedClass(String name) {
    ClassWriter innerClassWriter = new ClassWriter(this.name.nestedClassNamed(name));
    nestedTypeWriters.add(innerClassWriter);
    return innerClassWriter;
  }

  MethodWriter addMethod(TypeWriter returnType, String name) {
    MethodWriter methodWriter = new MethodWriter(Optional.of(returnType.name), name);
    methodWriters.add(methodWriter);
    return methodWriter;
  }

  MethodWriter addMethod(Class<?> returnType, String name) {
    MethodWriter methodWriter =
        new MethodWriter(Optional.of(ClassName.fromClass(returnType)), name);
    methodWriters.add(methodWriter);
    return methodWriter;
  }

  MethodWriter addVoidMethod(String name) {
    MethodWriter methodWriter = new MethodWriter(Optional.<ClassName>absent(), name);
    methodWriters.add(methodWriter);
    return methodWriter;
  }

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    writeModifiers(appendable).append("class ").append(name.simpleName()).append(" {\n");
    for (VariableWriter fieldWriter : fieldWriters) {
      fieldWriter.write(new IndentingAppendable(appendable), context).append("\n");
    }
    appendable.append('\n');
    for (ConstructorWriter constructorWriter : constructorWriters) {
      constructorWriter.write(new IndentingAppendable(appendable), context);
    }
    appendable.append('\n');
    for (MethodWriter methodWriter : methodWriters) {
      methodWriter.write(new IndentingAppendable(appendable), context);
    }
    appendable.append('\n');
    for (TypeWriter nestedTypeWriter : nestedTypeWriters) {
      nestedTypeWriter.write(new IndentingAppendable(appendable), context);
    }
    appendable.append("}\n");
    return appendable;
  }

  @Override
  public Set<ClassName> referencedClasses() {
    Iterable<? extends HasClassReferences> concat =
        Iterables.concat(nestedTypeWriters, fieldWriters, constructorWriters);
    return FluentIterable.from(concat)
        .transformAndConcat(new Function<HasClassReferences, Set<ClassName>>() {
          @Override
          public Set<ClassName> apply(HasClassReferences input) {
            return input.referencedClasses();
          }
        })
        .toSet();
  }
}
