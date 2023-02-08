package palaiologos.kamilalisp.runtime.IO;

import palaiologos.kamilalisp.atom.*;
import palaiologos.kamilalisp.runtime.dataformat.BufferAtomList;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GetFileBuffer extends PrimitiveFunction implements Lambda {
    @Override
    protected String name() {
        return "io:get-file-buffer";
    }

    @Override
    public Atom apply(Environment env, List<Atom> args) {
        assertArity(args, 1);
        Atom arg = args.get(0);
        arg.assertTypes(Type.STRING);
        String fileName = arg.getString();
        try {
            byte[] data = Files.readAllBytes(Path.of(fileName));
            return new Atom(BufferAtomList.from(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
