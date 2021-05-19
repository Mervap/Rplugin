/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.hints.parameterInfo

import com.intellij.openapi.util.io.DataInputOutputUtilRt
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef

/**
 * @property argumentNames Names of arguments that can be passed directly to **...**
 * @property functionArgNames Names of arguments that are functions whose arguments can also be passed to **...**
 */
data class RExtraNamedArgumentsInfo(val argumentNames: List<String>, val functionArgNames: List<String>) {
  fun serialize(dataStream: StubOutputStream) {
    DataInputOutputUtilRt.writeSeq(dataStream, argumentNames) { dataStream.writeName(it) }
    DataInputOutputUtilRt.writeSeq(dataStream, functionArgNames) { dataStream.writeName(it) }
  }

  companion object {
    fun deserialize(dataStream: StubInputStream): RExtraNamedArgumentsInfo {
      val argNames = DataInputOutputUtilRt.readSeq(dataStream) { StringRef.toString(dataStream.readName()) }
      val funArgNames = DataInputOutputUtilRt.readSeq(dataStream) { StringRef.toString(dataStream.readName()) }
      return RExtraNamedArgumentsInfo(argNames, funArgNames)
    }
  }
}