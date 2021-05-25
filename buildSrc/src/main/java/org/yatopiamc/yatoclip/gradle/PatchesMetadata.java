package org.yatopiamc.yatoclip.gradle;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PatchesMetadata {

    public final Set<PatchMetadata> patches;
    public final Set<String> copyExcludes;
    public final Map<String, String> relocationMapping;
    public final Map<String, String> relocationInvertedMapping;

    public PatchesMetadata(Set<PatchMetadata> patches, Set<String> copyExcludes, Map<String, String> relocationMapping, Map<String, String> relocationInvertedMapping) {
        Objects.requireNonNull(copyExcludes);
        this.copyExcludes = Collections.unmodifiableSet(copyExcludes);
        Objects.requireNonNull(patches);
        this.patches = Collections.unmodifiableSet(patches);
        Objects.requireNonNull(relocationMapping);
        this.relocationMapping = relocationMapping;
        Objects.requireNonNull(relocationInvertedMapping);
        this.relocationInvertedMapping = relocationInvertedMapping;
    }

    public static class PatchMetadata {
        public final String originalName;
        public final String targetName;
        public final String originalHash;
        public final String targetHash;
        public final String patchHash;

        public PatchMetadata(String originalName, String targetName, String originalHash, String targetHash, String patchHash) {
            this.originalName = originalName;
            this.targetName = targetName;
            this.originalHash = originalHash;
            this.targetHash = targetHash;
            this.patchHash = patchHash;
        }
    }
}
