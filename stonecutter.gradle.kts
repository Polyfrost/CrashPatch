plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.11"

stonecutter tasks {
    order("publishModrinth")
}