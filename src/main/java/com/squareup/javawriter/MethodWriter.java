package com.squareup.javawriter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;


public class MethodWriter extends Modifiable implements HasClassReferences, Writable {
  private final Optional<ClassName> returnType;
  private final String name;
  private final Map<String, VariableWriter> parameterWriters;
  private Optional<BlockWriter> body;

  MethodWriter(Optional<ClassName> returnType, String name) {
    this.returnType = returnType;
    this.name = name;
    this.parameterWriters = Maps.newLinkedHashMap();
    this.body = Optional.absent();
  }

  VariableWriter addParameter(Class<?> type, String name) {
    return addParameter(ClassName.fromClass(type), name);
  }

  VariableWriter addParameter(TypeWriter type, String name) {
    return addParameter(type.name, name);
  }

  private VariableWriter addParameter(ClassName type, String name) {
    checkArgument(!parameterWriters.containsKey(name));
    VariableWriter parameterWriter = new VariableWriter(type, name);
    parameterWriters.put(name, parameterWriter);
    return parameterWriter;
  }

  BlockWriter body() {
    BlockWriter blockWriter = new BlockWriter();
    body = Optional.of(blockWriter);
    return blockWriter;
  }

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    String returnString = returnType.isPresent()
        ? context.sourceReferenceForClassName(returnType.get())
        : "void";
    writeModifiers(appendable).append(returnString).append(' ').append(name).append('(');
    Iterator<VariableWriter> parameterWritersIterator = parameterWriters.values().iterator();
    if (parameterWritersIterator.hasNext()) {
      parameterWritersIterator.next().write(appendable, context);
    }
    while (parameterWritersIterator.hasNext()) {
      appendable.append(", ");
      parameterWritersIterator.next().write(appendable, context);
    }
    appendable.append(")");
    if (body.isPresent()) {
      appendable.append(" {");
      body.get().write(new IndentingAppendable(appendable), context);
      appendable.append("}\n");
    } else {
      appendable.append(";\n");
    }
    return appendable;
  }

  @Override
  public Set<ClassName> referencedClasses() {
    return FluentIterable.from(parameterWriters.values())
        .transformAndConcat(new Function<HasClassReferences, Set<ClassName>>() {
          @Override
          public Set<ClassName> apply(HasClassReferences input) {
            return input.referencedClasses();
          }
        })
        .toSet();
  }
}
