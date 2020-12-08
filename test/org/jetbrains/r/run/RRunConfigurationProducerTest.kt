package org.jetbrains.r.run

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.testFramework.MapDataContext
import org.jetbrains.r.RUsefulTestCase
import org.jetbrains.r.interpreter.RInterpreterManager
import org.jetbrains.r.interpreter.RInterpreterUtil
import org.jetbrains.r.run.configuration.RRunConfiguration
import org.jetbrains.r.run.configuration.RRunConfigurationProducer
import org.jetbrains.r.settings.RSettings

class RRunConfigurationProducerTest : RUsefulTestCase() {
  fun testCreateRunConfiguration() {
    myFixture.configureByText(
      "my_file.R",
      """
        a <- c(1, 2, 3)
      """.trimIndent()
    )

    val runConfiguration = getConfigurationFromCaret()
    assertNotNull(runConfiguration)

    if (runConfiguration !is RRunConfiguration) {
      fail("RRunConfiguration expected")
    }
    assertEquals("/src", (runConfiguration as RRunConfiguration).workingDirectory)
    assertEquals("/src/my_file.R", runConfiguration.filePath)
    assertEquals("", runConfiguration.scriptArguments)
    assertEquals(0, runConfiguration.environmentVariablesData.envs.size)
  }

  fun testDoNotCreateRunConfigurationForRMarkdown() {
    myFixture.configureByText(
      "my_file.Rmd",
      """
        ```{r i<caret>nclude = FALSE}
        ```
      """.trimIndent()
    )

    val runConfiguration = getConfigurationFromCaret()
    assertNull(runConfiguration)
  }

  override fun setUp() {
    super.setUp()
    val rInterpreter = RInterpreterManager.getInterpreterBlocking(project, RInterpreterUtil.DEFAULT_TIMEOUT)!!
    RSettings.getInstance(myFixture.project).interpreterLocation = rInterpreter.interpreterLocation
  }

  private fun getConfigurationFromCaret(): RunConfiguration? {
    val templateConfigurationContext = getConfigurationContext(myFixture.file)

    val configurationContext = RRunConfigurationProducer().createConfigurationFromContext(templateConfigurationContext)
    return configurationContext?.configurationSettings?.configuration
  }

  private fun getConfigurationContext(element: PsiElement): ConfigurationContext {
    val dataContext = MapDataContext()
    dataContext.put(CommonDataKeys.PROJECT, myFixture.project)
    dataContext.put(LangDataKeys.MODULE, ModuleUtilCore.findModuleForPsiElement(element))
    val location = PsiLocation.fromPsiElement(element)
    dataContext.put(Location.DATA_KEY, location)
    return ConfigurationContext.getFromContext(dataContext)
  }
}