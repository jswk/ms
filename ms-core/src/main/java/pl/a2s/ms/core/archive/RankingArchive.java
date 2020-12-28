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

package pl.a2s.ms.core.archive;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import pl.a2s.ms.core.ind.*;
import pl.a2s.ms.core.orch.hgs.Deme;

import java.util.*;
import java.util.stream.Collectors;

@ToString
public class RankingArchive implements Archive {

    @Getter @Setter private int maxRank;
    @Getter private final boolean enabled;
    private Population population = new Population(0);

    public RankingArchive(boolean enabled, int maxRank) {
        this.enabled = enabled;
        this.maxRank = maxRank;
    }

    /**
     * This implementation remembers the origin.
     */
    @Override
    public void addAllFrom(Deme origin) {
        final Individual[] incoming = origin.getPopulation().getIndividuals();
        if (ArrayUtils.isEmpty(incoming)) {
            return;
        }
        final List<Individual> inds = new ArrayList<>(Arrays.asList(population.getIndividuals()));
        inds.addAll(
                Arrays.stream(incoming)
                .map(ind -> coerce(ind, origin))
                .collect(Collectors.toList())
                );
        population = new Population(inds);
        population.updateRanks();
        if (maxRank >= 0 && maxRank < Integer.MAX_VALUE) {
            population = population.select(ri -> ri.getRank() <= maxRank);
        }
    }

    private static LocatedIndividual coerce(Individual ind, Deme origin) {
        LocatedIndividual coerced;
        if (ind instanceof ContainerIndividual) {
            final Individual sind = new SimpleIndividual(ind.getPoint());
            sind.setObjectives(ind.getObjectives());
            coerced = new LocatedIndividual(sind, origin);
        }
        else {
            coerced = new LocatedIndividual(ind, origin);
        }
        return coerced;
    }

    @Override
    public void addAll(Collection<? extends Individual> c) {
        final Deme nullDeme = Deme.builder()
                .name(NULL_DEME_NAME)
                .population(new Population(c))
                .build();
        addAllFrom(nullDeme);
    }

    @Override
    public void addAll(Individual... individuals) {
        addAll(Arrays.asList(individuals));
    }

    @Override
    public void add(Individual individual) {
        addAll(individual);
    }

    /**
     * Returns individuals from this archive.
     * This implementation removes the information about the localization
     * from the individuals (intentionally).
     */
    @Override
    public List<Individual> getIndividuals() {
        return Arrays.stream(population.getIndividuals())
                .map(RankingArchive::evict)
                .collect(Collectors.toList());
    }

    /**
     * Returns {@link Population} of individuals from this archive.
     * This implementation removes the information about the localization
     * from the individuals (intentionally).
     */
    public Population getPopulation() {
        return Population.of(population, true);
    }

    /**
     * The only accessor that returns the individuals along with the information
     * about the localization.
     * <strong>Use with caution!</strong> Probably will get removed.
     * @return list of individuals from this archive
     */
    public List<LocatedIndividual> getLocatedIndividuals() {
        return Arrays.stream(population.getIndividuals())
                .map(i -> (LocatedIndividual) i)
                .collect(Collectors.toList());
    }

    private static Individual evict(Individual ind) {
        if (!(ind instanceof LocatedIndividual)) {
            return ind;
        }
        final LocatedIndividual lind = (LocatedIndividual) ind;
        return lind.getIndividual();
    }

    /**
     * Performs pre-clustering of this archive grouping the individuals
     * with respect to the demes of origin. This operation preserves
     * the ranks of individuals: to obtain deme-respective ranks
     * one should run {@link Population#updateRanks()} on the
     * returned populations.
     */
    @Override
    public Map<String, Population> splitAlongDemes() {
        final Map<Object, List<RankedIndividual>> pops = population.stream()
            .collect(Collectors.groupingBy(
                    ri -> ((LocatedIndividual) ri.getIndividual()).getLocationName())
                    );
        final Map<String, Population> split = new HashMap<>();
        for (final Object key: pops.keySet()) {
            split.put(key.toString(), Population.of(pops.get(key), true));
        }
        return split;
    }

    @Override
    public int getActualMaxRank() {
        return population.getSize() == 0 ? 0 :
            Arrays.stream(population.getRanks()).max().getAsInt();
    }

    public static RankingArchive setupSampleArchive(int maxRank) {
        final double[][][] indSetup = {
                {{1.0, 1.0}, {0.0, 1.0}},
                {{1.1, 1.1}, {0.1, 1.0}},
                {{1.2, 1.2}, {0.0, 1.1}},
                {{1.3, 1.3}, {0.2, 1.2}},
                {{1.4, 1.4}, {10.0, 4.5}},
                {{1.5, 1.5}, {11.1, 0.0}},
        };
        final Individual[] inds = new Individual[indSetup.length];
        for (int i = 0; i < inds.length; i++) {
            inds[i] = new SimpleIndividual(indSetup[i][0]);
            inds[i].setObjectives(indSetup[i][1]);
        }
        final Deme pcim = Deme.builder().name("Pcim").build();
        pcim.setPopulation(new Population(inds[1], inds[4]));
        final Deme sworne = Deme.builder().name("Swornegacie").build();
        sworne.setPopulation(new Population(inds[2], inds[3], inds[5]));
        final RankingArchive ra = new RankingArchive(true, maxRank);
        ra.add(inds[0]);
        ra.addAllFrom(pcim);
        ra.addAllFrom(sworne);
        return ra;
    }

}
