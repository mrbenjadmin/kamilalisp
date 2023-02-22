package palaiologos.kamilalisp.runtime.math.prime;

import palaiologos.kamilalisp.atom.*;

import java.math.BigInteger;
import java.util.List;

public class PrimeFactors extends PrimitiveFunction implements Lambda {
    private static Atom factor(Atom a) {
        if (a.getType() == Type.INTEGER) {
            if (a.getInteger().compareTo(BigInteger.TWO) < 0)
                throw new ArithmeticException("prime:factors not defined for integers less than 2");
            return new Atom(PollardRhoStateManager.factor(a.getInteger()).stream().map(Atom::new).toList());
        } else if (a.getType() == Type.LIST) {
            return new Atom(a.getList().stream().map(PrimeFactors::factor).toList());
        } else {
            throw new UnsupportedOperationException("prime:factors not defined for: " + a.getType());
        }
    }

    @Override
    public Atom apply(Environment env, List<Atom> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("prime:factors called with no arguments.");
        }

        if (args.size() == 1) {
            return factor(args.get(0));
        }

        return new Atom(args.stream().map(PrimeFactors::factor).toList());
    }

    @Override
    protected String name() {
        return "prime:factors";
    }
}
