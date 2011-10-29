package com.cadrlife.mvc;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math.stat.regression.SimpleRegression;

public class StatMain {
public static void main(String[] args) throws MathException {
	SplineInterpolator i = new SplineInterpolator();
	PolynomialSplineFunction fn = i.interpolate(new double[]{0,8000, 9000,10000,11111,100000}, new double[]{0,8,9,10,11,300});
	System.out.println(fn.value(12000));
}
}
