package mapper;

import org.cadixdev.lorenz.io.srg.SrgReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.mercury.remapper.MercuryRemapper;
import org.cadixdev.mercury.Mercury;

public class Main {
    public static void main(String[] args) throws Exception {
        Path mappingsPath = Paths.get(args[0]);
        Path src = Paths.get(args[1]);
        Path dist = Paths.get(args[2]);
        Files.deleteIfExists(dist);
        MappingSet mappings;
        {
            BufferedReader reader = Files.newBufferedReader(mappingsPath);
            mappings = new SrgReader(reader).read();
        }
        Mercury m = new Mercury();
        m.getProcessors().add(MercuryRemapper.create(mappings));
        m.rewrite(src, dist);
    }
}
