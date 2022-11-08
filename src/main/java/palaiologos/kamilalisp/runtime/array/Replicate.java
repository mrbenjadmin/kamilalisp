package palaiologos.kamilalisp.runtime.array;

import ch.obermuhlner.math.big.BigComplexMath;
import ch.obermuhlner.math.big.BigDecimalMath;
import com.google.common.collect.Streams;
import palaiologos.kamilalisp.atom.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Replicate extends PrimitiveFunction implements Lambda {
    @Override
    protected String name() {
        return "replicate";
    }

    private static Atom replicate(List<Atom> l1, List<Atom> l2) {
        ArrayList<Atom> result = new ArrayList<>();

        if(l1.size() != l2.size()) {
            throw new RuntimeException("replicate: lists must be of equal length.");
        }

        for(int i = 0; i < l1.size(); i++) {
            Atom a1 = l1.get(i);
            Atom a2 = l2.get(i);
            if(a1.getType() == Type.INTEGER) {
                if(a1.getInteger().compareTo(BigInteger.ZERO) < 0)
                    throw new RuntimeException("replicate: negative count.");
                for(int j = 0; j < a1.getInteger().intValue(); j++)
                    result.add(a2);
            } else if(a1.getType() == Type.LIST && a2.getType() == Type.LIST) {
                result.add(replicate(a1.getList(), a2.getList()));
            } else {
                throw new RuntimeException("replicate: invalid arguments.");
            }
        }

        return new Atom(result);
    }

    @Override
    public Atom apply(Environment env, List<Atom> args) {
        assertArity(args, 2);
        List<Atom> list1 = args.get(0).getList();
        List<Atom> list2 = args.get(1).getList();
        return replicate(list1, list2);
    }
}