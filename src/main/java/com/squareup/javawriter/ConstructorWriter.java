package com.squareup.javawriter;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class ConstructorWriter extends Modifiable implements Writable, HasClassReferences {
  private final String name;
  private final Map<String, VariableWriter> parameterWriters;
  private final BlockWriter blockWriter;

  ConstructorWriter(String name) {
    this.name = name;
    this.parameterWriters = Maps.newLinkedHashMap();
    this.blockWriter = new BlockWriter();
  }

  VariableWriter addParameter(Class<?> type, String name) {
    return addParameter(ClassName.fromClass(type), name);
  }

  VariableWriter addParameter(TypeWriter type, String name) {
    return addParameter(type.name, name);
  }

  BlockWriter blockWriter() {
    return blockWriter;
  }

  private VariableWriter addParameter(ClassName type, String name) {
    checkArgument(!parameterWriters.containsKey(name));
    VariableWriter parameterWriter = new VariableWriter(type, name);
    parameterWriters.put(name, parameterWriter);
    return parameterWriter;
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

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    writeModifiers(appendable).append(name).append('(');
    Iterator<VariableWriter> parameterWritersIterator = parameterWriters.values().iterator();
    if (parameterWritersIterator.hasNext()) {
      parameterWritersIterator.next().write(appendable, context);
    }
    while (parameterWritersIterator.hasNext()) {
      appendable.append(", ");
      parameterWritersIterator.next().write(appendable, context);
    }
    appendable.append(") {");
    blockWriter.write(new IndentingAppendable(appendable), context);
    return appendable.append("}\n");
  }
}
