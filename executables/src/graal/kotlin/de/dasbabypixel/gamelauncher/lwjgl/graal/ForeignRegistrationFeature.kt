package de.dasbabypixel.gamelauncher.lwjgl.graal

import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeForeignAccess.registerForDowncall
import java.lang.foreign.AddressLayout
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemoryLayout
import java.lang.foreign.ValueLayout.*

class ForeignRegistrationFeature : Feature {
    companion object {
        val C_WCHAR: OfChar = JAVA_CHAR
        val C_SHORT: OfShort = JAVA_SHORT
        val C_INT: OfInt = JAVA_INT
        val C_POINTER: AddressLayout = ADDRESS
    }

    override fun duringSetup(access: Feature.DuringSetupAccess?) {
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_INT
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_POINTER, C_INT
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT,
                C_INT,
                C_POINTER,
                C_INT,
                C_INT,
                C_POINTER,
                C_INT,
                C_POINTER
            )
        )
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER, C_SHORT))
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER, C_INT))
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER))
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER))
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER, COORD.LAYOUT))
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_WCHAR, C_INT, COORD.LAYOUT, C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_SHORT, C_INT, COORD.LAYOUT, C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT,
                C_POINTER,
                C_POINTER,
                C_INT,
                C_POINTER,
                C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT, C_POINTER, C_POINTER
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                C_INT,
                C_POINTER,
                C_POINTER,
                C_POINTER,
                COORD.LAYOUT,
                C_POINTER
            )
        )
        registerForDowncall(FunctionDescriptor.of(C_INT))
        registerForDowncall(FunctionDescriptor.of(C_INT, C_POINTER))
        registerForDowncall(FunctionDescriptor.of(C_POINTER, C_INT))
    }

    @Suppress("SpellCheckingInspection")
    object COORD {
        val LAYOUT: GroupLayout =
            MemoryLayout.structLayout(C_SHORT.withName("x"), C_SHORT.withName("y"))
    }
}