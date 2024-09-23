package de.dasbabypixel.gamelauncher.api.util

@JvmInline
@Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")
value class Color(inline val rgba: UInt) {
    constructor(
        red: Float, green: Float, blue: Float, alpha: Float = 1F
    ) : this(convert(red), convert(green), convert(blue), convert(alpha))

    constructor(
        red: UInt, green: UInt, blue: UInt, alpha: UInt = UByte.MAX_VALUE.toUInt()
    ) : this(red.toUByte(), green.toUByte(), blue.toUByte(), alpha.toUByte())

    @Suppress("UNUSED_PARAMETER")
    constructor(
        red: Int, green: Int, blue: Int, alpha: Int = Byte.MAX_VALUE.toInt(), ignored: Byte = 0
    ) : this(red.toUByte(), green.toUByte(), blue.toUByte(), alpha.toUByte())

    @Suppress("UNUSED_PARAMETER")
    constructor(
        red: Byte, green: Byte, blue: Byte, alpha: Byte = Byte.MAX_VALUE, ignored: Byte = 0
    ) : this(red.toUByte(), green.toUByte(), blue.toUByte(), alpha.toUByte())

    constructor(
        red: UByte, green: UByte, blue: UByte, alpha: UByte = UByte.MAX_VALUE
    ) : this(red0(red) or green0(green) or blue0(blue) or alpha0(alpha))

    fun withGB(green: Int, blue: Int): Color = withGB(green.toUByte(), blue.toUByte())
    fun withGB(green: UByte, blue: UByte): Color = Color(rgba and 0xFF0000FFu or green0(green) or blue0(blue))
    fun withRB(red: Int, blue: Int): Color = withRB(red.toUByte(), blue.toUByte())
    fun withRB(red: UByte, blue: UByte): Color = Color(rgba and 0x00FF00FFu or red0(red) or blue0(blue))
    fun withRG(red: Int, green: Int): Color = withRG(red.toUByte(), green.toUByte())
    fun withRG(red: UByte, green: UByte): Color = Color(rgba and 0x0000FFFFu or red0(red) or green0(green))
    fun withRGB(red: Int, green: Int, blue: Int): Color = withRGB(red.toUByte(), green.toUByte(), blue.toUByte())
    fun withRGB(red: UByte, green: UByte, blue: UByte): Color = Color(red, green, blue, alpha)

    infix fun withRed(red: Int): Color = withRed(red.toUByte())
    infix fun withRed(red: UByte): Color = Color(rgba and 0x00FFFFFFu or red0(red))
    infix fun withGreen(green: Int): Color = withGreen(green.toUByte())
    infix fun withGreen(green: UByte): Color = Color(rgba and 0xFF00FFFFu or green0(green))
    infix fun withBlue(blue: Int): Color = withBlue(blue.toUByte())
    infix fun withBlue(blue: UByte): Color = Color(rgba and 0xFFFF00FFu or blue0(blue))
    infix fun withAlpha(alpha: Int): Color = withAlpha(alpha.toUByte())
    infix fun withAlpha(alpha: UByte): Color = Color(rgba and 0xFFFFFF00u or alpha0(alpha))

    inline val red: UByte
        get() = (rgba shr SHIFT_RED).toUByte()
    inline val green: UByte
        get() = (rgba shr SHIFT_GREEN).toUByte()
    inline val blue: UByte
        get() = (rgba shr SHIFT_BLUE).toUByte()
    inline val alpha: UByte
        get() = (rgba shr SHIFT_ALPHA).toUByte()

    inline val rgb: UInt
        get() = (rgba shr 8) and 0xFFFFFFu

    inline val fred: Float
        get() = red.toFloat() / 255F
    inline val fgreen: Float
        get() = green.toFloat() / 255F
    inline val fblue: Float
        get() = blue.toFloat() / 255F
    inline val falpha: Float
        get() = alpha.toFloat() / 255F

    inline val ired: UInt
        get() = red.toUInt()
    inline val igreen: UInt
        get() = green.toUInt()
    inline val iblue: UInt
        get() = blue.toUInt()
    inline val ialpha: UInt
        get() = alpha.toUInt()

    inline val hex: String
        get() = rgbaHex
    inline val rgbaHex: String
        get() = String.format("%06X", rgba.toInt())
    inline val rgbHex: String
        get() = String.format("%06X", rgb.toInt())

    companion object {
        const val SHIFT_RED = 24
        const val SHIFT_GREEN = 16
        const val SHIFT_BLUE = 8
        const val SHIFT_ALPHA = 0
        private fun convert(f: Float): UByte {
            if (f >= 1F) return 0xFFu
            if (f <= 0F) return 0u
            val t = f * 255F
            return Math.round(t).toUByte()
        }

        private fun c0(b: UByte, s: Int) = b.toUInt() and 0xFFu shl s
        private fun red0(b: UByte) = c0(b, SHIFT_RED)
        private fun green0(b: UByte) = c0(b, SHIFT_GREEN)
        private fun blue0(b: UByte) = c0(b, SHIFT_BLUE)
        private fun alpha0(b: UByte) = c0(b, SHIFT_ALPHA)
    }
}