package palaiologos.kamilalisp.runtime.array;

import palaiologos.kamilalisp.atom.*;
import palaiologos.kamilalisp.error.TypeError;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Tally extends PrimitiveFunction implements Lambda {
    private static final String name = "tally";

    @Override
    protected String name() {
        return name;
    }

    private static Atom tally(Atom a) {
        if(a.getType() == Type.LIST) {
            return new Atom(BigDecimal.valueOf(a.getList().size()));
        } else {
            return new Atom(BigDecimal.ZERO);
        }
    }

    @Override
    public Atom apply(Environment env, List<Atom> args) {
        if(args.isEmpty())
            throw new TypeError("Expected 1 or more arguments to `" + name + "'.");
        if(args.size() == 1)
            return tally(args.get(0));
        return new Atom(args.stream().map(Tally::tally).collect(Collectors.toList()));
    }
}