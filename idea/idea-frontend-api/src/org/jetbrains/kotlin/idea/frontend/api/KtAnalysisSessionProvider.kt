/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import org.jetbrains.kotlin.idea.frontend.api.types.render
import org.jetbrains.kotlin.idea.util.application.runReadAction
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.utils.PrintingLogger

abstract class KtAnalysisSessionProvider {
    abstract fun getAnalysisSessionFor(contextElement: KtElement): KtAnalysisSession
}

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Should not be directly used as it is not allowed to store KtAnalysisSession in a variable, consider using `analyze` instead",
    level = DeprecationLevel.WARNING,
)
fun getAnalysisSessionFor(contextElement: KtElement): KtAnalysisSession =
    contextElement.project.service<KtAnalysisSessionProvider>().getAnalysisSessionFor(contextElement)

@Suppress("DEPRECATION")
inline fun <R> analyze(contextElement: KtElement, action: KtAnalysisSession.() -> R): R =
    getAnalysisSessionFor(contextElement).action()

inline fun <R> analyzeWithReadAction(
    contextElement: KtElement,
    crossinline action: KtAnalysisSession.() -> R
): R = runReadAction {
    analyze(contextElement, action)
}

/**
 * Run a modal window with progress bar and specified [windowTitle]
 * and execute given [action] task with [KtAnalysisSession] context
 * If [action] throws exception, then [analyseInModalWindow] will rethrow it
 */
inline fun <R> analyseInModalWindow(
    contextElement: KtElement,
    windowTitle: String,
    crossinline action: KtAnalysisSession.() -> R
): R {
    ApplicationManager.getApplication().assertIsDispatchThread()
    val task = object : Task.WithResult<R, Exception>(contextElement.project, windowTitle, /*canBeCancelled*/ true) {
        override fun compute(indicator: ProgressIndicator): R = analyzeWithReadAction(contextElement) { action() }
    }
    task.queue()
    return task.result
}