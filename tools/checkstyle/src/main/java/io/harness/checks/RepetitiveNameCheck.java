/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class RepetitiveNameCheck extends AbstractCheck {
  private static final String MSG_KEY = "readability.repetitive.name";

  @Override
  public int[] getDefaultTokens() {
    return new int[] {
        TokenTypes.DOT,
    };
  }

  @Override
  public int[] getRequiredTokens() {
    return getDefaultTokens();
  }

  @Override
  public int[] getAcceptableTokens() {
    return getDefaultTokens();
  }

  @Override
  public void visitToken(DetailAST ast) {
    if (ast.getType() != TokenTypes.DOT) {
      return;
    }

    final DetailAST outer = ast.getFirstChild();
    if (outer.getType() != TokenTypes.IDENT) {
      return;
    }

    if (!Character.isUpperCase(outer.getText().charAt(0))) {
      return;
    }

    final DetailAST inner = outer.getNextSibling();
    if (inner.getType() != TokenTypes.IDENT) {
      return;
    }

    DetailAST parent = ast.getParent();
    while (parent != null) {
      if (parent.getType() == TokenTypes.IMPORT) {
        return;
      }
      if (parent.getType() == TokenTypes.STATIC_IMPORT) {
        return;
      }

      parent = parent.getParent();
    }

    if (inner.getText().toLowerCase().startsWith(outer.getText().toLowerCase())) {
      log(inner, MSG_KEY);
    }
  }
}
