package net.yatopia.hwaccel.utils.registry;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class Identifier {

    private final String namespace;
    private final String path;

    public Identifier(String namespace, String path) {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(path);
        Preconditions.checkArgument(!namespace.trim().isBlank());
        Preconditions.checkArgument(!path.trim().isBlank());
        Preconditions.checkArgument(isNamespaceValid(namespace));
        Preconditions.checkArgument(isPathValid(path));
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public static Identifier tryParse(String str) {
        String[] strings = str.trim().split(":");
        Preconditions.checkArgument(strings.length == 2);
        return new Identifier(strings[0], strings[1]);
    }

    static boolean isPathValid(String path) {
        for(int i = 0; i < path.length(); ++i)
            if (!isPathCharacterValid(path.charAt(i)))
                return false;

        return true;
    }

    static boolean isNamespaceValid(String namespace) {
        for(int i = 0; i < namespace.length(); ++i)
            if (!isNamespaceCharacterValid(namespace.charAt(i)))
                return false;

        return true;
    }

    public static boolean isPathCharacterValid(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
    }

    private static boolean isNamespaceCharacterValid(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return namespace.equals(that.namespace) &&
                path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
