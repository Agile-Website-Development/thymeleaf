/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.standard.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;



/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.1
 *
 */
public final class ExpressionSequence implements Iterable<Expression>, Serializable {

    private static final long serialVersionUID = -6069208208568731809L;
    

    private static final char OPERATOR = ',';
    // Future proof, just in case in the future we add other tokens as operators
    static final String[] OPERATORS = new String[] {String.valueOf(OPERATOR)};

    private final List<Expression> expressions;
         
    public ExpressionSequence(final List<? extends Expression> expressions) {
        super();
        Validate.notNull(expressions, "Expression list cannot be null");
        Validate.containsNoNulls(expressions, "Expression list cannot contain any nulls");
        this.expressions = Collections.unmodifiableList(expressions);
    }

    
    public List<Expression> getExpressions() {
        return this.expressions;
    }
  
    public int size() {
        return this.expressions.size();
    }
    
    public Iterator<Expression> iterator() {
        return this.expressions.iterator();
    }

    public String getStringRepresentation() {
        final StringBuilder sb = new StringBuilder();
        if (this.expressions.size() > 0) {
            sb.append(this.expressions.get(0));
            for (int i = 1; i < this.expressions.size(); i++) {
                sb.append(OPERATOR);
                sb.append(this.expressions.get(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }
    
    
    
    

    
    static ExpressionSequence parse(final String input) {

        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        final ExpressionParsingState decomposition =
                ExpressionParsingUtil.decompose(input,ExpressionParsingDecompositionConfig.DECOMPOSE_ALL_AND_UNNEST);

        if (decomposition == null) {
            return null;
        }

        return composeSequence(decomposition, 0);

    }
        
    


    private static ExpressionSequence composeSequence(final ExpressionParsingState state, final int nodeIndex) {

        if (state == null || nodeIndex >= state.size()) {
            return null;
        }

        if (state.hasExpressionAt(nodeIndex)) {
            // could happen if we are traversing pointers recursively, so we will consider an expression sequence
            // with one expression only
            final List<Expression> expressions = new ArrayList<Expression>(2);
            expressions.add(state.get(nodeIndex).getExpression());
            return new ExpressionSequence(expressions);
        }

        final String input = state.get(nodeIndex).getInput();

        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        // First, check whether we are just dealing with a pointer input
        int pointer = ExpressionParsingUtil.parseAsSimpleIndexPlaceholder(input);
        if (pointer != -1) {
            return composeSequence(state, pointer);
        }

        final String[] inputParts = StringUtils.split(input, ",");

        final List<Expression> expressions = new ArrayList<Expression>(4);
        for (final String inputPart : inputParts) {
            final Expression expression = ExpressionParsingUtil.parseAndCompose(state, inputPart);
            if (expression == null) {
                return null;
            }
            expressions.add(expression);
        }

        return new ExpressionSequence(expressions);

    }




    public static void main(String[] args) {

        System.out.println(parse("'one',${two},'three'"));

    }
    
    
}
