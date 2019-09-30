package net.globulus.easyflavor.processor.codegen;

import java.io.IOException;

import javawriter.EzfWriter;

public interface CodeGen<T> {

  void generateCode(T type, EzfWriter jw) throws IOException;
}
