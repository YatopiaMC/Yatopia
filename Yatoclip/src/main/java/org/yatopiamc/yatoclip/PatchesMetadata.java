package org.yatopiamc.yatoclip;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PatchesMetadata {

    public final Set<PatchMetadata> patches;
    public final Set<Relocation> relocations;
    public final Set<String> copyExcludes;

    public PatchesMetadata(Set<PatchMetadata> patches, Set<Relocation> relocations, Set<String> copyExcludes) {
        Objects.requireNonNull(copyExcludes);
        this.copyExcludes = Collections.unmodifiableSet(copyExcludes);
        Objects.requireNonNull(relocations);
        this.relocations = Collections.unmodifiableSet(relocations);
        Objects.requireNonNull(patches);
        this.patches = Collections.unmodifiableSet(patches);
    }

    public static class PatchMetadata {
        public final String name;
        public final String originalHash;
        public final String targetHash;
        public final String patchHash;

        public PatchMetadata(String name, String originalHash, String targetHash, String patchHash) {
            this.name = name;
            this.originalHash = originalHash;
            this.targetHash = targetHash;
            this.patchHash = patchHash;
        }
    }

    public static class Relocation implements Serializable {

        public final String from;
        public final String to;
        public final boolean includeSubPackages;

        public Relocation(String from, String to, boolean includeSubPackages) {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
            this.from = from.replaceAll("\\.", "/");
            this.to = to.replaceAll("\\.", "/");
            this.includeSubPackages = includeSubPackages;
        }
    }
}
