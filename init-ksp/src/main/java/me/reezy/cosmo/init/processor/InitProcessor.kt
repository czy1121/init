package me.reezy.cosmo.init.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import me.reezy.cosmo.init.annotation.Init
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets


class InitProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>,
) : SymbolProcessor {
    private val moduleName: String = options["init.moduleName"] ?: options["moduleName"] ?: ""
    private val formattedModuleName: String = moduleName.replace("[^0-9a-zA-Z_]+".toRegex(), "")
    private val generatedPackageName: String = (options["init.packageName"] ?: options["packageName"] ?: PKG) + ".generated"

    init {
        logger.warn(options.toString())
    }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("====== >>> [$moduleName] init")

        if (moduleName.isBlank()) {
            logger.warn("this moduleName is empty, skipped")
            return listOf()
        }
        val symbols0 = resolver.getSymbolsWithAnnotation(requireNotNull(Init::class.qualifiedName))
        val symbols1 = symbols0.filter { it.validate() }

        logger.warn("Found ${symbols0.count()} - ${symbols1.count()} InitTask in [$moduleName]")
        if (symbols0.count() > 0) {
            generate(resolver, symbols0)
        }
        return listOf()
    }

    private fun FileSpec.writeFile(codeGenerator: CodeGenerator) {
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, name)
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
    }


    @OptIn(KspExperimental::class)
    private fun generate(resolver: Resolver, symbols: Sequence<KSAnnotated>) {
        val clazzCollector = resolver.getClassDeclarationByName("$PKG.InitTaskCollector")!!.asType(listOf())
        val clazzTask = resolver.getClassDeclarationByName("$PKG.InitTask")!!.asType(listOf())

        val funcSpec = FunSpec.builder("load").addParameter("l", clazzCollector.toTypeName())

        symbols.forEach {
            val an = it.getAnnotationsByType(Init::class).first()
            it.accept(object : KSVisitorVoid() {
                override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                    val type = classDeclaration.asType(listOf())
                    if (clazzTask.isAssignableFrom(type)) {
                        logger.warn("Found $type")

                        funcSpec.addStatement(
                            "l.add(%T::class.java, %S, %L, %L, %L, %L, %L, %L)",
                            type.toTypeName(),
                            an.process,
                            an.leading,
                            an.background,
                            an.manual,
                            an.debugOnly,
                            an.priority,
                            an.depends.format()
                        )
                    } else {
                        logger.warn("Skip $type")
                    }
                }
            }, Unit)
        }

        val typeSpec = TypeSpec.classBuilder("InitLoader_$formattedModuleName")
            .addKdoc(WARNINGS)
            .addFunction(funcSpec.build())
            .build()

        val fileSpec = FileSpec.builder(generatedPackageName, "InitLoader_$formattedModuleName")
            .addType(typeSpec)
            .build()

        fileSpec.writeFile(codeGenerator)
    }

    private fun Array<String>.format(): String {
        return if (isEmpty()) "setOf()" else joinToString("\",\"", "setOf(\"", "\")") {
            if (it.contains(":")) it else "$moduleName:$it"
        }
    }

    companion object {
        private const val WARNINGS = """
   ******************************************************
   *    THIS CODE IS GENERATED BY Init, DO NOT EDIT.    *
   ******************************************************
"""
        private const val PKG = "me.reezy.cosmo.init"
    }
}



