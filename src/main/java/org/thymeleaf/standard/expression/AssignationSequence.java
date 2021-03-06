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
public final class AssignationSequence implements Iterable<Assignation>, Serializable {

    
    private static final long serialVersionUID = -4915282307441011014L;


    private static final char OPERATOR = ',';
    // Future proof, just in case in the future we add other tokens as operators
    static final String[] OPERATORS = new String[] {String.valueOf(OPERATOR)};

    
    private final List<Assignation> assignations;
    
    
    
    AssignationSequence(final List<Assignation> assignations) {
        super();
        Validate.notNull(assignations, "Assignation list cannot be null");
        Validate.containsNoNulls(assignations, "Assignation list cannot contain any nulls");
        this.assignations = Collections.unmodifiableList(assignations);
    }

    
    public List<Assignation> getAssignations() {
        return this.assignations;
    }
  
    public int size() {
        return this.assignations.size();
    }
    
    public Iterator<Assignation> iterator() {
        return this.assignations.iterator();
    }

    
    public String getStringRepresentation() {
        final StringBuilder sb = new StringBuilder();
        if (this.assignations.size() > 0) {
            sb.append(this.assignations.get(0));
            for (int i = 1; i < this.assignations.size(); i++) {
                sb.append(OPERATOR);
                sb.append(this.assignations.get(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }

    
    
    
    
    static AssignationSequence parse(final String input, final boolean allowParametersWithoutValue) {
        
        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        final ExpressionParsingState decomposition =
            ExpressionParsingUtil.decompose(input,ExpressionParsingDecompositionConfig.DECOMPOSE_ALL_AND_UNNEST);

        if (decomposition == null) {
            return null;
        }

        return composeSequence(decomposition, 0, allowParametersWithoutValue);

    }
    
    
    
    
    private static AssignationSequence composeSequence(
            final ExpressionParsingState state, final int nodeIndex, final boolean allowParametersWithoutValue) {

        if (state == null || nodeIndex >= state.size()) {
            return null;
        }

        if (state.hasExpressionAt(nodeIndex)) {
            if (!allowParametersWithoutValue) {
                return null;
            }
            // could happen if we are traversing pointers recursively, so we will consider it a sequence containing
            // only one, no-value assignation (though we will let the Assignation.compose(...) method do the job.
            final Assignation assignation =
                    Assignation.composeAssignation(state, nodeIndex, allowParametersWithoutValue);
            if (assignation == null) {
                return null;
            }
            final List<Assignation> assignations = new ArrayList<Assignation>(2);
            assignations.add(assignation);
            return new AssignationSequence(assignations);
        }

        final String input = state.get(nodeIndex).getInput();

        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        // First, check whether we are just dealing with a pointer input
        int pointer = ExpressionParsingUtil.parseAsSimpleIndexPlaceholder(input);
        if (pointer != -1) {
            return composeSequence(state, pointer, allowParametersWithoutValue);
        }

        final String[] inputParts = StringUtils.split(input, ",");

        for (final String inputPart : inputParts) {
            // We create new String parsing nodes for all of the elements
            // We add all nodes first so that we know the exact indexes in which they are
            // (composing assignations here can modify the size of the state object without we noticing)
            state.addNode(inputPart.trim());
        }

        final List<Assignation> assignations = new ArrayList<Assignation>(4);
        final int startIndex = state.size() - inputParts.length;
        final int endIndex = state.size();
        for (int i = startIndex; i < endIndex; i++) {
            final Assignation assignation =
                    Assignation.composeAssignation(state, i, allowParametersWithoutValue);
            if (assignation == null) {
                return null;
            }
            assignations.add(assignation);
        }

        return new AssignationSequence(assignations);

    }
    
    
}

