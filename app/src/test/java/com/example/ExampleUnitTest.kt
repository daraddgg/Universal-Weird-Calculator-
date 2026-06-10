package com.example

import com.example.calculator.ChemistryEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testChemistryEngineParsingSteps() {
    // Stage 1, 2, 3: Parse formula, expand parentheses, count atoms
    val atoms = ChemistryEngine.parseFormula("Ca(OH)2")
    assertEquals(1, atoms["Ca"])
    assertEquals(2, atoms["O"])
    assertEquals(2, atoms["H"])

    val complexAtoms = ChemistryEngine.parseFormula("Fe2(SO4)3")
    assertEquals(2, complexAtoms["Fe"])
    assertEquals(3, complexAtoms["S"])
    assertEquals(12, complexAtoms["O"])
  }

  @Test
  fun testChemistryEngineBalancing() {
    // Stage 4: Balance Unbalanced Chemical Equation
    val equationStr = "H2 + O2 -> H2O"
    val parsed = ChemistryEngine.parseReaction(equationStr)
    val balanced = ChemistryEngine.balanceEquation(parsed)
    
    // Check balanced coefficients: 2 H2 + O2 -> 2 H2O
    assertEquals(2, balanced.reactants[0].coefficient)
    assertEquals(1, balanced.reactants[1].coefficient)
    assertEquals(2, balanced.products[0].coefficient)
  }

  @Test
  fun testChemistryEnginePipeline() {
    // Custom sequential pipeline verification
    val parsedOutput = ChemistryEngine.processAndValidateReaction("CH4 + O2 -> CO2 + H2O")
    
    // Check balance: CH4 + 2 O2 -> CO2 + 2 H2O
    assertEquals(1, parsedOutput.reactants[0].coefficient)
    assertEquals(2, parsedOutput.reactants[1].coefficient)
    assertEquals(1, parsedOutput.products[0].coefficient)
    assertEquals(2, parsedOutput.products[1].coefficient)
  }

  @Test
  fun testChemistryEnginePolyatomicPipeline() {
    // Verify balanced reaction with polyatomic grouping parenthesis is executed without truncation
    val eqStr = "Ca(OH)2 + CO2 -> CaCO3 + H2O"
    val parsedOutput = ChemistryEngine.processAndValidateReaction(eqStr)
    
    // Check balanced coefficients: Ca(OH)2 + CO2 -> CaCO3 + H2O (already balanced 1:1:1:1)
    assertEquals(1, parsedOutput.reactants[0].coefficient)
    assertEquals(1, parsedOutput.reactants[1].coefficient)
    assertEquals(1, parsedOutput.products[0].coefficient)
    assertEquals(1, parsedOutput.products[1].coefficient)
  }

  @Test
  fun testIncompleteParsingHandling() {
    // Mismatched parenthesis
    try {
      ChemistryEngine.parseFormula("Ca(OH")
      fail("Should throw on mismatched parenthesis")
    } catch (e: IllegalArgumentException) {
      assertEquals("Parsing incomplete - expanding chemical structure", e.message)
    }

    // Incomplete reaction string (trailing operator)
    try {
      ChemistryEngine.parseReaction("H2 + O2 ->")
      fail("Should throw on trailing operator")
    } catch (e: IllegalArgumentException) {
      assertEquals("Parsing incomplete - expanding chemical structure", e.message)
    }
  }
}
