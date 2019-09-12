package net.globulus.easyflavor.processor.codegen;

import java.io.IOException;

import javawriter.EzfJavaWriter;

public interface CodeGen<T> {

  void generateCode(T type, EzfJavaWriter jw) throws IOException;
}
