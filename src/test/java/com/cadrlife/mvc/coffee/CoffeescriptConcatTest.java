package com.cadrlife.mvc.coffee;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cadrlife.mvc.coffee.concat.CoffeescriptConcat;

public class CoffeescriptConcatTest {

	@Before
	public void setup() {
		
	}
	
	@Test
	public void shouldCompileZeroFiles() {
		
	}
	
	@Test
	public void findClasses() {
		List<String> classDefs = new CoffeescriptConcat().findClasses("class Horse extends Animal\nclass Animal");
		assertEquals("Horse", classDefs.get(0));
		assertEquals("Animal", classDefs.get(1));
	}
	
	@Test
	public void findClassDependenciesForSuperclasses() {
		List<String> classDefs = new CoffeescriptConcat().findClassDependencies("class Horse extends Animal\nclass Animal");
		assertEquals("Animal", classDefs.get(0));
	}
	
	@Test
	public void findClassDependenciesForRequireDirective() {
		List<String> classDefs = new CoffeescriptConcat().findClassDependencies("#= require ClassName");
		assertEquals("ClassName", classDefs.get(0));
	}
	
	@Test
	public void findFileDependencies() {
		List<String> classDefs = new CoffeescriptConcat().findFileDependencies("#= require <Filename.coffee>\n() ->  ");
		assertEquals("Filename.coffee", classDefs.get(0));
	}
	@Test
	public void findFileDependenciesNoSpace() {
		List<String> classDefs = new CoffeescriptConcat().findFileDependencies("#= require<Filename.coffee>");
		assertEquals("Filename.coffee", classDefs.get(0));
	}
}
