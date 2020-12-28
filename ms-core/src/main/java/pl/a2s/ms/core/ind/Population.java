/*
 * Copyright 2021 A2S
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.a2s.ms.core.ind;

import lombok.Getter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Population extends AbstractCollection<RankedIndividual> {

    @Getter private final int size;
    @Getter private final Individual[] individuals;
    @Getter private final int[] ranks;

    public Population(int size) {
        this.size = size;
        individuals = new Individual[size];
        ranks = new int[size];
    }

    public Population(Collection<? extends Individual> c) {
        this(c.size());
        setAll(c);
    }

    public Population(Individual... individuals) {
        this(individuals.length);
        setAll(individuals);
    }

    /**
     * Rank-preserving pseudo-constructor (a kind of copy constructor).
     *
     * @param c a collection of {@link RankedIndividual} objects.
     * @param simplify if true individuals contained in individuals from c
     * will be converted into {@link SimpleIndividual}s
     * @return Newly created {@link Population} inheriting ranks from c.
     */
    public static Population of(Collection<RankedIndividual> c, boolean simplify) {
        final int size = c.size();
        final Individual[] inds = new Individual[size];
        final int[] ranks = new int[size];
        int i = 0;
        for (final RankedIndividual ri : c) {
            if (simplify) {
                inds[i] = new SimpleIndividual(ri.getPoint());
                inds[i].setObjectives(ri.getObjectives());
            } else {
                inds[i] = ri.getIndividual();
            }
            ranks[i] = ri.getRank();
            i++;
        }
        final Population pop = new Population(inds);
        Arrays.setAll(pop.ranks, j -> ranks[j]);
        return pop;
    }

    /**
     * Equivalent to of(c, false).
     * @see #of(Collection, boolean)
     * @param c
     * @return
     */
    public static Population of(Collection<RankedIndividual> c) {
        return of(c, false);
    }

    /**
     * Rank-preserving pseudo-constructor (a kind of copy constructor).
     * Calls {@link #of(Collection)}, so does not perform simplification.
     *
     * @param ris an array of {@link RankedIndividual} objects.
     * @return Newly created {@link Population} inheriting ranks from c.
     */
    public static Population of(RankedIndividual... ris) {
        return of(Arrays.asList(ris));
    }

    /**
     * A short-cut for filtering populations.
     *
     * @param predicate a predicate (typically involving ranks)
     * @return Newly created {@link Population} containing individuals satisfying given predicate
     */
    public Population select(Predicate<? super RankedIndividual> predicate) {
        return of(stream().filter(predicate).collect(Collectors.toList()));
    }

    public int[] updateRanks() {
        // domination counts
        final int[] n = new int[size];
        // domination sets
        final List<Set<Integer>> S = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            S.add(new HashSet<>());
        }
        for (int i = 0; i < size; i++) {
            for (int j = i+1; j < size; j++) {
                if (individuals[i].dominates(individuals[j])) {
                    n[j]++;
                    S.get(i).add(j);
                }
                if (individuals[j].dominates(individuals[i])) {
                    n[i]++;
                    S.get(j).add(i);
                }
            }
        }
        List<Integer> Q = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (n[i] == 0) {
                Q.add(i);
            }
        }
        int rank = 0;
        while (!Q.isEmpty()) {
            final List<Integer> prevQ = Q;
            Q = new ArrayList<>();
            for (final int i: prevQ) {
                ranks[i] = rank;
                for (final int j: S.get(i)) {
                    n[j]--;
                    if (n[j] == 0) {
                        Q.add(j);
                    }
                }
            }
            rank++;
        }
        return ranks;
    }

    /**
     * Sets the individual at position pos to ind. If ind is {@link RankedIndividual},
     * its rank gets dropped.
     *
     * @param pos where to put ind
     * @param ind what to put at pos
     */
    public void setAt(int pos, Individual ind) {
        if (ind instanceof RankedIndividual) {
            ind = ((RankedIndividual) ind).getIndividual();
        }
        individuals[pos] = ind;
    }

    /**
     * Sets the individuals starting from first to the elements of a collection.
     * If elements of c are RankedIndividuals, this information is dropped.
     * @param c
     */
    public void setAll(Collection<? extends Individual> c) {
        final Iterator<? extends Individual> iter = c.iterator();
        for (int i = 0; i < size && iter.hasNext(); i++) {
            setAt(i, iter.next());
        }
    }

    /**
     * Sets the individuals starting from first to the elements of a collection.
     * If elements of c are RankedIndividuals, this information is dropped.
     * @param individuals
     */
    public void setAll(Individual... individuals) {
        for (int i = 0; i < size && i < individuals.length; i++) {
            setAt(i, individuals[i]);
        }
    }

    @Override
    public int size() {
        return getSize();
    }

    @Override
    public Iterator<RankedIndividual> iterator() {
        return new Iterator<>() {
            private int next = 0;

            @Override
            public boolean hasNext() {
                return next < size;
            }

            @Override
            public RankedIndividual next() {
                final RankedIndividual ri = new RankedIndividual(individuals[next], ranks[next]);
                next++;
                return ri;
            }
        };
    }

}
