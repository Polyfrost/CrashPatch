package cc.woverflow.crashpatch.gui

import gg.essential.elementa.dsl.toConstraint
import gg.essential.vigilance.gui.VigilancePalette
import java.awt.Color

object CrashPatchGUI {
    val white = Color(255, 255, 255, 200).toConstraint()
    val black = Color(0, 0, 0, 200).toConstraint()
    val focusedScrollBar = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 230)
    val unfocusedScrollBar = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 128)
}