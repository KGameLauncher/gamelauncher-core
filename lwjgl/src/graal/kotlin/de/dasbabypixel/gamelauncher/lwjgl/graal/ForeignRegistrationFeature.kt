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

        val `C_BOOL$LAYOUT`: OfBoolean = JAVA_BOOLEAN
        val `C_CHAR$LAYOUT`: OfByte = JAVA_BYTE
        val `C_WCHAR$LAYOUT`: OfChar = JAVA_CHAR
        val `C_SHORT$LAYOUT`: OfShort = JAVA_SHORT
        val `C_WORD$LAYOUT`: OfShort = JAVA_SHORT
        val `C_DWORD$LAYOUT`: OfInt = JAVA_INT
        val `C_INT$LAYOUT`: OfInt = JAVA_INT
        val `C_LONG$LAYOUT`: OfLong = JAVA_LONG
        val `C_LONG_LONG$LAYOUT`: OfLong = JAVA_LONG
        val `C_FLOAT$LAYOUT`: OfFloat = JAVA_FLOAT
        val `C_DOUBLE$LAYOUT`: OfDouble = JAVA_DOUBLE
        val `C_POINTER$LAYOUT`: AddressLayout = ADDRESS
    }

    override fun duringSetup(access: Feature.DuringSetupAccess?) {
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_INT$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_POINTER$LAYOUT`, `C_INT$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`,
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_INT$LAYOUT`,
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_SHORT$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_INT$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_POINTER$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`, COORD.LAYOUT))
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_WCHAR$LAYOUT`, `C_INT$LAYOUT`, COORD.LAYOUT, `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_SHORT$LAYOUT`, `C_INT$LAYOUT`, COORD.LAYOUT, `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_POINTER$LAYOUT`, `C_INT$LAYOUT`, `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_POINTER$LAYOUT`, `C_INT$LAYOUT`, `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`, `C_POINTER$LAYOUT`, `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(
            FunctionDescriptor.of(
                `C_INT$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_POINTER$LAYOUT`,
                `C_POINTER$LAYOUT`,
                COORD.LAYOUT,
                `C_POINTER$LAYOUT`
            )
        )
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_INT$LAYOUT`, `C_POINTER$LAYOUT`))
        registerForDowncall(FunctionDescriptor.of(`C_POINTER$LAYOUT`, `C_INT$LAYOUT`))
    }

    @Suppress("SpellCheckingInspection")
    object COORD {
        val LAYOUT: GroupLayout =
            MemoryLayout.structLayout(`C_SHORT$LAYOUT`.withName("x"), `C_SHORT$LAYOUT`.withName("y"))
    }
}