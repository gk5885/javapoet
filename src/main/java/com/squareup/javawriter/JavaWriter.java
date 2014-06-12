package com.squareup.javawriter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;

public final class JavaWriter {
  public static JavaWriter inPackage(String packageName) {
    return new JavaWriter(packageName);
  }

  public static JavaWriter inPackage(Package enclosingPackage) {
    return new JavaWriter(enclosingPackage.getName());
  }

  public static JavaWriter inPackage(PackageElement packageElement) {
    return new JavaWriter(packageElement.getQualifiedName().toString());
  }

  private final String packageName;
  private final List<TypeWriter> typeWriters;
  private final List<ClassName> explicitImports;

  private JavaWriter(String packageName) {
    this.packageName = packageName;
    this.typeWriters = Lists.newArrayList();
    this.explicitImports = Lists.newArrayList();
  }

  public JavaWriter addImport(Class<?> importedClass) {
    explicitImports.add(ClassName.fromClass(importedClass));
    return this;
  }

  public ClassWriter addClass(String simpleName) {
    return addClass(ImmutableSet.<Modifier>of(), simpleName, Optional.<Class<?>>absent(),
        ImmutableSet.<Class<?>>of());
  }

  private ClassWriter addClass(Set<Modifier> modifiers, String simpleName,
      Optional<Class<?>> extending, Set<Class<?>> implementing) {
    checkNotNull(modifiers);
    checkNotNull(simpleName);
    checkArgument(!modifiers.contains(PROTECTED));
    checkArgument(!modifiers.contains(PRIVATE));
    checkArgument(!modifiers.contains(STATIC));
    checkNotNull(extending);
    checkNotNull(implementing);
    ClassWriter classWriter = new ClassWriter(ClassName.create(packageName, simpleName));
    typeWriters.add(classWriter);
    return classWriter;
  }

  static ImmutableSet<ClassName> collectReferencedClasses(
      Iterable<? extends HasClassReferences> iterable) {
    return FluentIterable.from(iterable)
        .transformAndConcat(new Function<HasClassReferences, Set<ClassName>>() {
          @Override
          public Set<ClassName> apply(HasClassReferences input) {
            return input.referencedClasses();
          }
        })
        .toSet();
  }

  public Appendable write(Appendable appendable) throws IOException {
    appendable.append("package ").append(packageName).append(';').append("\n\n");

    // write imports
    ImmutableSet<ClassName> classNames = FluentIterable.from(typeWriters)
        .transformAndConcat(new Function<HasClassReferences, Set<ClassName>>() {
          @Override
          public Set<ClassName> apply(HasClassReferences input) {
            return input.referencedClasses();
          }
        })
        .toSet();
    BiMap<String, ClassName> importedClassIndex = HashBiMap.create();
    // TODO(gak): check for collisions with types declared in this compilation unit too
    for (ClassName className : Iterables.concat(classNames, explicitImports)) {
      if (!className.packageName().equals(packageName)
          && !(className.packageName().equals("java.lang")
              && className.enclosingSimpleNames().isEmpty())) {
        Optional<ClassName> importCandidate = Optional.of(className);
        while (importCandidate.isPresent()
            && importedClassIndex.containsKey(importCandidate.get().simpleName())) {
          importCandidate = importCandidate.get().enclosingClassName();
        }
        if (importCandidate.isPresent()) {
          appendable.append("import ").append(className.canonicalName()).append('\n');
          importedClassIndex.put(className.simpleName(), className);
        }
      }
    }

    appendable.append('\n');

    CompilationUnitContext context =
        new CompilationUnitContext(packageName, ImmutableSet.copyOf(importedClassIndex.values()));

    // write types
    for (TypeWriter typeWriter : typeWriters) {
      typeWriter.write(appendable, context).append('\n');
    }
    return appendable;
  }

  public void file(Filer filer, Element originatingElements) throws IOException {
    JavaFileObject sourceFile = filer.createSourceFile(
        Iterables.getOnlyElement(typeWriters).name.canonicalName(), originatingElements);
    Writer writer = sourceFile.openWriter();
    write(writer);
    writer.close();
  }

  @Override
  public String toString() {
    try {
      return write(new StringBuilder()).toString();
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  static final class CompilationUnitContext {
    private final String packageName;
    private final ImmutableSortedSet<ClassName> importedClasses;

    CompilationUnitContext(String packageName, ImmutableSet<ClassName> importedClasses) {
      this.packageName = packageName;
      this.importedClasses =
          ImmutableSortedSet.copyOf(Ordering.natural().reverse(), importedClasses);
    }

    String sourceReferenceForClassName(ClassName className) {
      if (isImported(className)) {
        return className.simpleName();
      }
      Optional<ClassName> enclosingClassName = className.enclosingClassName();
      while (enclosingClassName.isPresent()) {
        if (isImported(enclosingClassName.get())) {
          return className.canonicalName()
              .substring(enclosingClassName.get().canonicalName().length() + 1);
        }
        enclosingClassName = enclosingClassName.get().enclosingClassName();
      }
      return className.canonicalName();
    }

    private boolean isImported(ClassName className) {
      return packageName.equals(className.packageName()) // need to account for scope
          || importedClasses.contains(className)
          || (className.packageName().equals("java.lang")
              && className.enclosingSimpleNames().isEmpty());
    }

    String compressTypesWithin(String snippet) {
      // TODO(gak): deal with string literals
      for (ClassName importedClass : importedClasses) {
        snippet = snippet.replace(importedClass.canonicalName(), importedClass.simpleName());
      }
      return snippet;
    }
  }
}
