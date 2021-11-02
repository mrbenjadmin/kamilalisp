package kamilalisp.libs;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import kamilalisp.data.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListLib {
    public static void install(Environment env) {
        env.push("iota", new Atom(new Closure() {
            private List<BigDecimal> iota(long n) {
                List<BigDecimal> result = new ArrayList<>();
                for(long i = 0; i < n; i++)
                    result.add(BigDecimal.valueOf(i));
                return result;
            }

            protected <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
                List<List<T>> resultLists = new ArrayList<List<T>>();
                if (lists.size() == 0) {
                    resultLists.add(new ArrayList<T>());
                    return resultLists;
                } else {
                    List<T> firstList = lists.get(0);
                    List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
                    for (T condition : firstList) {
                        for (List<T> remainingList : remainingLists) {
                            ArrayList<T> resultList = new ArrayList<T>();
                            resultList.add(condition);
                            resultList.addAll(remainingList);
                            resultLists.add(resultList);
                        }
                    }
                }
                return resultLists;
            }

            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 1)
                    throw new Error("Invalid invocation to 'iota'.");
                if(arguments.get(0).getType() == Type.NUMBER)
                    return new Atom(new LbcSupplier<>(() ->
                            iota(arguments.get(0).getNumber().get().toBigInteger().intValue())
                                    .stream().map(Atom::new).collect(Collectors.toList())));
                else if(arguments.get(0).getType() == Type.LIST) {
                    return new Atom(new LbcSupplier<>(() -> {
                        List<List<BigDecimal>> iotas = arguments.get(0).getList().get().stream().map(x -> {
                            if(x.getType() != Type.NUMBER)
                                throw new Error("Invalid invocation to 'iota'. Expected a list of numbers.");
                            return iota(x.getNumber().get().toBigInteger().intValue());
                        }).collect(Collectors.toList());

                        return cartesianProduct(iotas)
                                .stream()
                                .map(x -> new Atom(x.stream().map(Atom::new).collect(Collectors.toList())))
                                .collect(Collectors.toList());
                    }));
                }
                throw new Error("Unimplemented");
            }
        }));

        env.push("nth", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 2)
                    throw new Error("Invalid invocation to 'nth'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("Argument to 'nth'.", Type.NUMBER);
                    if(arguments.get(1).getType() == Type.LIST) {
                        List<Atom> l = arguments.get(1).getList().get();
                        int n = arguments.get(0).getNumber().get().intValue();
                        if (n < 0 || n >= l.size())
                            throw new Error("Index out of bounds.");
                        return l.get(n).get().get();
                    } else if(arguments.get(1).getType() == Type.STRING_CONSTANT) {
                        String s = arguments.get(1).getStringConstant().get().get();
                        int n = arguments.get(0).getNumber().get().intValue();
                        if (n < 0 || n >= s.length())
                            throw new Error("Index out of bounds.");
                        return new StringConstant("" + s.charAt(n));
                    }

                    throw new Error("Invalid argument to 'nth'.");
                }));
            }
        }));

        env.push("tie", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                return new Atom(arguments);
            }
        }));

        env.push("flatten", new Atom(new Closure() {
            public <T> List<T> flat(List<List<T>> list) {
                return list.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }

            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 1)
                    throw new Error("Invalid invocation to 'flatten'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("Argument to 'flatten'.", Type.LIST);
                    List<Atom> l = arguments.get(0).getList().get();
                    return flat(l.stream().map(x -> x.getType() == Type.LIST ? x.getList().get() : List.of(x)).collect(Collectors.toList()));
                }));
            }
        }));

        env.push("reverse", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 1)
                    throw new Error("Invalid invocation to 'reverse'.");
                return new Atom(new LbcSupplier<>(() -> {
                    if(arguments.get(0).getType() == Type.LIST) {
                        List<Atom> l = arguments.get(0).getList().get();
                        List<Atom> r = new ArrayList<>();
                        for (int i = l.size() - 1; i >= 0; i--)
                            r.add(l.get(i));
                        return r;
                    } else if(arguments.get(0).getType() == Type.STRING_CONSTANT) {
                        String s = arguments.get(0).getStringConstant().get().get();
                        String r = "";
                        for (int i = s.length() - 1; i >= 0; i--)
                            r += s.charAt(i);
                        return new StringConstant(r);
                    }

                    throw new Error("Invalid invocation to 'reverse': expected a string or list.");
                }));
            }
        }));

        env.push("rotate", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 2)
                    throw new Error("Invalid invocation to 'rotate'.");
                return new Atom(new LbcSupplier<>(() -> {
                    if(arguments.get(0).getType() == Type.LIST) {
                        List<Atom> l = arguments.get(0).getList().get();
                        int n = arguments.get(1).getNumber().get().intValue();
                        if(n < 0)
                            n += l.size();
                        if(n >= l.size())
                            n %= l.size();
                        List<Atom> r = new ArrayList<>();
                        for (int i = n; i < l.size(); i++)
                            r.add(l.get(i));
                        for (int i = 0; i < n; i++)
                            r.add(l.get(i));
                        return r;
                    } else if(arguments.get(0).getType() == Type.STRING_CONSTANT) {
                        String s = arguments.get(0).getStringConstant().get().get();
                        int n = arguments.get(1).getNumber().get().intValue();
                        if(n < 0)
                            n += s.length();
                        if(n >= s.length())
                            n %= s.length();
                        String r = "";
                        for (int i = n; i < s.length(); i++)
                            r += s.charAt(i);
                        for (int i = 0; i < n; i++)
                            r += s.charAt(i);
                        return new StringConstant(r);
                    }

                    throw new Error("Invalid invocation to 'rotate': expected a string or list.");
                }));
            }
        }));

        env.push("zip", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 2)
                    throw new Error("Invalid invocation to 'zip'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'zip'", Type.LIST);
                    arguments.get(1).guardType("Second argument to 'zip'", Type.LIST);
                    List<Atom> a = arguments.get(0).getList().get();
                    List<Atom> b = arguments.get(1).getList().get();
                    return Streams.zip(a.stream(), b.stream(), (x, y) -> new Atom(new LbcSupplier<>(() -> List.of(x, y)))).collect(Collectors.toList());
                }));
            }
        }));

        env.push("foldl", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 3)
                    throw new Error("Invalid invocation to 'foldl'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'foldl'", Type.CLOSURE);
                    arguments.get(2).guardType("Third argument to 'foldl'", Type.LIST);
                    List<Atom> data = arguments.get(2).getList().get();
                    Atom acc = arguments.get(1);
                    if(data.isEmpty())
                        return acc.get().get();
                    else {
                        return Stream.concat(Stream.of(acc), data.stream()).reduce((x, y) ->
                                arguments.get(0).getClosure().get().apply(env, Arrays.asList(x, y))
                        ).get().get().get();
                    }
                }));
            }
        }));

        env.push("foldr", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 3)
                    throw new Error("Invalid invocation to 'foldr'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'foldr'", Type.CLOSURE);
                    arguments.get(2).guardType("Third argument to 'foldr'", Type.LIST);
                    List<Atom> data = arguments.get(2).getList().get();
                    Atom acc = arguments.get(1);
                    if(data.isEmpty())
                        return acc.get().get();
                    else {
                        return Stream.concat(Stream.of(acc), Lists.reverse(data).stream()).reduce((x, y) ->
                                arguments.get(0).getClosure().get().apply(env, Arrays.asList(y, x))
                        ).get().get().get();
                    }
                }));
            }
        }));

        env.push("foldl'", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 3)
                    throw new Error("Invalid invocation to 'foldl''.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'foldl''", Type.CLOSURE);
                    arguments.get(2).guardType("Third argument to 'foldl''", Type.LIST);
                    List<Atom> data = arguments.get(2).getList().get();
                    Atom acc = arguments.get(1);
                    if(data.isEmpty())
                        return acc.get().get();
                    else {
                        return Stream.concat(Stream.of(acc), data.stream()).reduce((x, y) ->
                                arguments.get(0).getClosure().get().apply(env, Arrays.asList(x.eager(), y.eager())).eager()
                        ).get().get().get();
                    }
                }));
            }
        }));

        env.push("foldr'", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 3)
                    throw new Error("Invalid invocation to 'foldr''.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'foldr''", Type.CLOSURE);
                    arguments.get(2).guardType("Third argument to 'foldr''", Type.LIST);
                    List<Atom> data = arguments.get(2).getList().get();
                    Atom acc = arguments.get(1);
                    if(data.isEmpty())
                        return acc.get().get();
                    else {
                        return Stream.concat(Stream.of(acc), Lists.reverse(data).stream()).reduce((x, y) ->
                                arguments.get(0).getClosure().get().apply(env, Arrays.asList(y.eager(), x.eager())).eager()
                        ).get().get().get();
                    }
                }));
            }
        }));

        env.push("any", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 2)
                    throw new Error("Invalid invocation to 'any'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("First argument to 'any'", Type.CLOSURE);
                    arguments.get(1).guardType("Second argument to 'any'", Type.LIST);
                    return arguments.get(1).getList().get().stream().anyMatch(x ->
                            arguments.get(0).getClosure().get().apply(env, Collections.singletonList(x)).coerceBool()
                    ) ? BigDecimal.ONE : BigDecimal.ZERO;
                }));
            }
        }));

        env.push("cdr", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 1)
                    throw new Error("Invalid invocation to 'cdr'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("Argument to 'cdr'", Type.LIST);
                    List<Atom> data = arguments.get(0).getList().get();
                    if(data.isEmpty())
                        return Atom.NULL.get().get();
                    else
                        return data.subList(1, data.size());
                }));
            }
        }));

        env.push("cons", new Atom(new Closure() {
            private List<Atom> cons2(Atom element, Atom list) {
                if(list.getType() != Type.LIST)
                    throw new Error("invalid invocation to 'cons'.");
                LinkedList<Atom> l = new LinkedList<>(list.getList().get());
                l.addFirst(element);
                return l;
            }

            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 2)
                    throw new Error("invalid invocation to 'cons'.");
                else
                    return new Atom(new LbcSupplier(() -> cons2(arguments.get(0), arguments.get(1))));
            }
        }));

        env.push("append", new Atom(new Closure() {
            private List<Atom> append2(Atom list, Atom tail) {
                list.guardType("Argument to 'append'", Type.LIST);
                LinkedList<Atom> l = new LinkedList<Atom>(list.getList().get());
                l.addLast(tail);
                return l;
            }

            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() < 2)
                    throw new Error("invalid invocation to 'append'.");
                if(arguments.size() == 2)
                    return new Atom(new LbcSupplier(() -> append2(arguments.get(0), arguments.get(1))));
                else
                    return new Atom(new LbcSupplier(() -> {
                        arguments.get(0).guardType("Argument to 'append'", Type.LIST);
                        List<Atom> l = new LinkedList<>(arguments.get(0).getList().get());
                        l.addAll(arguments.stream().skip(1).collect(Collectors.toList()));
                        return l;
                    }));
            }
        }));

        env.push("car", new Atom(new Closure() {
            private Atom car(Atom l) {
                l.guardType("Argument to 'car'", Type.LIST);
                List<Atom> data = l.getList().get();
                if(data.isEmpty())
                    return Atom.NULL;
                else
                    return data.get(0);
            }

            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() == 1) {
                    return new Atom(new LbcSupplier(() -> car(arguments.get(0)).get().get()));
                } else if(arguments.size() >= 2) {
                    return new Atom(new LbcSupplier(() -> arguments.stream().map(x -> car(x)).collect(Collectors.toList())));
                } else
                    throw new Error("Invalid invocation to 'car'.");
            }
        }));

        env.push("size", new Atom(new Closure() {
            @Override
            public Atom apply(Executor env, List<Atom> arguments) {
                if(arguments.size() != 1)
                    throw new Error("Invalid invocation to 'size'.");
                return new Atom(new LbcSupplier<>(() -> {
                    arguments.get(0).guardType("Argument to 'size'", Type.LIST, Type.STRING_CONSTANT);

                    if(arguments.get(0).getType() == Type.LIST)
                        return BigDecimal.valueOf(arguments.get(0).getList().get().size());
                    else if(arguments.get(0).getType() == Type.STRING_CONSTANT)
                        return BigDecimal.valueOf(arguments.get(0).getStringConstant().get().get().length());

                    throw new Error("Invalid invocation to 'size'.");
                }));
            }
        }));
    }
}
