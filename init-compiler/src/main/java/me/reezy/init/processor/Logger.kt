package me.reezy.init.processor

import javax.annotation.processing.Messager
import javax.tools.Diagnostic


class Logger(private val messager: Messager, private val tag: String) {

    fun info(message: CharSequence?) {
        println("$tag >>> $message")
        messager.printMessage(Diagnostic.Kind.NOTE, "$tag >>> $message")
    }

    fun warning(message: CharSequence?) {
        println("$tag >>> $message")
        messager.printMessage(Diagnostic.Kind.WARNING, "$tag >>> $message")
    }

    fun error(message: CharSequence?) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$tag >>> $message")
    }

    fun error(error: Throwable) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$tag >>> [${error.message}]\n ${formatStackTrace(error.stackTrace)}")
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString()).append("\n")
        }
        return sb.toString()
    }
}