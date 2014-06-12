package com.squareup.javawriter;

import com.squareup.javawriter.JavaWriter.CompilationUnitContext;
import java.io.IOException;

interface Writable {
  Appendable write(Appendable appendable, CompilationUnitContext context) throws IOException;
}
