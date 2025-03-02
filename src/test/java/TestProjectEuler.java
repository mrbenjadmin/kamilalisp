import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestProjectEuler {
    @Test
    void testProblem1() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem1.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem2() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem2.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem3() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem3.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem4() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem4.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem5() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem5.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem6() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem6.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem7() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem7.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem9() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem9.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem10() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem10.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem12() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem12.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem14() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem14.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem15() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem15.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem16() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem16.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem20() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem20.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem21() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem21.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProblem23() {
        try {
            assertTrue(Common.runCode(Files.readString(Path.of("project-euler/problem23.lisp"))).coerceBool());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}