package palaiologos.kamilalisp.runtime.cas;

import com.google.common.base.Objects;
import palaiologos.kamilalisp.atom.*;

import java.util.*;

public class MathExpression implements Userdata {
    private final Set<String> args;
    private final Atom data;
    private final Environment e;
    private final String expressionCache;

    public MathExpression(Environment e, Set<String> args, Atom data) {
        this.args = args;
        this.data = data;
        this.e = e;

        args.forEach(x -> {
            if(allowedFunctions.contains(x))
                throw new RuntimeException("Identifier " + x + " is not allowed as a variable, as it denotes a function already.");
        });

        expressionCache = stringifyExpression(data);
    }

    public Set<String> getArgs() {
        return args;
    }

    private static final Set<String> allowedFunctions = Set.of(
            "sin", "cos", "tan", "cot", "asin", "acos", "atan", "acot",
            "exp", "ln", "log2", "log10", "sqrt", "sec", "csc", "asec", "acsc",
            "dilog", "polylog"
    );

    private static final Map<String, Integer> expectedArities = Map.ofEntries(
            Map.entry("sin", 1),
            Map.entry("cos", 1),
            Map.entry("tan", 1),
            Map.entry("cot", 1),
            Map.entry("asin", 1),
            Map.entry("acos", 1),
            Map.entry("atan", 1),
            Map.entry("acot", 1),
            Map.entry("exp", 1),
            Map.entry("ln", 1),
            Map.entry("log2", 1),
            Map.entry("log10", 1),
            Map.entry("sqrt", 1),
            Map.entry("sec", 1),
            Map.entry("csc", 1),
            Map.entry("asec", 1),
            Map.entry("acsc", 1),
            Map.entry("dilog", 1),
            Map.entry("polylog", 2)
    );

    private static final Map<String, String> primitiveTranslations = Map.ofEntries(
            Map.entry("ln", "log")
    );

    private String stringifyExpression(Atom tree) {
        if(tree.getType() == Type.IDENTIFIER) {
            String id = tree.getIdentifier();
            if(id.startsWith("`")) {
                if(!e.has(id.substring(1)))
                    throw new RuntimeException("Unknown identifier: " + id.substring(1));
                return "(" + stringifyExpression(e.get(id.substring(1))) + ")";
            } else if(id.equals("pi")) {
                return "%pi";
            } else if(id.equals("e")) {
                return "exp(1)";
            } else if(id.equals("oo")) {
                return "%plusInfinity";
            } else if(id.equals("-oo")) {
                return "%minusInfinity";
            } else {
                if(!id.matches("[a-zA-Z]+"))
                    throw new RuntimeException("Invalid identifier: " + id);
                if(!args.contains(id))
                    throw new RuntimeException("Unbound identifier: " + id);
                return id;
            }
        } else if(tree.getType() == Type.REAL) {
            return tree.getReal().toString();
        } else if (tree.getType() == Type.INTEGER) {
            return tree.getInteger().toString();
        } else if(tree.getType() == Type.COMPLEX) {
            String re = tree.getComplex().re.toString();
            String im = tree.getComplex().im.toString();
            return re + "+(" + im + "*%i)";
        } else if(tree.getType() == Type.LIST) {
            Atom head = tree.getList().get(0);
            if(head.getType() != Type.IDENTIFIER) {
                throw new RuntimeException("Invalid function call.");
            }
            String id = head.getIdentifier();
            switch (id) {
                case "+" -> {
                    if (tree.getList().size() == 1)
                        throw new RuntimeException("Invalid arity for function: +");
                    if (tree.getList().size() == 2)
                        return "conj(" + stringifyExpression(tree.getList().get(1)) + ")";
                    return tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> "((" + x + ")+(" + y + "))").get();
                }
                case "-" -> {
                    if (tree.getList().size() == 1)
                        throw new RuntimeException("Invalid arity for function: -");
                    if (tree.getList().size() == 2)
                        return "-(" + stringifyExpression(tree.getList().get(1)) + ")";
                    return tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> "((" + x + ")-(" + y + "))").get();
                }
                case "*" -> {
                    if (tree.getList().size() == 1)
                        throw new RuntimeException("Invalid arity for function: *");
                    if (tree.getList().size() == 2)
                        return "signum(" + stringifyExpression(tree.getList().get(1)) + ")";
                    return tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> "((" + x + ")*(" + y + "))").get();
                }
                case "/" -> {
                    if (tree.getList().size() == 1)
                        throw new RuntimeException("Invalid arity for function: /");
                    if (tree.getList().size() == 2)
                        return "1/(" + stringifyExpression(tree.getList().get(1)) + ")";
                    return tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> "((" + x + ")/(" + y + "))").get();
                }
                case "**" -> {
                    if (tree.getList().size() <= 2)
                        throw new RuntimeException("Invalid arity for function: **");
                    return tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> "((" + x + ")^(" + y + "))").get();
                }
                case "pi" -> {
                    if (tree.getList().size() == 1)
                        return "%pi";
                    else if (tree.getList().size() == 2)
                        return "(%pi*(" + stringifyExpression(tree.getList().get(1)) + "))";
                    else
                        throw new RuntimeException("Invalid arity for function: gamma");
                }
                case "e" -> {
                    if (tree.getList().size() == 1)
                        return "exp(1)";
                    else if (tree.getList().size() == 2)
                        return "(exp(1)*(" + stringifyExpression(tree.getList().get(1)) + "))";
                    else
                        throw new RuntimeException("Invalid arity for function: gamma");
                }
                case "o" -> { return "0"; }
                case "gamma" -> {
                    if (tree.getList().size() == 2)
                        return "gamma(" + stringifyExpression(tree.getList().get(1)) + ")";
                    else if (tree.getList().size() == 3)
                        return "gamma(" + stringifyExpression(tree.getList().get(1)) + "," + stringifyExpression(tree.getList().get(2)) + ")";
                    else
                        throw new RuntimeException("Invalid arity for function: gamma");
                }
                default -> {
                    if (allowedFunctions.contains(id)) {
                        if (tree.getList().size() - 1 != expectedArities.get(id))
                            throw new RuntimeException("Invalid arity for function: " + id);
                        if (primitiveTranslations.containsKey(id))
                            id = primitiveTranslations.get(id);
                        return id + "(" + tree.getList().stream().skip(1).map(this::stringifyExpression).reduce((x, y) -> x + "," + y).get() + ")";
                    } else {
                        throw new RuntimeException("Unknown function: " + id);
                    }
                }
            }
        } else {
            throw new RuntimeException("Invalid expression. Unexpected component of type " + tree.getType());
        }
    }

    public Atom force() {
        if(!args.isEmpty())
            throw new IllegalArgumentException("The function is not constant.");
        return Evaluation.evaluate(e, data);
    }

    public String getExpression() {
        return expressionCache;
    }

    @Override
    public Atom field(Object key) {
        if(!(key instanceof String keyStr))
            throw new RuntimeException("Invalid key type: " + key.getClass().getName());
        if(!args.contains(keyStr))
            throw new RuntimeException("Invalid argument: " + keyStr);
        return new Atom(new MathExpressionSubstitute(keyStr));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expressionCache, args, data);
    }

    @Override
    public int compareTo(Userdata other) {
        return hashCode() - other.hashCode();
    }

    @Override
    public boolean equals(Userdata other) {
        if(!(other instanceof MathExpression otherExpr))
            return false;
        return expressionCache.equals(otherExpr.expressionCache);
    }

    public static MathExpression constantExpression(Environment env, Atom a) {
        if(a.getType() == Type.USERDATA && a.isUserdata(MathExpression.class))
            return a.getUserdata(MathExpression.class);
        return new MathExpression(env, Set.of(), a);
    }

    public static void unknownsFrom(Atom expr, Set<String> dest) {
        if(expr.getType() == Type.USERDATA && expr.isUserdata(MathExpression.class)) {
            MathExpression me = expr.getUserdata(MathExpression.class);
            dest.addAll(me.args);
        } else if(expr.getType() == Type.LIST) {
            if(expr.getList().isEmpty())
                throw new RuntimeException("Invalid expression: " + expr);
            for(Atom a : expr.getList().subList(1, expr.getList().size()))
                unknownsFrom(a, dest);
        } else if(expr.getType() == Type.IDENTIFIER) {
            dest.add(expr.getIdentifier());
        } else if(!expr.isNumeric()) {
            throw new RuntimeException("Invalid expression: " + expr);
        }
    }

    @Override
    public String toDisplayString() {
        return "ƒ(" + args.stream().reduce((x, y) -> x + "," + y).orElse("") + ")=" + data.toString();
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

    @Override
    public String typeName() {
        return "cas:function";
    }

    @Override
    public boolean coerceBoolean() {
        return true;
    }

    private class MathExpressionSubstitute extends PrimitiveFunction implements Lambda {
        private String keyStr;

        public MathExpressionSubstitute(String keyStr) {
            this.keyStr = keyStr;
        }

        @Override
        public Atom apply(Environment env, List<Atom> args) {
            assertArity(args, 1);
            Set<String> variables = new LinkedHashSet<>();
            unknownsFrom(args.get(0), variables);
            MathExpression me = new MathExpression(env, variables, args.get(0));
            Atom result = substituteRecursively(MathExpression.this.data, me.data);
            Set<String> returnVariables = new LinkedHashSet<>();
            unknownsFrom(result, returnVariables);
            return new Atom(new MathExpression(env, returnVariables, result));
        }

        private Atom substituteRecursively(Atom data, Atom sub) {
            if(data.getType() == Type.IDENTIFIER) {
                if(data.getIdentifier().equals(keyStr))
                    return sub;
                else
                    return data;
            } else if(data.getType() == Type.LIST) {
                List<Atom> newList = new ArrayList<>();
                for(Atom a : data.getList())
                    newList.add(substituteRecursively(a, sub));
                return new Atom(newList);
            } else {
                return data;
            }
        }

        @Override
        protected String name() {
            return "cas:function." + keyStr;
        }
    }
}