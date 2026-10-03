package structures;

import java.util.Map;

public sealed interface Node permits Value, Branch {
}

record Value(String value) implements Node {
}

record Branch(Map<String, Node> children) implements Node {
}
