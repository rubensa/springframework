/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.comparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A comparator that chains a sequence of one or more more Comparators.
 * <p>
 * A compound comparator calls each Comparator in sequence until a single
 * Comparator returns a non-zero result, or the comparators are exhausted and
 * zero is returned.
 * <p>
 * This facilitates in-memory sorting similar to multi-column sorting in SQL.
 * The order of any single Comparator in the list can also be reversed.
 * 
 * @author Keith Donald
 */
public class CompoundComparator implements Comparator, Serializable {
    private List sortDefinitions;

    /**
     * Construct a CompoundComparator with initially no Comparators. Clients
     * must add at least one Comparator before calling the compare method or an
     * IllegalStateException is thrown.
     */
    public CompoundComparator() {
        this(new ArrayList());
    }

    /**
     * Construct a CompoundComparator from the Comparators in the provided
     * array. All Comparators will default to the forward sort order.
     * 
     * @param list
     *            the list of comparators
     */
    public CompoundComparator(Comparator[] comparators) {
        this(SortDefinition.createSortDefinitionList(comparators));
    }

    /**
     * Construct a CompoundComparator from the SortDefinitions in the provided
     * array.
     * 
     * @param sortDefinitions
     *            the sortDefinition array
     */
    public CompoundComparator(SortDefinition[] sortDefinitions) {
        this(Arrays.asList(sortDefinitions));
    }

    /**
     * Construct a CompoundComparator from the SortDefinitions in the provided
     * list.
     * 
     * @param sortDefinitions
     *            the sortDefinition list
     */
    public CompoundComparator(List sortDefinitions) {
        Assert.notNull(sortDefinitions,
                "At least one sortDefinition is required");
        this.sortDefinitions = sortDefinitions;
    }

    /**
     * Add a Comparator to the end of the chain using forward (ascending) sort
     * order
     * 
     * @param comparator
     *            Comparator with forward sort order
     */
    public void addComparator(Comparator comparator) {
        addComparator(comparator, SortOrder.ASCENDING);
    }

    /**
     * Add a Comparator to the end of the chain using the provided sort order
     * 
     * @param comparator
     *            Comparator to add to the end of the chain
     * @param order
     *            The sort order
     */
    public void addComparator(Comparator comparator, SortOrder order) {
        sortDefinitions.add(new SortDefinition(comparator, order));
    }

    /**
     * Replace the Comparator at the given index, maintaining the existing sort
     * order.
     * 
     * @param index
     *            index of the Comparator to replace
     * @param comparator
     *            Comparator to place at the given index
     */
    public void setComparator(int index, Comparator comparator) {
        setComparator(index, comparator, null);
    }

    /**
     * Replace the Comparator at the given index using the given sort order
     * 
     * @param index
     *            index of the Comparator to replace
     * @param comparator
     *            Comparator to set
     * @param order
     *            the sort order
     */
    public void setComparator(int index, Comparator comparator, SortOrder order) {
        SortDefinition definition = getSortDefinition(index);
        definition.setComparator(comparator);
        if (order != null) {
            definition.setOrder(order);
        }
    }

    private SortDefinition getSortDefinition(int index) {
        return (SortDefinition)sortDefinitions.get(index);
    }

    /**
     * Change the sort order at the given index to forward sort.
     * 
     * @param index
     *            the index into the list of aggregated comparators
     */
    public void setAscendingOrder(int index) {
        getSortDefinition(index).setOrder(SortOrder.ASCENDING);
    }

    /**
     * Change the sort order at the given index to reverse sort.
     * 
     * @param index
     *            the index into the list of aggregated comparators
     */
    public void setDescendingOrder(int index) {
        getSortDefinition(index).setOrder(SortOrder.DESCENDING);
    }

    /**
     * Flip the sort order of each sort definition contained by this compound
     * comparator.
     */
    public void flipOrder() {
        Iterator it = sortDefinitions.iterator();
        while (it.hasNext()) {
            ((SortDefinition)it.next()).flipOrder();
        }
    }

    /**
     * Flip the sort order of the sort definition at the specified index.
     * 
     * @param index
     *            the sort definition index to flip
     */
    public void flipOrder(int index) {
        getSortDefinition(index).flipOrder();
    }

    /**
     * Returns the number of aggregated comparators.
     * 
     * @return The comparator count
     */
    public int size() {
        return sortDefinitions.size();
    }

    public int compare(Object o1, Object o2) {
        Assert
                .state(sortDefinitions.size() > 0,
                        "No sort definitions have been added to this CompoundComparator to compare!");
        Iterator it = sortDefinitions.iterator();
        for (int i = 0; it.hasNext(); i++) {
            SortDefinition def = (SortDefinition)it.next();
            int result = def.compare(o1, o2);
            if (result != 0) { return result; }
        }
        return 0;
    }

    public int hashCode() {
        return sortDefinitions.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o instanceof CompoundComparator) {
            CompoundComparator c = (CompoundComparator)o;
            return sortDefinitions.equals(c.sortDefinitions);
        }
        return false;
    }

    public String toString() {
        return new ToStringCreator(this).append("sortDefinitions",
                sortDefinitions).toString();
    }

}