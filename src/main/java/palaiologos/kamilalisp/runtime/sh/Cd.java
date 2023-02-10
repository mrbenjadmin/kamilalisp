package palaiologos.kamilalisp.runtime.sh;

import palaiologos.kamilalisp.atom.*;

import java.io.File;
import java.util.List;

public class Cd extends ShellFunction {
    @Override
    protected String command() {
        return "cd";
    }

    @Override
    protected Atom execute(String flags, List<Atom> args) {
        assertArity(args, 1);
        String dir = args.get(0).getString();
        File dest = new File(dir);
        if (!dest.exists())
            throw new RuntimeException("Directory " + dir + " does not exist");
        if (!dest.isDirectory())
            throw new RuntimeException(dir + " is not a directory");
        System.setProperty("user.dir", dest.getAbsolutePath());
        return Atom.NULL;
    }
}