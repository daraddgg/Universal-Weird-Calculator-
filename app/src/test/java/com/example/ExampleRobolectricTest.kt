package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.WeirdCalculatorNluParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Universal Weird Calculator", appName)
  }

  @Test
  fun `test deterministic parsing of length query`() {
    val query = "1000 meters"
    val parsed = WeirdCalculatorNluParser.parseQuery(query)
    
    assertFalse(parsed.isAmbiguous)
    assertEquals(1000.0, parsed.value, 0.001)
    assertEquals("m", parsed.fromUnitKey)
    assertEquals("Length", parsed.category)
    
    val response = WeirdCalculatorNluParser.executeNluQuery(parsed)
    assertTrue(response.success)
    assertTrue(response.resultText.contains("1,000 Meter"))
  }

  @Test
  fun `test deterministic parsing with target elephants`() {
    val query = "500 kg in elephants"
    val parsed = WeirdCalculatorNluParser.parseQuery(query)
    
    assertFalse(parsed.isAmbiguous)
    assertEquals(500.0, parsed.value, 0.001)
    assertEquals("kg", parsed.fromUnitKey)
    assertNotNull(parsed.targetObject)
    assertEquals("African Elephant", parsed.targetObject?.name)
    
    val response = WeirdCalculatorNluParser.executeNluQuery(parsed)
    assertTrue(response.success)
    assertTrue(response.comparisons.isNotEmpty())
  }

  @Test
  fun `test chemical formula parsing with structural grouping`() {
    val atoms = com.example.calculator.ChemistryEngine.parseFormula("Fe2(SO4)3")
    assertEquals(3, atoms.size)
    assertEquals(2, atoms["Fe"])
    assertEquals(3, atoms["S"])
    assertEquals(12, atoms["O"])
  }

  @Test
  fun `test molar mass dynamic calculation`() {
    val atomsNacl = com.example.calculator.ChemistryEngine.parseFormula("NaCl")
    val mmNacl = com.example.calculator.ChemistryEngine.calculateMolarMass(atomsNacl)
    assertEquals(58.44, mmNacl, 0.01)

    val atomsH2o = com.example.calculator.ChemistryEngine.parseFormula("H2O")
    val mmH2o = com.example.calculator.ChemistryEngine.calculateMolarMass(atomsH2o)
    assertEquals(18.015, mmH2o, 0.01)
  }

  @Test
  fun `test stoichiometry and limiting reagent solver`() {
    val reaction = "2 H2 + O2 -> 2 H2O"
    val parsedEq = com.example.calculator.ChemistryEngine.parseReaction(reaction)
    
    // Give 4.0 g of H2 (approx 2 moles) and 16.0 g of O2 (approx 0.5 moles)
    // O2 should be the limiting reagent
    val masses = mapOf("H2" to 4.0, "O2" to 16.0)
    val res = com.example.calculator.ChemistryEngine.solveStoichiometry(parsedEq, masses)
    
    assertEquals("O2", res.limitingReagent)
    assertTrue(res.productYields.any { it.contains("H2O") })
  }

  @Test
  fun `test thermodynamics spontaneity calculations`() {
    val reaction = "2 H2(g) + O2(g) -> 2 H2O(l)"
    val parsedEq = com.example.calculator.ChemistryEngine.parseReaction(reaction)
    val res = com.example.calculator.ChemistryEngine.solveThermodynamics(parsedEq, 298.15)
    
    assertTrue(res.isValid)
    assertTrue(res.isSpontaneous)
    assertTrue(res.explanation.contains("SPONTANEOUS"))
  }

  @Test
  fun `test solution chemistry dilutions`() {
    // Dilute C1 = 12M, V1 = ?, to C2 = 3M, V2 = 1L. V1 should be 0.25L
    val res = com.example.calculator.ChemistryEngine.solveDilution(12.0, null, 3.0, 1.0)
    assertTrue(res.success)
    assertTrue(res.resultText.contains("0.2500 L") || res.resultText.contains("0.25"))
  }
}
