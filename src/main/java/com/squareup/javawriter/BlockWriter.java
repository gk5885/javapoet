package com.squareup.javawriter;

import com.google.common.collect.Lists;
import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;
import java.util.List;

public final class BlockWriter implements Writable {
  private final List<String> snippets;

  BlockWriter() {
    this.snippets = Lists.newArrayList();
  }

  public BlockWriter addSnippet(String snippet, Object... args) {
    snippets.add(String.format(snippet, args));
    return this;
  }

  @Override
  public Appendable write(Appendable appendable, CompilationUnitContext context)
      throws IOException {
    for (String snippet : snippets) {
      appendable.append('\n').append(context.compressTypesWithin(snippet));
    }
    return appendable.append('\n');
  }
}
