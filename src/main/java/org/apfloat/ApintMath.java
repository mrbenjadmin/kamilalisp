/*
 * MIT License
 *
 * Copyright (c) 2002-2023 Mikko Tommila
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.apfloat;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Random;

/**
 * Various mathematical functions for arbitrary precision integers.
 *
 * @version 1.11.0
 * @author Mikko Tommila
 */

public class ApintMath
{
    private ApintMath()
    {
    }

    /**
     * Integer power.
     *
     * @param x Base of the power operator.
     * @param n Exponent of the power operator.
     *
     * @return <code>x</code> to the <code>n</code>:th power, that is <code>x<sup>n</sup></code>.
     *
     * @exception ArithmeticException If both <code>x</code> and <code>n</code> are zero.
     */

    public static Apint pow(Apint x, long n)
        throws ArithmeticException, ApfloatRuntimeException
    {
        if (n == 0)
        {
            if (x.signum() == 0)
            {
                throw new ArithmeticException("Zero to power zero");
            }

            return new Apint(1, x.radix());
        }
        else if (n < 0)
        {
            return Apint.ZEROS[x.radix()];
        }

        // Algorithm improvements by Bernd Kellner
        int b2pow = 0;

        while ((n & 1) == 0)
        {
            b2pow++;
            n >>= 1;
        }

        Apint r = x;

        while ((n >>= 1) > 0)
        {
            x = x.multiply(x);
            if ((n & 1) != 0)
            {
                r = r.multiply(x);
            }
        }

        while (b2pow-- > 0)
        {
            r = r.multiply(r);
        }

        return r;
    }

    /**
     * Square root and remainder.
     *
     * @param x The argument.
     *
     * @return An array of two apints: <code>[q, r]</code>, where <code>q<sup>2</sup> + r = x</code>.
     *
     * @exception ArithmeticException If <code>x</code> is negative.
     */

    public static Apint[] sqrt(Apint x)
        throws ArithmeticException, ApfloatRuntimeException
    {
        return root(x, 2);
    }

    /**
     * Cube root and remainder.
     *
     * @param x The argument.
     *
     * @return An array of two apints: <code>[q, r]</code>, where <code>q<sup>3</sup> + r = x</code>.
     */

    public static Apint[] cbrt(Apint x)
        throws ApfloatRuntimeException
    {
        return root(x, 3);
    }

    /**
     * Positive integer root and remainder.<p>
     *
     * Returns the <code>n</code>:th root of <code>x</code>,
     * that is <code>x<sup>1/n</sup></code>, rounded towards zero.
     *
     * @param x The argument.
     * @param n Which root to take.
     *
     * @return An array of two apints: <code>[q, r]</code>, where <code>q<sup>n</sup> + r = x</code>.
     *
     * @exception ArithmeticException If <code>n</code> and <code>x</code> are zero, or <code>x</code> is negative and <code>n</code> is even.
     */

    public static Apint[] root(Apint x, long n)
        throws ArithmeticException, ApfloatRuntimeException
    {
        if (n == 0)
        {
            if (x.signum() == 0)
            {
                throw new ArithmeticException("Zeroth root of zero");
            }

            Apint one = new Apint(1, x.radix());
            return new Apint[] { one, x.subtract(one) };
        }
        else if (x.signum() == 0)
        {
            return new Apint[] { x, x };                        // Avoid division by zero
        }
        else if (x.equals(Apint.ONE) || n == 1)
        {
            return new Apint[] { x, Apint.ZEROS[x.radix()] };
        }
        else if (n < 0)
        {
            return new Apint[] { Apint.ZEROS[x.radix()], x };   // 1 / x where x > 1
        }

        long precision = x.scale() / n + Apint.EXTRA_PRECISION;
        Apfloat approxX = x.precision(precision);
        Apfloat approxRoot;

        approxRoot = ApfloatMath.root(approxX, n);

        Apint root = approxRoot.truncate(),                             // May be one too big or one too small
              pow = pow(root, n);

        if (abs(pow).compareTo(abs(x)) > 0)
        {
            // Approximate root was one too big

            pow = (x.signum() >= 0 ? powXMinus1(pow, root, n) : powXPlus1(pow, root, n));
            root = root.subtract(new Apint(x.signum(), x.radix()));
        }
        else
        {
            // Approximate root was correct or one too small

            Apint powPlus1 = (x.signum() >= 0 ? powXPlus1(pow, root, n) : powXMinus1(pow, root, n));

            if (abs(powPlus1).compareTo(abs(x)) <= 0)
            {
                // Approximate root was one too small

                pow = powPlus1;
                root = root.add(new Apint(x.signum(), x.radix()));
            }
        }

        Apint remainder = x.subtract(pow);

        assert (remainder.signum() * x.signum() >= 0);

        return new Apint[] { root, remainder };
    }

    private static Apint powXMinus1(Apint pow, Apint x, long n)
        throws ApfloatRuntimeException
    {
        Apint one = new Apint(1, x.radix());

        if (n == 2)
        {
            // (x - 1)^2 = x^2 - 2*x + 1
            pow = pow.subtract(x).subtract(x).add(one);
        }
        else if (n == 3)
        {
            // (x - 1)^3 = x^3 - 3*x^2 + 3*x - 1 = x^3 - 3*x*(x - 1) - 1
            pow = pow.subtract(new Apint(3, x.radix()).multiply(x).multiply(x.subtract(one))).subtract(one);
        }
        else
        {
            pow = pow(x.subtract(one), n);
        }

        return pow;
    }

    private static Apint powXPlus1(Apint pow, Apint x, long n)
        throws ApfloatRuntimeException
    {
        Apint one = new Apint(1, x.radix());

        if (n == 2)
        {
            // (x + 1)^2 = x^2 + 2*x + 1
            pow = pow.add(x).add(x).add(one);
        }
        else if (n == 3)
        {
            // (x + 1)^3 = x^3 + 3*x^2 + 3*x + 1 = x^3 + 3*x*(x + 1) + 1
            pow = pow.add(new Apint(3, x.radix()).multiply(x).multiply(x.add(one))).add(one);
        }
        else
        {
            pow = pow(x.add(one), n);
        }

        return pow;
    }

    /**
     * Returns an apint whose value is <code>-x</code>.
     *
     * @deprecated Use {@link Apint#negate()}.
     *
     * @param x The argument.
     *
     * @return <code>-x</code>.
     */

    @Deprecated
    public static Apint negate(Apint x)
        throws ApfloatRuntimeException
    {
        return x.negate();
    }

    /**
     * Absolute value.
     *
     * @param x The argument.
     *
     * @return Absolute value of <code>x</code>.
     */

    public static Apint abs(Apint x)
        throws ApfloatRuntimeException
    {
        if (x.signum() >= 0)
        {
            return x;
        }
        else
        {
            return x.negate();
        }
    }

    /**
     * Copy sign from one argument to another.
     *
     * @param x The value whose sign is to be adjusted.
     * @param y The value whose sign is to be used.
     *
     * @return <code>x</code> with its sign changed to match the sign of <code>y</code>.
     *
     * @since 1.1
     */

    public static Apint copySign(Apint x, Apint y)
        throws ApfloatRuntimeException
    {
        if (y.signum() == 0)
        {
            return y;
        }
        else if (x.signum() != y.signum())
        {
            return x.negate();
        }
        else
        {
            return x;
        }
    }

    /**
     * Multiply by a power of the radix.
     * Any rounding will occur towards zero.
     *
     * @param x The argument.
     * @param scale The scaling factor.
     *
     * @return <code>x * x.radix()<sup>scale</sup></code>.
     */

    public static Apint scale(Apint x, long scale)
        throws ApfloatRuntimeException
    {
        return ApfloatMath.scale(x, scale).truncate();
    }

    /**
     * Quotient and remainder.
     *
     * @param x The dividend.
     * @param y The divisor.
     *
     * @return An array of two apints: <code>[quotient, remainder]</code>, that is <code>[x / y, x % y]</code>.
     *
     * @exception ArithmeticException In case the divisor is zero.
     */

    public static Apint[] div(Apint x, Apint y)
        throws ArithmeticException, ApfloatRuntimeException
    {
        if (y.signum() == 0)
        {
            throw new ArithmeticException("Division by zero");
        }
        else if (x.signum() == 0)
        {
            // 0 / x = 0
            return new Apint[] { x, x };
        }
        else if (y.equals(Apint.ONE))
        {
            // x / 1 = x
            return new Apint[] { x, Apint.ZEROS[x.radix()] };
        }

        long precision;
        Apfloat tx, ty;
        Apint a, b, q, r;

        a = abs(x);
        b = abs(y);

        if (a.compareTo(b) < 0)
        {
            return new Apint[] { Apint.ZEROS[x.radix()], x };   // abs(x) < abs(y)
        }
        else
        {
            precision = x.scale() - y.scale() + Apint.EXTRA_PRECISION;        // Some extra precision to avoid round-off errors
        }

        tx = x.precision(precision);
        ty = y.precision(precision);

        q = tx.divide(ty).truncate();           // Approximate division

        a = a.subtract(abs(q.multiply(y)));

        if (a.compareTo(b) >= 0)                // Fix division round-off error
        {
            q = q.add(new Apint(x.signum() * y.signum(), x.radix()));
            a = a.subtract(b);
        }
        else if (a.signum() < 0)                // Fix division round-off error
        {
            q = q.subtract(new Apint(x.signum() * y.signum(), x.radix()));
            a = a.add(b);
        }

        r = copySign(a, x);

        return new Apint[] { q, r };
    }

    /**
     * Greatest common divisor.
     * This method returns a positive number even if one of <code>a</code>
     * and <code>b</code> is negative.
     *
     * @param a First argument.
     * @param b Second argument.
     *
     * @return Greatest common divisor of <code>a</code> and <code>b</code>.
     */

    public static Apint gcd(Apint a, Apint b)
        throws ApfloatRuntimeException
    {
        return GCDHelper.gcd(a, b);
    }

    /**
     * Least common multiple.
     * This method returns a positive number even if one of <code>a</code>
     * and <code>b</code> is negative.
     *
     * @param a First argument.
     * @param b Second argument.
     *
     * @return Least common multiple of <code>a</code> and <code>b</code>.
     */

    public static Apint lcm(Apint a, Apint b)
        throws ApfloatRuntimeException
    {
        if (a.signum() == 0 && b.signum() == 0)
        {
            return Apint.ZEROS[a.radix()];
        }
        else
        {
            return abs(a.multiply(b)).divide(gcd(a, b));
        }
    }

    /**
     * Modular multiplication. Returns <code>a * b % m</code>
     *
     * @param a First argument.
     * @param b Second argument.
     * @param m Modulus.
     *
     * @return <code>a * b mod m</code>
     */

    public static Apint modMultiply(Apint a, Apint b, Apint m)
        throws ApfloatRuntimeException
    {
        return a.multiply(b).mod(m);
    }

    private static Apint modMultiply(Apint x1, Apint x2, Apint y, Apfloat inverseY)
        throws ApfloatRuntimeException
    {
        Apint x = x1.multiply(x2);

        if (x.signum() == 0)
        {
            // 0 % x = 0
            return x;
        }

        long precision = x.scale() - y.scale() + Apfloat.EXTRA_PRECISION;       // Some extra precision to avoid round-off errors
        Apint a, b, t;

        a = abs(x);
        b = abs(y);

        if (a.compareTo(b) < 0)
        {
            return x;                           // abs(x) < abs(y)
        }

        t = x.multiply(inverseY.precision(precision)).truncate();               // Approximate division

        a = a.subtract(abs(t.multiply(y)));

        if (a.compareTo(b) >= 0)                // Fix division round-off error
        {
            a = a.subtract(b);
        }
        else if (a.signum() < 0)                // Fix division round-off error
        {
            a = a.add(b);
        }

        t = copySign(a, x);

        return t;
    }

    /**
     * Modular power.
     *
     * @param a Base.
     * @param b Exponent.
     * @param m Modulus.
     *
     * @return <code>a<sup>b</sup> mod m</code>
     *
     * @exception ArithmeticException If the exponent is negative but the GCD of <code>a</code> and <code>m</code> is not 1 and the modular inverse does not exist.
     */

    public static Apint modPow(Apint a, Apint b, Apint m)
        throws ArithmeticException, ApfloatRuntimeException
    {
        if (b.signum() == 0)
        {
            if (a.signum() == 0)
            {
                throw new ArithmeticException("Zero to power zero");
            }

            return new Apint(1, a.radix());
        }
        else if (m.signum() == 0)
        {
            return m;                           // By definition
        }

        m = abs(m);

        Apfloat inverseModulus = ApfloatMath.inverseRoot(m, 1, m.scale() + Apfloat.EXTRA_PRECISION);
        a = a.mod(m);

        if (b.signum() < 0)
        {
            // Calculate modular inverse first
            a = modInverse(a, m);
            b = b.negate();
        }

        Apint two = new Apint(2, b.radix());    // Sub-optimal; the divisor could be some power of two
        Apint[] qr;

        while ((qr = div(b, two))[1].signum() == 0)
        {
            a = modMultiply(a, a, m, inverseModulus);
            b = qr[0];
        }

        Apint r = a;
        qr = div(b, two);

        while ((b = qr[0]).signum() > 0)
        {
            a = modMultiply(a, a, m, inverseModulus);
            qr = div(b, two);
            if (qr[1].signum() != 0)
            {
                r = modMultiply(r, a, m, inverseModulus);
            }
        }

        return r;
    }

    private static Apint modInverse(Apint a, Apint m)
        throws ArithmeticException, ApfloatRuntimeException
    {
        // Extended Euclidean algorithm
        Apint one = new Apint(1, m.radix()),
              x = Apint.ZERO,
              y = one,
              oldX = one,
              oldY = Apint.ZERO,
              oldA = a,
              b = m;

        while (b.signum() != 0)
        {
            Apint q = a.divide(b);

            Apint tmp = b;
            b = a.mod(b);
            a = tmp;

            tmp = x;
            x = oldX.subtract(q.multiply(x));
            oldX = tmp;

            tmp = y;
            y = oldY.subtract(q.multiply(y));
            oldY = tmp;
        }

        if (!abs(a).equals(one))
        {
            // GCD is not 1
            throw new ArithmeticException("Modular inverse does not exist");
        }

        if (oldX.signum() != oldA.signum())
        {
            // Adjust by one modulus if sign is wrong
            oldX = oldX.add(copySign(m, oldA));
        }

        return oldX;
    }

    /**
     * Factorial function. Uses the default radix.
     *
     * @param n The number whose factorial is to be calculated. Should be non-negative.
     *
     * @return <code>n!</code>
     *
     * @exception ArithmeticException If <code>n</code> is negative.
     * @exception NumberFormatException If the default radix is not valid.
     *
     * @since 1.1
     */

    public static Apint factorial(long n)
        throws ArithmeticException, NumberFormatException, ApfloatRuntimeException
    {
        return new Apint(ApfloatMath.factorial(n, Apfloat.INFINITE));
    }

    /**
     * Factorial function. Returns a number in the specified radix.
     *
     * @param n The number whose factorial is to be calculated. Should be non-negative.
     * @param radix The radix to use.
     *
     * @return <code>n!</code>
     *
     * @exception ArithmeticException If <code>n</code> is negative.
     * @exception NumberFormatException If the radix is not valid.
     *
     * @since 1.1
     */

    public static Apint factorial(long n, int radix)
        throws ArithmeticException, NumberFormatException, ApfloatRuntimeException
    {
        return new Apint(ApfloatMath.factorial(n, Apfloat.INFINITE, radix));
    }

    /**
     * Binomial coefficient. Uses the default radix.
     *
     * @param n The first argument.
     * @param k The second argument.
     *
     * @return <math xmlns="http://www.w3.org/1998/Math/MathML">
     *           <mrow>
     *             <mo>(</mo>
     *               <mfrac linethickness="0">
     *                 <mi>n</mi>
     *                 <mi>k</mi>
     *               </mfrac>
     *             <mo>)</mo>
     *           </mrow>
     *         </math>
     *
     * @since 1.11.0
     */

    public static Apint binomial(long n, long k)
        throws ApfloatRuntimeException
    {
        ApfloatContext ctx = ApfloatContext.getContext();
        int radix = ctx.getDefaultRadix();

        return binomial(n, k, radix);
    }

    /**
     * Binomial coefficient. Uses the specified radix.
     *
     * @param n The first argument.
     * @param k The second argument.
     * @param radix The radix.
     *
     * @return <math xmlns="http://www.w3.org/1998/Math/MathML">
     *           <mrow>
     *             <mo>(</mo>
     *               <mfrac linethickness="0">
     *                 <mi>n</mi>
     *                 <mi>k</mi>
     *               </mfrac>
     *             <mo>)</mo>
     *           </mrow>
     *         </math>
     *
     * @throws NumberFormatException If the radix is not valid.
     *
     * @since 1.11.0
     */

    public static Apint binomial(long n, long k, int radix)
        throws NumberFormatException, ApfloatRuntimeException
    {
        // See https://mathworld.wolfram.com/BinomialCoefficient.html for the logic on negative values
        boolean negate = false;
        if (n < 0)
        {
            if (k >= 0)
            {
                n = Math.subtractExact(k, n) - 1;
            }
            else if (k <= n)
            {
                long n1 = -k - 1;
                k = n - k;
                n = n1;
            }
            else
            {
                return Apint.ZEROS[radix];
            }
            negate = ((k & 1) == 1);
        }
        else if (k < 0)
        {
            k = Math.subtractExact(n, k);
        }
        if (k < 0 || k > n)
        {
            return Apint.ZEROS[radix];
        }
        assert (n >= 0);
        assert (k >= 0);
        if (k > n / 2)
        {
            // Optimize performance
            k = n - k;
        }
        Apint b = pochhammer(n - k + 1, k, radix).divide(factorial(k, radix));
        return (negate ? b.negate() : b);
    }

    /**
     * Binomial coefficient.
     *
     * @param n The first argument.
     * @param k The second argument.
     *
     * @return <math xmlns="http://www.w3.org/1998/Math/MathML">
     *           <mrow>
     *             <mo>(</mo>
     *               <mfrac linethickness="0">
     *                 <mi>n</mi>
     *                 <mi>k</mi>
     *               </mfrac>
     *             <mo>)</mo>
     *           </mrow>
     *         </math>
     *
     * @since 1.11.0
     */

    public static Apint binomial(Apint n, Apint k)
        throws ApfloatRuntimeException
    {
        // See https://mathworld.wolfram.com/BinomialCoefficient.html for the logic on negative values
        boolean negate = false;
        int radix = n.radix();
        Apint one = Apint.ONES[radix],
              two = new Apint(2, radix);
        if (n.signum() < 0)
        {
            if (k.signum() >= 0)
            {
                n = k.subtract(n).subtract(one);
            }
            else if (k.compareTo(n) <= 0)
            {
                Apint n1 = k.negate().subtract(one);
                k = n.subtract(k);
                n = n1;
            }
            else
            {
                return Apint.ZEROS[radix];
            }
            negate = (k.mod(two).signum() != 0);
        }
        else if (k.signum() < 0)
        {
            k = n.subtract(k);
        }
        if (k.signum() < 0 || k.compareTo(n) > 0)
        {
            return Apint.ZEROS[radix];
        }
        assert (n.signum() >= 0);
        assert (k.signum() >= 0);
        if (k.compareTo(n.divide(two)) > 0)
        {
            // Optimize performance
            k = n.subtract(k);
        }
        Apint b = pochhammer(n.subtract(k).add(one), k).divide(factorial(k.longValueExact(), radix));
        return (negate ? b.negate() : b);
    }

    // Product of the numbers n * (n + 1) * (n + 2) * ... * (n + m - 1)
    private static Apint pochhammer(long n, long m, int radix)
    {
        assert (m >= 0);
        if (m == 0)
        {
            return Apint.ONES[radix];
        }
        if (m == 1)
        {
            return new Apint(n, radix);
        }
        long k = m >>> 1;
        return pochhammer(n, k, radix).multiply(pochhammer(n + k, m - k, radix));
    }

    // Product of the numbers n * (n + 1) * (n + 2) * ... * (n + m - 1)
    private static Apint pochhammer(Apint n, Apint m)
    {
        assert (m.signum() >= 0);
        Apint one = Apint.ONES[n.radix()];
        if (m.signum() == 0)
        {
            return one;
        }
        if (m.equals(one))
        {
            return n;
        }
        Apint two = new Apint(2, n.radix());
        Apint k = m.divide(two);
        return pochhammer(n, k).multiply(pochhammer(n.add(k), m.subtract(k)));
    }

    /**
     * Product of numbers.
     * This method may perform significantly better
     * than simply multiplying the numbers sequentially.<p>
     *
     * If there are no arguments, the return value is <code>1</code>.
     *
     * @param x The argument(s).
     *
     * @return The product of the given numbers.
     *
     * @since 1.3
     */

    public static Apint product(Apint... x)
        throws ApfloatRuntimeException
    {
        return new Apint(ApfloatMath.product(x));
    }

    /**
     * Sum of numbers.<p>
     *
     * If there are no arguments, the return value is <code>0</code>.
     *
     * @param x The argument(s).
     *
     * @return The sum of the given numbers.
     *
     * @since 1.3
     */

    public static Apint sum(Apint... x)
        throws ApfloatRuntimeException
    {
        return new Apint(ApfloatMath.sum(x));
    }

    /**
     * Generates a random number. Uses the default radix.
     * Returned values are chosen pseudorandomly with (approximately)
     * uniform distribution from the range <code>0 &le; x &lt; radix<sup>digits</sup></code>.
     * The generated random numbers may have leading zeros and may thus not
     * always have exactly the requested number of digis.
     *
     * @param digits Maximum number of digits in the number.
     *
     * @return A random number, uniformly distributed between <code>0 &le; x &lt; radix<sup>digits</sup></code>.
     *
     * @exception NumberFormatException If the default radix is not valid.
     * @exception IllegalArgumentException In case the number of specified digits is invalid.
     *
     * @since 1.9.0
     */

    public static Apint random(long digits)
    {
        ApfloatContext ctx = ApfloatContext.getContext();
        int radix = ctx.getDefaultRadix();

        return random(digits, radix);
    }

    /**
     * Generates a random number.
     * Returned values are chosen pseudorandomly with (approximately)
     * uniform distribution from the range <code>0 &le; x &lt; radix<sup>digits</sup></code>.
     * The generated random numbers may have leading zeros and may thus not
     * always have exactly the requested number of digis.
     *
     * @param digits Maximum number of digits in the number.
     * @param radix The radix in which the number should be generated.
     *
     * @return A random number, uniformly distributed between <code>0 &le; x &lt; radix<sup>digits</sup></code>, in base <code>radix</code>.
     *
     * @exception NumberFormatException If the radix is not valid.
     * @exception IllegalArgumentException In case the number of specified digits is invalid.
     *
     * @since 1.9.0
     */

    public static Apint random(long digits, int radix)
    {
        if (digits <= 0)
        {
            throw new IllegalArgumentException(digits + " is not positive");
        }
        else if (digits == Apfloat.INFINITE)
        {
            throw new InfiniteExpansionException("Cannot generate an infinite number of random digits");
        }
        PushbackReader reader = new PushbackReader(new Reader()
        {
            @Override
            public int read(char[] buffer, int offset, int length)
            {
                if (this.remaining == 0)
                {
                    return -1;
                }
                length = (int) Math.min(length, this.remaining);
                for (int i = 0; i < length; i++)
                {
                    buffer[i + offset] = Character.forDigit(RANDOM.nextInt(radix), radix);
                }
                this.remaining -= length;
                return length;
            }

            @Override
            public void close()
            {
            }

            private long remaining = digits;
        });
        try
        {
            return new Apint(reader, radix);
        }
        catch (IOException ioe)
        {
            throw new ApfloatRuntimeException("Error generating random number");
        }
        
    }

    /**
     * Returns the greater of the two values.
     *
     * @param x An argument.
     * @param y Another argument.
     *
     * @return The greater of the two values.
     *
     * @since 1.9.0
     */

    public static Apint max(Apint x, Apint y)
    {
        return (x.compareTo(y) > 0 ? x : y);
    }

    /**
     * Returns the smaller of the two values.
     *
     * @param x An argument.
     * @param y Another argument.
     *
     * @return The smaller of the two values.
     *
     * @since 1.9.0
     */

    public static Apint min(Apint x, Apint y)
    {
        return (x.compareTo(y) < 0 ? x : y);
    }

    private static final Random RANDOM = new Random();
}
