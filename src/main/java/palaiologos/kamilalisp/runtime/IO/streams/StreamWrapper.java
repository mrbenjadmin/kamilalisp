package palaiologos.kamilalisp.runtime.IO.streams;

import palaiologos.kamilalisp.atom.*;
import palaiologos.kamilalisp.runtime.dataformat.BufferAtomList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

public class StreamWrapper {
    public abstract static class InputStreamUserdata implements Userdata {
        public final InputStream stream;

        public InputStreamUserdata(InputStream stream) {
            this.stream = stream;
        }

        public InputStream getStream() {
            return stream;
        }

        @Override
        public int hashCode() {
            return stream.hashCode();
        }

        @Override
        public Atom field(Object key) {
            if (key instanceof String str) {
                return switch (str) {
                    case "available" -> {
                        try {
                            yield new Atom(BigInteger.valueOf(stream.available()));
                        } catch (IOException e) {
                            yield Atom.FALSE;
                        }
                    }
                    case "skip" -> new Atom(new InputStreamSkip());
                    case "read" -> new Atom(new InputStreamRead());
                    case "skip-n" -> new Atom(new InputStreamSkipN());
                    case "read-n" -> new Atom(new InputStreamReadN());
                    case "read-all" -> {
                        try {
                            yield new Atom(BufferAtomList.from(stream.readAllBytes()));
                        } catch (IOException e) {
                            yield Atom.FALSE;
                        }
                    }
                    case "read-byte" -> {
                        try {
                            yield new Atom(BigInteger.valueOf(stream.read()));
                        } catch (IOException e) {
                            yield Atom.FALSE;
                        }
                    }
                    case "skip-byte" -> {
                        try {
                            yield new Atom(BigInteger.valueOf(stream.skip(1)));
                        } catch (IOException e) {
                            yield Atom.FALSE;
                        }
                    }
                    case "close" -> new Atom(new InputStreamClose());
                    case "transfer-to" -> new Atom(new InputStreamTransferTo());
                    case "pipe-to" -> new Atom(new InputStreamPipeTo());
                    default -> specialField(key);
                };
            } else {
                return specialField(key);
            }
        }

        @Override
        public int compareTo(Userdata other) {
            return hashCode() - other.hashCode();
        }

        @Override
        public boolean equals(Userdata other) {
            return hashCode() == other.hashCode();
        }

        @Override
        public String typeName() {
            return "io:input-stream";
        }

        @Override
        public boolean coerceBoolean() {
            try {
                return stream.available() > 0;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public abstract String toDisplayString();

        public abstract Atom specialField(Object key);

        private class InputStreamClose extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
                return Atom.NULL;
            }

            @Override
            protected String name() {
                return "io:input-stream:close";
            }
        }

        private class InputStreamTransferTo extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                assertArity(args, 1);
                StreamWrapper.OutputStreamUserdata arg = (StreamWrapper.OutputStreamUserdata) args.get(0).getUserdata();
                try {
                    return new Atom(BigInteger.valueOf(stream.transferTo(arg.getStream())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:input-stream:transfer-to";
            }
        }

        private class InputStreamPipeTo extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                assertArity(args, 1);
                StreamWrapper.OutputStreamUserdata arg = (StreamWrapper.OutputStreamUserdata) args.get(0).getUserdata();
                new Thread(() -> {
                    try {
                        stream.transferTo(arg.getStream());
                    } catch (IOException e) {
                    }
                }).start();
                return Atom.NULL;
            }

            @Override
            protected String name() {
                return "io:input-stream:pipe-to";
            }
        }

        private class InputStreamSkip extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    return new Atom(BigInteger.valueOf(stream.skip(args.get(0).getInteger().longValue())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:input-stream:skip";
            }
        }

        private class InputStreamRead extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    byte[] buffer = new byte[args.get(0).getInteger().intValue()];
                    int read = stream.read(buffer);
                    return new Atom(BufferAtomList.from(buffer, read));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:input-stream:read";
            }
        }

        private class InputStreamSkipN extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    stream.skipNBytes(args.get(0).getInteger().longValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return Atom.NULL;
            }

            @Override
            protected String name() {
                return "io:input-stream:skip-n";
            }
        }

        private class InputStreamReadN extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    return new Atom(BufferAtomList.from(stream.readNBytes(args.get(0).getInteger().intValue())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:input-stream:read-n";
            }
        }
    }

    public abstract static class OutputStreamUserdata implements Userdata {
        public final OutputStream stream;

        public OutputStreamUserdata(OutputStream stream) {
            this.stream = stream;
        }

        public OutputStream getStream() {
            return stream;
        }

        @Override
        public int hashCode() {
            return stream.hashCode();
        }

        @Override
        public Atom field(Object key) {
            if (key instanceof String str) {
                return switch (str) {
                    case "write" -> new Atom(new OutputStreamWrite());
                    case "close" -> new Atom(new OutputStreamClose());
                    case "flush" -> new Atom(new OutputStreamFlush());
                    default -> specialField(key);
                };
            } else {
                return specialField(key);
            }
        }

        @Override
        public int compareTo(Userdata other) {
            return hashCode() - other.hashCode();
        }

        @Override
        public boolean equals(Userdata other) {
            return hashCode() == other.hashCode();
        }

        @Override
        public String typeName() {
            return "io:output-stream";
        }

        @Override
        public boolean coerceBoolean() {
            return true;
        }

        @Override
        public abstract String toDisplayString();

        public abstract Atom specialField(Object key);

        private class OutputStreamWrite extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    if (args.get(0).getType() == Type.LIST) {
                        List<Atom> buffer = args.get(0).getList();
                        byte[] bytes = new byte[buffer.size()];
                        for (int i = 0; i < buffer.size(); i++)
                            bytes[i] = buffer.get(i).getInteger().byteValue();
                        stream.write(bytes);
                    } else if (args.get(0).getType() == Type.INTEGER) {
                        stream.write(args.get(0).getInteger().intValue());
                    } else {
                        throw new RuntimeException("io:output-stream:write: expected integer or list");
                    }
                    return Atom.NULL;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:output-stream:write";
            }
        }

        private class OutputStreamFlush extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    stream.flush();
                    return Atom.NULL;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:output-stream:flush";
            }
        }

        private class OutputStreamClose extends PrimitiveFunction implements Lambda {
            @Override
            public Atom apply(Environment env, List<Atom> args) {
                try {
                    stream.close();
                    return Atom.NULL;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected String name() {
                return "io:output-stream:close";
            }
        }
    }
}
