package palaiologos.kamilalisp.runtime.flt64;

import palaiologos.kamilalisp.atom.Atom;
import palaiologos.kamilalisp.atom.Environment;
import palaiologos.kamilalisp.atom.Lambda;
import palaiologos.kamilalisp.atom.PrimitiveFunction;

import java.math.BigDecimal;

public class Flt64Base {
    public static final double EPSILON = Math.ulp(1.0d);

    static double toFlt64(Atom a) {
        return switch (a.getType()) {
            case INTEGER -> a.getInteger().doubleValue();
            case REAL -> a.getReal().doubleValue();
            case COMPLEX -> a.getComplex().re.doubleValue();
            default -> throw new IllegalArgumentException("Cannot convert " + a.getType() + " to flt64");
        };
    }

    public static Atom toAtom(double d) {
        if(Double.isNaN(d))
            throw new ArithmeticException("NaN");
        return new Atom(BigDecimal.valueOf(d));
    }

    public void registerFlt64(Environment env) {
        // TODO:
        // airy-bi, hypergeom-2f1, hypergeom-pfq, meijer-g, fox-h, hypergeom-1f1, log-integral,
        // whittaker-m, whittaker-w, elliptic-k, elliptic-f, elliptic-e, elliptic-pi, hankel-h2,
        // dirichlet-beta, dirichlet-eta, dirichlet-lambda, barnesg, logbarnesg, hankel-h1,
        env.setPrimitive("flt64:+", new Atom(Flt64Arith.add));
        env.setPrimitive("flt64:-", new Atom(Flt64Arith.sub));
        env.setPrimitive("flt64:*", new Atom(Flt64Arith.mul));
        env.setPrimitive("flt64:/", new Atom(Flt64Arith.div));
        env.setPrimitive("flt64:mod", new Atom(Flt64Arith.mod));
        env.setPrimitive("flt64:**", new Atom(Flt64Arith.pow));
        env.setPrimitive("flt64:abs", new Atom(Flt64Arith.abs));
        env.setPrimitive("flt64:ceil", new Atom(Flt64Arith.ceil));
        env.setPrimitive("flt64:floor", new Atom(Flt64Arith.floor));
        env.setPrimitive("flt64:round", new Atom(Flt64Arith.round));
        env.setPrimitive("flt64:exp", new Atom(Flt64Arith.exp));
        env.setPrimitive("flt64:ln", new Atom(Flt64Arith.ln));
        env.setPrimitive("flt64:log2", new Atom(Flt64Arith.log2));
        env.setPrimitive("flt64:log10", new Atom(Flt64Arith.log));
        env.setPrimitive("flt64:sqrt", new Atom(Flt64Arith.sqrt));
        env.setPrimitive("flt64:min", new Atom(Flt64Cmp.min));
        env.setPrimitive("flt64:max", new Atom(Flt64Cmp.max));
        env.setPrimitive("flt64:signum", new Atom(Flt64Cmp.signum));
        env.setPrimitive("flt64:sin", new Atom(Flt64Trig.sin));
        env.setPrimitive("flt64:cos", new Atom(Flt64Trig.cos));
        env.setPrimitive("flt64:tan", new Atom(Flt64Trig.tan));
        env.setPrimitive("flt64:asin", new Atom(Flt64Trig.asin));
        env.setPrimitive("flt64:acos", new Atom(Flt64Trig.acos));
        env.setPrimitive("flt64:atan", new Atom(Flt64Trig.atan));
        env.setPrimitive("flt64:sec", new Atom(Flt64Trig.sec));
        env.setPrimitive("flt64:csc", new Atom(Flt64Trig.csc));
        env.setPrimitive("flt64:cot", new Atom(Flt64Trig.cot));
        env.setPrimitive("flt64:asec", new Atom(Flt64Trig.asec));
        env.setPrimitive("flt64:acsc", new Atom(Flt64Trig.acsc));
        env.setPrimitive("flt64:acot", new Atom(Flt64Trig.acot));
        env.setPrimitive("flt64:sinh", new Atom(Flt64Trig.sinh));
        env.setPrimitive("flt64:cosh", new Atom(Flt64Trig.cosh));
        env.setPrimitive("flt64:tanh", new Atom(Flt64Trig.tanh));
        env.setPrimitive("flt64:asinh", new Atom(Flt64Trig.asinh));
        env.setPrimitive("flt64:acosh", new Atom(Flt64Trig.acosh));
        env.setPrimitive("flt64:atanh", new Atom(Flt64Trig.atanh));
        env.setPrimitive("flt64:sech", new Atom(Flt64Trig.sech));
        env.setPrimitive("flt64:csch", new Atom(Flt64Trig.csch));
        env.setPrimitive("flt64:coth", new Atom(Flt64Trig.coth));
        env.setPrimitive("flt64:gamma", new Atom(Flt64Gamma.fGamma));
        env.setPrimitive("flt64:digamma", new Atom(Flt64Gamma.fDigamma));
        env.setPrimitive("flt64:trigamma", new Atom(Flt64Gamma.fTrigamma));
        env.setPrimitive("flt64:beta", new Atom(Flt64Gamma.fBeta));
        env.setPrimitive("flt64:zeta", new Atom(Flt64Zeta.fRiemannZeta));
        env.setPrimitive("flt64:hurwitz-zeta", new Atom(Flt64Zeta.fHurwitzZeta));
        env.setPrimitive("flt64:polygamma", new Atom(Flt64Gamma.fPolygamma));
        env.setPrimitive("flt64:pochhammer", new Atom(Flt64Gamma.fPochhammer));
        env.setPrimitive("flt64:erf", new Atom(Flt64Erf.erf));
        env.setPrimitive("flt64:erfc", new Atom(Flt64Erf.erfc));
        env.setPrimitive("flt64:erf-inverse", new Atom(Flt64Erf.erfInverse));
        env.setPrimitive("flt64:erfc-inverse", new Atom(Flt64Erf.erfcInverse));
        env.setPrimitive("flt64:erfi", new Atom(Flt64Erf.erfi));
        env.setPrimitive("flt64:dawson+", new Atom(Flt64Erf.dawson));
        env.setPrimitive("flt64:dawson-", new Atom(Flt64Erf.dawsonMinus));
        env.setPrimitive("flt64:ui-gamma", new Atom(Flt64Gamma.fUpperIncompleteGamma));
        env.setPrimitive("flt64:li-gamma", new Atom(Flt64Gamma.fLowerIncompleteGamma));
        env.setPrimitive("flt64:log-gamma", new Atom(Flt64Gamma.fLogGamma));
        env.setPrimitive("flt64:bessel-j0", new Atom(Flt64Bessel.fBessel0));
        env.setPrimitive("flt64:bessel-j1", new Atom(Flt64Bessel.fBessel1));
        env.setPrimitive("flt64:bessel-jn", new Atom(Flt64Bessel.fBessel));
        env.setPrimitive("flt64:bessel-jn-derv", new Atom(Flt64Bessel.fBesselderv));
        env.setPrimitive("flt64:bessel-y0", new Atom(Flt64Bessel.fBessely0));
        env.setPrimitive("flt64:bessel-y1", new Atom(Flt64Bessel.fBessely1));
        env.setPrimitive("flt64:bessel-yn", new Atom(Flt64Bessel.fBessely));
        env.setPrimitive("flt64:bessel-i0", new Atom(Flt64Bessel.fBesseli0));
        env.setPrimitive("flt64:bessel-i1", new Atom(Flt64Bessel.fBesseli1));
        env.setPrimitive("flt64:bessel-k0", new Atom(Flt64Bessel.fBesselk0));
        env.setPrimitive("flt64:bessel-k1", new Atom(Flt64Bessel.fBesselk1));
        env.setPrimitive("flt64:bessel-kn", new Atom(Flt64Bessel.fBesselk));
        env.setPrimitive("flt64:airy-ai", new Atom(Flt64Airy.airy));
        env.setPrimitive("flt64:airy-ai-derv", new Atom(Flt64Airy.airyDerv));
        env.setPrimitive("flt64:fresnel-S", new Atom(Flt64Fresnel.fFresnelSine));
        env.setPrimitive("flt64:fresnel-C", new Atom(Flt64Fresnel.fFresnelCosine));
        env.setPrimitive("flt64:Si", new Atom(Flt64TrigonometricIntegral.fSi));
        env.setPrimitive("flt64:si", new Atom(Flt64TrigonometricIntegral.fsi));
        env.setPrimitive("flt64:Ci", new Atom(Flt64TrigonometricIntegral.fCi));
        env.setPrimitive("flt64:Cin", new Atom(Flt64TrigonometricIntegral.fCin));
        env.setPrimitive("flt64:Shi", new Atom(Flt64TrigonometricIntegral.fShi));
        env.setPrimitive("flt64:Chi", new Atom(Flt64TrigonometricIntegral.fChi));
        env.setPrimitive("flt64:Ei", new Atom(Flt64Ei.fEi));
        env.setPrimitive("flt64:lambert-w0", new Atom(Flt64Lambert.lambert0));
        env.setPrimitive("flt64:lambert-w-1", new Atom(Flt64Lambert.lambertn1));
        env.setPrimitive("flt64:spence", new Atom(Flt64Spence.spence));
        env.setPrimitive("flt64:dilog", new Atom(Flt64Spence.dilog));
        env.setPrimitive("flt64:frexp", new Atom(Flt64Spence.frexp));
        env.setPrimitive("flt64:factorial", new Atom(Flt64Spence.factorial));
        env.setPrimitive("flt64:polylog", new Atom(Flt64Spence.fPolylogarithm));
        env.setPrimitive("flt64:lerch-phi", new Atom(Flt64Zeta.fLerchPhi));
        env.setPrimitive("flt64:=", new Atom(Flt64Cmp.eq));
        env.setPrimitive("flt64:/=", new Atom(Flt64Cmp.ne));
        env.setPrimitive("flt64:<", new Atom(Flt64Cmp.lt));
        env.setPrimitive("flt64:<=", new Atom(Flt64Cmp.le));
        env.setPrimitive("flt64:>", new Atom(Flt64Cmp.gt));
        env.setPrimitive("flt64:>=", new Atom(Flt64Cmp.ge));
        env.setPrimitive("flt64:LU", new Atom(new Flt64LU()));
        env.setPrimitive("flt64:PLU", new Atom(new Flt64PLU()));
        env.setPrimitive("flt64:det", new Atom(new Flt64Det()));
        env.setPrimitive("flt64:permanent", new Atom(new Flt64Permanent()));
        env.setPrimitive("flt64:invert", new Atom(new Flt64Inv()));
        env.setPrimitive("flt64:trace", new Atom(new Flt64Trace()));
        env.setPrimitive("flt64:e", toAtom(Math.E));
        env.setPrimitive("flt64:pi", toAtom(Math.PI));
        env.setPrimitive("flt64:euler-gamma", toAtom(0.57721566490153286060651209008240243104215933593992));
        env.setPrimitive("flt64:golden-ratio", toAtom(1.61803398874989484820));
        env.setPrimitive("flt64:apery", toAtom(Math.log(1.20205690315959428539)));
        env.setPrimitive("flt64:glaisher-A", toAtom(1.28242712910062263687));
        env.setPrimitive("flt64:catalan-C", toAtom(0.91596559417721901505));
        env.setPrimitive("flt64:golomb-lambda", toAtom(0.62432998854355087099));
        env.setPrimitive("flt64:khinchin-K0", toAtom(2.68545200106530644530));
        env.setPrimitive("flt64:mills-A", toAtom(1.30637788386308069046));
    }

    abstract static class Flt64Function extends PrimitiveFunction implements Lambda {
    }
}
