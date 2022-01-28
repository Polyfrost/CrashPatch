package cc.woverflow.crashpatch.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.universal.USound

open class Button(
    private val unfocusedColor: ColorConstraint,
    private val focusedColor: ColorConstraint,
    private val enabled: () -> Boolean = { true },
    private val onClick: Button.() -> Unit
) : UIBlock(unfocusedColor) {

    init {
        onMouseEnter {
            animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, focusedColor)
            }
        }
        onMouseLeave {
            animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, unfocusedColor)
            }
        }
        onMouseClick {
            if (enabled.invoke()) {
                USound.playButtonPress()
                onClick.invoke(this@Button)
            }
        }
    }
}