package cc.woverflow.crashpatch.gui.components

import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.dsl.*

class TextButton(
    text: String,
    unfocusedColor: ColorConstraint,
    focusedColor: ColorConstraint,
    enabled: () -> Boolean = { true },
    onClick: Button.() -> Unit
) : Button(unfocusedColor, focusedColor, enabled, onClick) {

    private val theThing by UIText(text, shadow = false) constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf this

    init {
        constrain {
            width = ChildBasedSizeConstraint() + 10.pixels()
            height = ChildBasedSizeConstraint() + 10.pixels()
        }
    }

    fun setText(string: String) {
        theThing.setText(string)
    }
}