package palaiologos.kamilalisp.runtime.math;

import com.google.common.collect.Streams;
import palaiologos.kamilalisp.atom.*;
import palaiologos.kamilalisp.error.ArrayError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Max extends PrimitiveFunction implements Lambda {
    private static Atom max2(Environment e, Atom a, Atom b) {
        if (a.getType() == Type.LIST && b.getType() == Type.LIST) {
            int min = Math.min(a.getList().size(), b.getList().size());
            ArrayList<Atom> list = new ArrayList<>(min);
            for (int i = 0; i < min; i++) {
                Atom atom = max2(e, a.getList().get(i), b.getList().get(i));
                list.add(atom);
            }
            return new Atom(list);
        } else if (a.getType() == Type.REAL && b.getType() == Type.REAL) {
            return new Atom(a.getReal().max(b.getReal()));
        } else if (a.getType() == Type.INTEGER && b.getType() == Type.INTEGER) {
            return new Atom(a.getInteger().max(b.getInteger()));
        } else if (a.getType() == Type.INTEGER && b.getType() == Type.REAL) {
            return new Atom(a.getReal().max(b.getReal()));
        } else if (a.getType() == Type.REAL && b.getType() == Type.INTEGER) {
            return new Atom(a.getReal().max(b.getReal()));
        } else if (a.getType() == Type.LIST && b.getType() == Type.REAL) {
            ArrayList<Atom> list = new ArrayList<>(a.getList().size());
            for (Atom x : a.getList()) {
                Atom atom = max2(e, x, b);
                list.add(atom);
            }
            return new Atom(list);
        } else if (a.getType() == Type.REAL && b.getType() == Type.LIST) {
            ArrayList<Atom> list = new ArrayList<>(b.getList().size());
            for (Atom x : b.getList()) {
                Atom atom = max2(e, a, x);
                list.add(atom);
            }
            return new Atom(list);
        } else {
            throw new UnsupportedOperationException("max not defined for: " + a.getType() + " and " + b.getType());
        }
    }

    @Override
    protected String name() {
        return "max";
    }

    @Override
    public Atom apply(Environment env, List<Atom> args) {
        if (args.size() == 1 && args.get(0).getType() == Type.LIST) {
            boolean seen = false;
            Atom acc = null;
            for (Atom atom : args.get(0).getList()) {
                if (!seen) {
                    seen = true;
                    acc = atom;
                } else {
                    acc = max2(env, acc, atom);
                }
            }
            return (seen ? Optional.of(acc) : Optional.<Atom>empty()).orElseThrow(() -> new ArrayError("can't fold a list with max."));
        }
        else if (args.size() <= 1)
            throw new RuntimeException("max called with too few arguments.");

        boolean seen = false;
        Atom acc = null;
        for (Atom arg : args) {
            if (!seen) {
                seen = true;
                acc = arg;
            } else {
                acc = max2(env, acc, arg);
            }
        }
        return acc;
    }
}
