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


import java.lang.reflect.Method;

import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.1
 *
 */
public abstract class AdditionSubtractionExpression extends BinaryOperationExpression {

    private static final long serialVersionUID = -7977102096580376925L;
    
    
    protected static final String ADDITION_OPERATOR = "+";
    protected static final String SUBTRACTION_OPERATOR = "-";

    static final String[] OPERATORS = new String[] { ADDITION_OPERATOR, SUBTRACTION_OPERATOR };
    private static final boolean[] LENIENCIES = new boolean[] { false, true };

    @SuppressWarnings("unchecked")
    private static final Class<? extends BinaryOperationExpression>[] OPERATOR_CLASSES =
            (Class<? extends BinaryOperationExpression>[]) new Class<?>[] {
                    AdditionExpression.class, SubtractionExpression.class };

    private static Method LEFT_ALLOWED_METHOD;
    private static Method RIGHT_ALLOWED_METHOD;


    static {
        try {
            LEFT_ALLOWED_METHOD = AdditionSubtractionExpression.class.getDeclaredMethod("isLeftAllowed", Expression.class);
            RIGHT_ALLOWED_METHOD = AdditionSubtractionExpression.class.getDeclaredMethod("isRightAllowed", Expression.class);
        } catch (final NoSuchMethodException e) {
            throw new TemplateProcessingException("Cannot register is*Allowed methods in binary operation expression", e);
        }
    }



    protected AdditionSubtractionExpression(final Expression left, final Expression right) {
        super(left, right);
    }



    static boolean isRightAllowed(final Expression right) {
        return right != null && !(right instanceof Token && !(right instanceof NumberTokenExpression));
    }

    static boolean isLeftAllowed(final Expression left) {
        return left != null && !(left instanceof Token && !(left instanceof NumberTokenExpression));
    }



    static ExpressionParsingState composeAdditionSubtractionExpression(
            final ExpressionParsingState state, int nodeIndex) {
        return composeBinaryOperationExpression(
                state, nodeIndex, OPERATORS, LENIENCIES, OPERATOR_CLASSES, LEFT_ALLOWED_METHOD, RIGHT_ALLOWED_METHOD);
    }
    

    
}
